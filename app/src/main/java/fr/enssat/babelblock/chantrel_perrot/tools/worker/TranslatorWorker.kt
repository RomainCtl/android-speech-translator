package fr.enssat.babelblock.chantrel_perrot.tools.worker

import android.content.Context
import android.util.LruCache
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.translate.*
import fr.enssat.babelblock.chantrel_perrot.ui.viewmodel.MainActivityViewModel
import timber.log.Timber

class TranslatorWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    companion object {
        private const val NUM_TRANSLATORS = 3
    }

    private val translators =
        object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
            override fun create(options: TranslatorOptions): Translator {
                return Translation.getClient(options)
            }
            override fun entryRemoved(
                evicted: Boolean,
                key: TranslatorOptions,
                oldValue: Translator,
                newValue: Translator?
            ) {
                oldValue.close()
            }
        }

    override fun doWork(): Result {
        val input = inputData.getString(MainActivityViewModel.INPUT_KEY)!!
        val source = inputData.getString(MainActivityViewModel.FROM_KEY)!!
        val target = inputData.getString(MainActivityViewModel.TO_KEY)!!
        val position = inputData.getInt(MainActivityViewModel.POSITION_KEY, 0)

        val sourceLangCode = TranslateLanguage.fromLanguageTag(source.take(2))!!
        val targetLangCode = TranslateLanguage.fromLanguageTag(target.take(2))!!
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()

        var result: Result

        try {
            if (input.isEmpty()) {
                Timber.e("Error applying translation")
                result = Result.failure()
            } else {
                // Download if model does not exist
                val downloadTask = translators[options].downloadModelIfNeeded()
                Tasks.await(downloadTask)

                if (downloadTask.isSuccessful) {
                    val translationTask = translators[options].translate(input)
                    Tasks.await(translationTask)

                    if (translationTask.isSuccessful) {
                        val output = workDataOf(
                            MainActivityViewModel.INPUT_KEY to translationTask.result.toString(), // for next worker
                            MainActivityViewModel.OUTPUT_KEY to translationTask.result.toString(), // to display on UI
                            MainActivityViewModel.POSITION_KEY to position+1
                        )
                        result = Result.success(output)
                    } else {
                        Timber.e("Translation failed")
                        throw Exception()
                    }
                } else {
                    Timber.e("Failure to download languages")
                    throw Exception()
                }
            }
        } catch (throwable: Throwable) {
            result = Result.failure()
        } finally {
            translators[options].close()
        }

        return result
    }
}