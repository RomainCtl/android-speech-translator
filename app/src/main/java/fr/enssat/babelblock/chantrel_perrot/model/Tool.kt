package fr.enssat.babelblock.chantrel_perrot.model

import androidx.work.Data
import fr.enssat.babelblock.chantrel_perrot.ui.utils.ToolInterface
import fr.enssat.babelblock.chantrel_perrot.ui.viewmodel.MainActivityViewModel
import timber.log.Timber
import java.util.*

class Tool(var language: Language, var title: String, var text: String) : ToolInterface {

    var inProgress: Boolean = false

    override fun close() {
        Timber.d( "$title close")
    }

    fun createInputDataForTranslator(input: String, from: Locale, position: Int): Data {
        val builder = Data.Builder()
        builder.putString(MainActivityViewModel.INPUT_KEY, input)
        builder.putString(MainActivityViewModel.FROM_KEY, from.isO3Language)
        builder.putString(MainActivityViewModel.TO_KEY, language.toLocale().isO3Language)
        builder.putInt(MainActivityViewModel.POSITION_KEY, position)
        return builder.build()
    }
}
