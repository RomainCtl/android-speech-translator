package fr.enssat.babelblock.chantrel_perrot.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import fr.enssat.babelblock.chantrel_perrot.model.Language
import fr.enssat.babelblock.chantrel_perrot.model.Tool
import java.util.*

class MainActivityViewModel : ViewModel() {

    var availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
        .map {
            Language(it)
        }
    var speakingLanguage: Locale = Locale.getDefault()

    private val list: MutableList<Tool> = mutableListOf()
    val size get() = list.size

    init {
        Log.i(this.javaClass.simpleName, "created!")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(this.javaClass.simpleName, "destroyed!")
    }

    private var onChangeListener: (() -> Unit)? = null

    //callback to invoke void method on toolchain Changes
    //see init of ToolChainAdapter
    fun setOnChangeListener(callback: () -> Unit) {
        onChangeListener = callback
    }

    fun add(tool: Tool) {
        list.add(tool)
        onChangeListener?.invoke()
    }

    fun get(index: Int) = list[index]

    //remove and insert
    fun move(from: Int, to: Int) {
        val dragged = list.removeAt(from)
        list.add(to, dragged)
    }

    //display each input/output of this chain
    //starting at the given position
    //with an initial empty input
    fun display(position: Int, input: String = "") {
        //recursive loop
        fun loop(value: String, from: Locale, chain: List<Tool>) {
            //if not null do the let statement
            //test end of recursion
            chain.firstOrNull()?.let {
                onChangeListener?.invoke()

                it.run(value, from) { output ->
                    it.text = output
                    onChangeListener?.invoke()

                    //loop on the remaining chain
                    loop(output, it.language.toLocale(), chain.drop(1))
                }
            }
        }
        //start recursion
        var _input: String = input
        if (position != 0)
            _input = list[position].text
        loop(_input, Locale.getDefault(), list.drop(position))
    }
}