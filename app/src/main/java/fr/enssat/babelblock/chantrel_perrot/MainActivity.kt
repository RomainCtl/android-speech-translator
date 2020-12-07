package fr.enssat.babelblock.chantrel_perrot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.enssat.babelblock.chantrel_perrot.tools.BlockService
import fr.enssat.babelblock.chantrel_perrot.tools.SpeechToTextTool
import fr.enssat.babelblock.chantrel_perrot.tools.TextToSpeechTool
import fr.enssat.babelblock.chantrel_perrot.tools.TranslationTool
import fr.enssat.babelblock.chantrel_perrot.tools.ui.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.speech_to_text_tool_chain.*
import kotlinx.android.synthetic.main.text_to_speech_tool_chain.*

class MainActivity : AppCompatActivity() {
    private val recordAudioRequestCode = 1

    lateinit var speechToText: SpeechToTextTool
    lateinit var translator: TranslationTool
    lateinit var textToSpeech: TextToSpeechTool
    lateinit var toolChain: ToolChain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check "RECORD_AUDIO" permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermission()
        }

        val service: BlockService = BlockService(this)
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
        val adpater = ToolChainAdapter(toolChain)

        //dedicated drag and drop mover helper
        val moveHelper = ToolChainMoveHelper.create(adpater)
        moveHelper.attachToRecyclerView(tool_chain_list)

        //see tool_chain_list in activity_tool_chain.xml
        //chain of tools
        tool_chain_list.adapter = adpater

        //see tool_list in activity_tool_chain.xml
        tool_list.adapter = ToolListAdapter(toolChain, translator)
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
                Log.d("Reco UI", "Button releases")
                speechToText.stop()
            }
            false
        }

        listen_to.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                Log.d(this.javaClass.simpleName, "Listen btn pressed")
                v.performClick()
                val tool = toolChain.get(toolChain.size - 1)
                textToSpeech.speak(tool.output, tool.language.toLocale())
            }
            false
        }
    }
}