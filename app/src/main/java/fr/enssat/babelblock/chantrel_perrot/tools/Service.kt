package fr.enssat.babelblock.chantrel_perrot.tools

import android.content.Context
import fr.enssat.babelblock.chantrel_perrot.tools.impl.TextToSpeechHandler
import java.util.Locale

interface TextToSpeechTool {
    fun speak(text: String)
    fun stop()
    fun close()
}

class BlockService(val context: Context) {
    fun textToSpeech():TextToSpeechTool {
        val locale = Locale.getDefault()
        return TextToSpeechHandler(context.applicationContext, locale)}

}