package fr.enssat.babelblock.chantrel_perrot.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.enssat.babelblock.chantrel_perrot.R
import fr.enssat.babelblock.chantrel_perrot.model.Tool
import fr.enssat.babelblock.chantrel_perrot.ui.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.list_item_tool.view.*

class ToolListAdapter(private val model: MainActivityViewModel) : RecyclerView.Adapter<ToolListAdapter.ToolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.list_item_tool, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return model.availableLanguages.size
    }

    //viewholder, kind of reusable view cache,  for each tool in the chain
    inner class ToolViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(model: MainActivityViewModel, position: Int) {
            val language = model.availableLanguages[position]
            itemView.tool_item.text = language.toString()
            itemView.tool_item.setOnClickListener {
                model.add(
                    Tool(language, "Translate to:\n$language", "")
                )
            }
        }
    }
}