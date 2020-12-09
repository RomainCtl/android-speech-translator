package fr.enssat.babelblock.chantrel_perrot.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.google.mlkit.nl.translate.TranslateLanguage
import fr.enssat.babelblock.chantrel_perrot.model.Language
import fr.enssat.babelblock.chantrel_perrot.model.Tool
import fr.enssat.babelblock.chantrel_perrot.tools.worker.TranslatorWorker
import timber.log.Timber
import java.util.*

class MainActivityViewModel(val context: Context) : ViewModel() {

    companion object {
        private const val UNIQUE_WORK_NAME = "translationChain"
        private const val TAG_TRANSLATE = "translation_tag"
        const val INPUT_KEY = "input"
        const val TO_KEY = "to"
        const val FROM_KEY = "from"
        const val OUTPUT_KEY = "output"
        const val POSITION_KEY = "position"
    }

    var availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
        .map {
            Language(it)
        }

    var speakingLanguage: Locale = Locale.getDefault()
    var spokenText: String = ""

    private val tools: MutableList<Tool> = mutableListOf()
    val size get() = tools.size

    private val workManager = WorkManager.getInstance(context)
    internal val outputWorkInfos: LiveData<List<WorkInfo>>

    init {
        Timber.i("created!")
        workManager.pruneWork()
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_TRANSLATE)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("destroyed!")
    }

    private var onChangeListener: (() -> Unit)? = null
    private var onItemRemovedListener: ((position: Int) -> Unit)? = null

    //callback to invoke void method on toolchain Changes
    //see init of ToolChainAdapter
    fun setOnChangeListener(callback: () -> Unit) {
        onChangeListener = callback
    }
    fun setOnItemRemovedListener(callback: (position: Int) -> Unit) {
        onItemRemovedListener = callback
    }

    fun setText(text: String, position: Int) {
        tools[position].inProgress = false
        tools[position].text = text
        onChangeListener?.invoke()
    }

    private fun setInProgress(inProgress: Boolean, startPosition: Int = 0) {
        for (i in startPosition until size)
            tools[i].inProgress = inProgress
    }

    fun add(tool: Tool) {
        cancelWork()
        tools.add(tool)
        onChangeListener?.invoke()
    }

    fun remove(position: Int) {
        cancelWork()
        tools.removeAt(position)
        onItemRemovedListener?.invoke(position)
    }

    fun get(index: Int) = tools[index]

    //remove and insert
    fun move(from: Int, to: Int) {
        cancelWork()
        val dragged = tools.removeAt(from)
        tools.add(to, dragged)
    }

    fun applyTranslation(position: Int) {
        // from speech Recognizer
        var input: String = spokenText
        var from: Locale = speakingLanguage
        // Or from previous translator block
        if (position != 0) {
            input = this.tools[position-1].text
            from = this.tools[position-1].language.toLocale()
        }

        setInProgress(true, position)

        // Ensure unique work, replace if one already exist
        var continuation = workManager.beginUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TranslatorWorker>()
                .setInputData(this.tools[position].createInputDataForTranslator(input, from, position))
                .addTag(TAG_TRANSLATE)
                .build()
        )

        for (i in position+1 until size) {
            input = this.tools[i-1].text // will be override by the output of previous worker operation
            from = this.tools[i-1].language.toLocale()

            continuation = continuation.then(
                OneTimeWorkRequestBuilder<TranslatorWorker>()
                    .setInputData(this.tools[i].createInputDataForTranslator(input, from, i))
                    .addTag(TAG_TRANSLATE)
                    .build()
            )
        }
        continuation.enqueue()
    }

    private fun cancelWork() {
        if (!workManager.getWorkInfosByTag(TAG_TRANSLATE).isDone) {
            workManager.cancelAllWorkByTag(TAG_TRANSLATE)
            setInProgress(false)
            Timber.i("Translation chain canceled!")
        }
    }
}
