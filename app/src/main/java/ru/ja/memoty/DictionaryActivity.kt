package ru.ja.memoty

import android.content.Context
import android.content.DialogInterface
import android.database.SQLException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.ja.memoty.db.dbi
import ru.ja.memoty.model.DictionaryName

class DictionaryActivity : AppCompatActivity() {

    private var dictionaryAdapter:DictionaryAdapter? = null
    private var dictionaryNames: ArrayList<DictionaryName>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dictionaryActivityInstance = this
        setContentView(R.layout.activity_dictionary)

        val nameListView: RecyclerView = findViewById(R.id.nameList)
        nameListView.layoutManager = LinearLayoutManager(this)

        dictionaryNames = dbi?.dictionaryGetAll()
        dictionaryAdapter = DictionaryAdapter(dictionaryNames)
        nameListView.adapter = dictionaryAdapter

        val nameEdit: TextView = findViewById(R.id.nameEdit)
        nameEdit.text = ""
        nameEdit.requestFocus()
    }

    fun onClickAddName(view: View) {
        val nameEdit: TextView = findViewById(R.id.nameEdit)
        val name = nameEdit.text.toString()
        Toast.makeText(this, name, Toast.LENGTH_LONG).show()
        try {
            dbi?.dictionaryAddName(name)
        } catch(e: SQLException) {
            Toast.makeText(this, "Такое имя уже существует!\n" + e.message, Toast.LENGTH_SHORT).show()
            return
        }
        dictionaryNames?.add(DictionaryName(dictionaryNames!!.size, name))
        nameEdit.text = ""
        dictionaryAdapter?.notifyDataSetChanged()
    }
}

var dictionaryActivityInstance: Context? = null

fun getActivity(): Context {
    return dictionaryActivityInstance!!
}

class DictionaryAdapter(private val values: ArrayList<DictionaryName>?): RecyclerView.Adapter<TextHolder>() {
    override fun getItemCount(): Int {
        return values?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextHolder {
        return TextHolder(LayoutInflater.from(parent.context).
            inflate(R.layout.textitem, parent, false))
    }

    override fun onBindViewHolder(holder: TextHolder, position: Int) {
        if (values == null || values.size <= position) // проверка на всякий случай
            return
        val id = values[position].id!!
        val currentName = values[position].name
        holder.textView.text = currentName
        holder.textView.setOnClickListener {
            val builder = AlertDialog.Builder(getActivity())
            val dlg = builder.setTitle("Удалить имя").setMessage("Удалить имя '$currentName'?")
                .setCancelable(true)
                .setPositiveButton("Да") { _, _ ->
                    deleteName(id, position)
                }.setNegativeButton("Отмена", null).create()
            dlg.show()
        }
    }

    private fun deleteName(id: Int, pos: Int) {
        try{
            dbi?.dictionaryDeleteName(id)
        } catch(e: SQLException) {
            Toast.makeText(getActivity(), "Ошибка удаления имени\n" + e.message, Toast.LENGTH_SHORT).show()
            return
        }
        values?.removeAt(pos)
        this.notifyDataSetChanged()
    }
}

class TextHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var textView: TextView = itemView.findViewById(R.id.recyclerTextHolder) as TextView
}