package fr.enssat.babelblock.chantrel_perrot.ui.adapter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.nl.translate.TranslateLanguage
import fr.enssat.babelblock.chantrel_perrot.R
import fr.enssat.babelblock.chantrel_perrot.model.Language
import fr.enssat.babelblock.chantrel_perrot.tools.TranslationTool
import fr.enssat.babelblock.chantrel_perrot.ui.Tool
import fr.enssat.babelblock.chantrel_perrot.ui.ToolChain
import fr.enssat.babelblock.chantrel_perrot.ui.ToolDisplay
import kotlinx.android.synthetic.main.list_item_tool.view.*
import java.util.*

class ToolListAdapter(val toolChain: ToolChain, val translator: TranslationTool) : RecyclerView.Adapter<ToolListAdapter.ToolViewHolder>() {

    var availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
        .map {
            Language(it)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_tool, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(toolChain, position)
    }

    override fun getItemCount(): Int {
        return availableLanguages.size
    }

    private val handler = Handler(Looper.getMainLooper())

    private fun getTool(language: Language) =
        object : ToolDisplay {
            override var language = language
            override var title  = "Translate to:\n$language"
            override var output = ""
            override val tool   = object : Tool {
                //override run method of Tool interface
                override fun run(input: String, from: Locale, output: (String) -> Unit) {
                    handler.postDelayed(
                        {
                            output("translation in progress...")
                            translator.translate(input, from, language.toLocale()) { text ->
                                output("$text")
                            }
                        },1000)
                }
                override fun close() {
                    Log.d(title, "close")
                }
            }
        }

    //viewholder, kind of reusable view cache,  for each tool in the chain
    inner class ToolViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(toolChain: ToolChain, position: Int) {
            val language = availableLanguages.get(position)
            itemView.tool_item.text = language.toString()
            itemView.tool_item.setOnClickListener {
                toolChain.add(getTool(language))
            }
        }
    }
}