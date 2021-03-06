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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import fr.enssat.babelblock.chantrel_perrot.model.Language
import fr.enssat.babelblock.chantrel_perrot.tools.*
import fr.enssat.babelblock.chantrel_perrot.ui.adapter.ToolChainAdapter
import fr.enssat.babelblock.chantrel_perrot.ui.ToolChainMoveHelper
import fr.enssat.babelblock.chantrel_perrot.ui.adapter.ToolListAdapter
import fr.enssat.babelblock.chantrel_perrot.ui.viewmodel.MainActivityViewModel
import fr.enssat.babelblock.chantrel_perrot.ui.viewmodel.MainActivityViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.speech_to_text_tool_chain.*
import kotlinx.android.synthetic.main.text_to_speech_tool_chain.*
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity()  {
    private val recordAudioRequestCode = 1

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewModelFactory: MainActivityViewModelFactory

    lateinit var speechToText: SpeechToTextTool
    lateinit var textToSpeech: TextToSpeechTool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        // Define service context
        BlockService.setContext(this)

        viewModelFactory = MainActivityViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainActivityViewModel::class.java)

        // Check "RECORD_AUDIO" permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        speechToText = BlockService.speechToText(viewModel.speakingLanguage)
        textToSpeech = BlockService.textToSpeech()

        initToolChain()
        initSpeechButtons()

        viewModel.outputWorkInfos.observe(this, workInfosObserver())
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
                Timber.d("Push to talk btn pressed")
                v.performClick()
                speechToText.start(object : SpeechToTextTool.Listener {
                    override fun onResult(text: String, isFinal: Boolean) {
                        if (isFinal) {
                            recognized_text.text = text
                            viewModel.spokenText = text
                            viewModel.applyTranslation(0)
                        }
                    }
                })
            } else if (event.action == MotionEvent.ACTION_UP) {
                Timber.d("Push to talk btn releases")
                speechToText.stop()
            }
            false
        }

        listen_to.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Timber.d("Listen btn pressed")
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

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            for (workInfo in listOfWorkInfo) {
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val output = workInfo.outputData.getString(MainActivityViewModel.OUTPUT_KEY)!!
                    val position = workInfo.outputData.getInt(MainActivityViewModel.POSITION_KEY, 0) -1
                    viewModel.setText(output, position)
                }
            }
        }
    }
}