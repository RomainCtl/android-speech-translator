package fr.enssat.babelblock.chantrel_perrot.tools

import android.content.Context
import fr.enssat.babelblock.chantrel_perrot.tools.handler.SpeechRecognizerHandler
import fr.enssat.babelblock.chantrel_perrot.tools.handler.TextToSpeechHandler
import java.util.Locale

interface TextToSpeechTool {
    fun speak(text: String, locale: Locale)
    fun stop()
    fun close()
}

interface SpeechToTextTool {
    interface Listener {
        fun onResult(text: String, isFinal: Boolean)
    }
    fun start(listener: Listener)
    fun stop()
    fun close()
    fun setLocale(locale: Locale)
}

class BlockService {
    companion object {
        private lateinit var context: Context

        fun setContext(context: Context) {
            this.context = context
        }

        fun textToSpeech(): TextToSpeechTool {
            return TextToSpeechHandler(context.applicationContext)
        }

        fun speechToText(from: Locale): SpeechToTextTool =
            SpeechRecognizerHandler(context.applicationContext, from)
    }
}