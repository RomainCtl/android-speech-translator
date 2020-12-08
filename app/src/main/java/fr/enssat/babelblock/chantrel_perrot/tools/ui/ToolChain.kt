package fr.enssat.babelblock.chantrel_perrot.tools.ui

import fr.enssat.babelblock.chantrel_perrot.tools.Language
import java.util.*


interface Tool {
    fun run(input: String, from: Locale, callback: (String) -> Unit)
    fun close()
}

interface ToolDisplay {
    var language: Language
    val tool: Tool
    val title: String
    var output: String
}

class ToolChain(list: List<ToolDisplay> = emptyList()) {

    private val list: MutableList<ToolDisplay> = list.toMutableList()
    val size
        get() = list.size

    private var onChangeListener: (() -> Unit)? = null

    //callback to invoke void method on toolchain Changes
    //see init of ToolChainAdapter
    fun setOnChangeListener(callback: () -> Unit) {
        onChangeListener = callback
    }

    fun add(tool: ToolDisplay) {
        list.add(tool)
        onChangeListener?.invoke()
    }

    fun get(index: Int) = list.get(index)

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
        fun loop(value: String, from: Locale, chain: List<ToolDisplay>) {
            //if not null do the let statement
            //test end of recursion
            chain.firstOrNull()?.let {
                onChangeListener?.invoke()

                it.tool.run(value, from) { output ->
                    it.output = output
                    onChangeListener?.invoke()

                    //loop on the remaining chain
                    loop(output, it.language.toLocale(), chain.drop(1))
                }
            }
        }
        //start recursion
        var _input: String = input
        if (position != 0)
            _input = list.get(position).output
        loop(_input, Locale.getDefault(), list.drop(position))
    }
}
