package fr.enssat.babelblock.chantrel_perrot.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_tool_chain.view.*
import fr.enssat.babelblock.chantrel_perrot.R
import fr.enssat.babelblock.chantrel_perrot.ui.ItemMoveAdapter
import fr.enssat.babelblock.chantrel_perrot.ui.ToolChain

class ToolChainAdapter(private val toolChain: ToolChain) : RecyclerView.Adapter<ToolChainAdapter.ToolViewHolder>(),
    ItemMoveAdapter {

    init {
        //notifyDataSetChanged() = redraw, the data set has changed
        toolChain.setOnChangeListener { notifyDataSetChanged() }
    }

    override fun getItemCount(): Int = toolChain.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tool_chain, parent, false)
        return ToolViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToolViewHolder, position: Int) {
        holder.bind(toolChain, position)
    }

    override fun onRowMoved(from: Int, to: Int) {
        toolChain.move(from, to)
        notifyItemMoved(from, to)
    }

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.GRAY)
    }

    override fun onRowReleased(viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
    }

    //viewholder, kind of reusable view cache,  for each tool in the chain
    class ToolViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(toolChain: ToolChain, i: Int) {
            val tool = toolChain.get(i)
            itemView.text_output.text = tool.output
            itemView.text_box.text = tool.title
            itemView.text_box.setOnClickListener {
                toolChain.display(i)
            }
        }
    }
}
