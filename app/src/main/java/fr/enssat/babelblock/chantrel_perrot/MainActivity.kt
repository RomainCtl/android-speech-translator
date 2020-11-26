package fr.enssat.babelblock.chantrel_perrot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.enssat.babelblock.chantrel_perrot.tools.ui.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //create toolchain and its adapter
        val toolChain = ToolChain()
        val adpater = ToolChainAdapter(toolChain)

        //dedicated drag and drop mover helper
        val moveHelper = ToolChainMoveHelper.create(adpater)
        moveHelper.attachToRecyclerView(tool_chain_list)

        //see tool_chain_list in activity_tool_chain.xml
        //chain of tools
        tool_chain_list.adapter = adpater

        //see tool_list in activity_tool_chain.xml
        tool_list.adapter = ToolListAdapter(toolChain)
    }
}