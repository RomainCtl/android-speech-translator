package fr.enssat.babelblock.chantrel_perrot.tools

import android.content.Context
import fr.enssat.babelblock.chantrel_perrot.tools.handler.SpeechRecognizerHandler
import fr.enssat.babelblock.chantrel_perrot.tools.handler.TextToSpeechHandler
import fr.enssat.babelblock.chantrel_perrot.tools.handler.TranslatorHandler
import java.util.Locale

interface TextToSpeechTool {
    fun speak(text: String, locale: Locale)
    fun stop()
    fun close()
}

interface TranslationTool {
    fun translate(text: String, callback: Locale, uk: Locale, function: (String) -> Unit) {}
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

class BlockService(val context: Context) {
    fun textToSpeech():TextToSpeechTool {
        return TextToSpeechHandler(context.applicationContext)
    }

    fun translator(): TranslationTool =
        TranslatorHandler(context.applicationContext)

    fun speechToText(from: Locale = Locale.getDefault()): SpeechToTextTool =
        SpeechRecognizerHandler(context.applicationContext, from)
}