package fr.enssat.babelblock.chantrel_perrot.model

import android.util.Log
import fr.enssat.babelblock.chantrel_perrot.tools.BlockService
import fr.enssat.babelblock.chantrel_perrot.ui.utils.ToolInterface
import java.util.*

class Tool(var language: Language, var title: String, var text: String) : ToolInterface {

    override fun run(input: String, from: Locale, callback: (String) -> Unit) {
        callback("translation in progress...")
        BlockService.translator().translate(input, from, language.toLocale()) { text ->
            callback(text)
        }
    }

    override fun close() {
        Log.d(this.javaClass.simpleName, "$title close")
    }
}
