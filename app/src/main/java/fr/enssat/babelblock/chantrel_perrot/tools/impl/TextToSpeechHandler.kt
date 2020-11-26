package fr.enssat.babelblock.chantrel_perrot.tools.impl

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import fr.enssat.babelblock.chantrel_perrot.tools.TextToSpeechTool
import java.util.*

class TextToSpeechHandler(context: Context, val locale: Locale): TextToSpeechTool {

    private val speaker = TextToSpeech(context, object: TextToSpeech.OnInitListener {
        override fun onInit(status: Int) {
            Log.d("Speak", "status: $status")
        }
    })

    override fun speak(text: String) {
        speaker.language = locale
        speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun stop() {
        speaker.stop()
    }

    override fun close() {
        speaker.shutdown()
    }
}