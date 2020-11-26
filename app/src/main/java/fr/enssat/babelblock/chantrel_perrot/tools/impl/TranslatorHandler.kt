package fr.enssat.babelblock.chantrel_perrot.tools.impl

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.translate.*
import fr.enssat.babelblock.chantrel_perrot.tools.TranslationTool
import java.util.*

class TranslatorHandler(context: Context): TranslationTool {

    companion object {
        // This specifies the number of translators instance we want to keep in our LRU cache.
        // Each instance of the translator is built with different options based on the source
        // language and the target language, and since we want to be able to manage the number of
        // translator instances to keep around, an LRU cache is an easy way to achieve this.
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

    override fun translate(text: String, from: Locale, to: Locale, callback: (String) -> Unit) {
        val source = from.language
        val target = to.language
        if (source == null || target == null || text == null || text.isEmpty()) {
            callback("")
        }
        val sourceLangCode = TranslateLanguage.fromLanguageTag(source)!!
        val targetLangCode = TranslateLanguage.fromLanguageTag(target)!!
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        translators[options].downloadModelIfNeeded().continueWithTask { task ->
            if (task.isSuccessful) {
                translators[options].translate(text)
                    .addOnSuccessListener(callback)
                    .addOnFailureListener{e -> Log.e(this::class.simpleName, "translation failed", e) }
            } else {
                Tasks.forException<String>(
                    task.exception
                        ?: Exception("Unknown error occurred.")
                )
            }
        }
    }

    override fun close() {
        // Each new instance of a translator needs to be closed appropriately.
        translators.evictAll()
    }
}