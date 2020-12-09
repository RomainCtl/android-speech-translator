package fr.enssat.babelblock.chantrel_perrot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import fr.enssat.babelblock.chantrel_perrot.model.Language
import fr.enssat.babelblock.chantrel_perrot.tools.*
import fr.enssat.babelblock.chantrel_perrot.ui.adapter.ToolChainAdapter
import fr.enssat.babelblock.chantrel_perrot.ui.ToolChainMoveHelper
import fr.enssat.babelblock.chantrel_perrot.ui.adapter.ToolListAdapter
import fr.enssat.babelblock.chantrel_perrot.ui.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.speech_to_text_tool_chain.*
import kotlinx.android.synthetic.main.text_to_speech_tool_chain.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    lateinit var viewModel: MainActivityViewModel

    lateinit var speechToText: SpeechToTextTool
    lateinit var translator: TranslationTool
    lateinit var textToSpeech: TextToSpeechTool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Define service context
        BlockService.setContext(this)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Check "RECORD_AUDIO" permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        speechToText = BlockService.speechToText(viewModel.speakingLanguage)
        translator = BlockService.translator()
        textToSpeech = BlockService.textToSpeech()

        initToolChain()
        initSpeechButtons()
    }

    override fun onDestroy() {
        speechToText.close()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
    }

    private fun initToolChain() {
        //create toolchain and its adapter
        val adapter = ToolChainAdapter(viewModel)

        //dedicated drag and drop mover helper
        val moveHelper = ToolChainMoveHelper.create(adapter)
        moveHelper.attachToRecyclerView(tool_chain_list)

        //see tool_chain_list in activity_tool_chain.xml
        //chain of tools
        tool_chain_list.adapter = adapter

        //see tool_list in activity_tool_chain.xml
        tool_list.adapter = ToolListAdapter(viewModel)

        // Input language (and select the default)
        selected_language.adapter = ArrayAdapter<Language>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            viewModel.availableLanguages
        )
        // set default
        selected_language.setSelection(viewModel.availableLanguages.indexOf(Language(viewModel.speakingLanguage.isO3Language.take(2))))
        selected_language.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.speakingLanguage = (parent?.getItemAtPosition(position) as Language).toLocale()
                speechToText.setLocale(viewModel.speakingLanguage)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initSpeechButtons() {
        push_to_talk.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d(this.javaClass.simpleName, "Push to talk btn pressed")
                v.performClick()
                speechToText.start(object : SpeechToTextTool.Listener {
                    override fun onResult(text: String, isFinal: Boolean) {
                        if (isFinal) {
                            recognized_text.text = text
                            viewModel.spokenText = text
                            viewModel.display(0)
                        }
                    }
                })
            } else if (event.action == MotionEvent.ACTION_UP) {
                Log.d(this.javaClass.simpleName, "Push to talk btn releases")
                speechToText.stop()
            }
            false
        }

        listen_to.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d(this.javaClass.simpleName, "Listen btn pressed")
                v.performClick()
                val text: String
                val from: Locale

                if (viewModel.size == 0) {
                    // No translation tool
                    text = recognized_text.text as String
                    from = viewModel.speakingLanguage
                } else {
                    // With translation tools
                    val tool = viewModel.get(viewModel.size - 1)
                    text = tool.text
                    from = tool.language.toLocale()
                }
                textToSpeech.speak(text, from)
            }
            false
        }
    }
}