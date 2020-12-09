package fr.enssat.babelblock.chantrel_perrot.ui.utils

import java.util.*

interface ToolInterface {
    fun run(input: String, from: Locale, callback: (String) -> Unit)
    fun close()
}