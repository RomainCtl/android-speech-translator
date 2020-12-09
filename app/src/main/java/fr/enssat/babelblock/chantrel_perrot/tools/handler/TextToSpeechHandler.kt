package fr.enssat.babelblock.chantrel_perrot.tools.handler

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import fr.enssat.babelblock.chantrel_perrot.tools.TextToSpeechTool
import java.util.*

class TextToSpeechHandler(context: Context): TextToSpeechTool {

    private val speaker = TextToSpeech(context) { status -> Log.d("Speak", "status: $status") }

    override fun speak(text: String, locale: Locale) {
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