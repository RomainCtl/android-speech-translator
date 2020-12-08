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
import fr.enssat.babelblock.chantrel_perrot.tools.*
import fr.enssat.babelblock.chantrel_perrot.tools.ui.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.speech_to_text_tool_chain.*
import kotlinx.android.synthetic.main.text_to_speech_tool_chain.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    lateinit var speechToText: SpeechToTextTool
    lateinit var translator: TranslationTool
    lateinit var textToSpeech: TextToSpeechTool
    lateinit var toolChain: ToolChain

    var speakingLanguage: Locale = Locale.getDefault()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check "RECORD_AUDIO" permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        val service = BlockService(this)
        speechToText = service.speechToText()
        translator = service.translator()
        textToSpeech = service.textToSpeech()

        initToolChain()
        initSpeechButtons()
    }

    override fun onDestroy() {
        speechToText.close()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == recordAudioRequestCode && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), recordAudioRequestCode)
    }

    private fun initToolChain() {
        //create toolchain and its adapter
        toolChain = ToolChain()
        val adapter = ToolChainAdapter(toolChain)

        //dedicated drag and drop mover helper
        val moveHelper = ToolChainMoveHelper.create(adapter)
        moveHelper.attachToRecyclerView(tool_chain_list)

        //see tool_chain_list in activity_tool_chain.xml
        //chain of tools
        tool_chain_list.adapter = adapter

        //see tool_list in activity_tool_chain.xml
        val toolListAdapter = ToolListAdapter(toolChain, translator)
        tool_list.adapter = toolListAdapter

        // Input language (and select the default)
        selected_language.adapter = ArrayAdapter<Language>(this, android.R.layout.simple_spinner_dropdown_item, toolListAdapter.availableLanguages)
        selected_language.setSelection(toolListAdapter.availableLanguages.indexOf(Language(speakingLanguage.isO3Language.take(2))))
        selected_language.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                speakingLanguage = (parent?.getItemAtPosition(position) as Language).toLocale()
                speechToText.setLocale(speakingLanguage)
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
                            toolChain.display(0, text)
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

                if (toolChain.size == 0) {
                    // No translation tool
                    text = recognized_text.text as String
                    from = speakingLanguage
                } else {
                    // With translation tools
                    val tool = toolChain.get(toolChain.size - 1)
                    text = tool.output
                    from = tool.language.toLocale()
                }
                textToSpeech.speak(text, from)
            }
            false
        }
    }
}