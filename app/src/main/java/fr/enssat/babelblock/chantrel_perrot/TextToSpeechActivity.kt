package fr.enssat.babelblock.chantrel_perrot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.enssat.babelblock.chantrel_perrot.tools.BlockService
import fr.enssat.babelblock.chantrel_perrot.tools.TextToSpeechTool
import kotlinx.android.synthetic.main.activity_text_to_speech.*

class TextToSpeechActivity: AppCompatActivity() {

    lateinit var speaker: TextToSpeechTool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_speech)

        val service = BlockService(this)
        speaker = service.textToSpeech()

        //cf build.gradle kotlin-android-extensions
        //cf import kotlinx.android.synthetic.main.activity_text_to_speech.*
        //say goodbye to findviewbyid, recovering and binding view from layout

        play_button.setOnClickListener {
            val text = edit_query.text.toString()
            speaker.speak(text)
        }
    }

    override fun onDestroy() {
        speaker.close()
        super.onDestroy()
    }
}