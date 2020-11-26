package fr.enssat.babelblock.chantrel_perrot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import fr.enssat.babelblock.chantrel_perrot.tools.BlockService
import fr.enssat.babelblock.chantrel_perrot.tools.TranslationTool
import kotlinx.android.synthetic.main.activity_translator.*
import java.util.*

class TranslatorActivity : AppCompatActivity() {

    lateinit var translator: TranslationTool

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translator)

        val service = BlockService(this)
        translator = service.translator()

        translate_button.setOnClickListener {
            translator.translate(edit_query.text.toString(), Locale.FRENCH, Locale.ENGLISH) { enText ->
                translated_text.text = enText
            }
        }
    }

    override fun onDestroy() {
        translator.close()
        super.onDestroy()
    }
}