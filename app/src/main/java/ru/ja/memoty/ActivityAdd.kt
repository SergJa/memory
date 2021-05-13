package ru.ja.memoty

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_dictionary.*
import ru.ja.memoty.db.dbi
import ru.ja.memoty.model.*

class ActivityAdd : AppCompatActivity() {

    private var nameType: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        addActivityInstance = this

        // setting caption
        nameType = intent.extras.getInt("nameType", 0)
        val caption = findViewById<TextView>(R.id.add_name_caption)
        caption.text = CAPTIONS[nameType!!]

        initAutocompleteInput()

        // prefix initialization
        prefixField = findViewById(R.id.prefix)
        val prefixListView = findViewById<RecyclerView>(R.id.prefixes)
        prefixListView.layoutManager = GridLayoutManager(this, 2)
        prefixesArray = dbi?.prefixGetAll()
        val prefixAdapter = PrefixAdapter(prefixesArray, prefixField)
        prefixListView.adapter = prefixAdapter


        val namesList = findViewById<RecyclerView>(R.id.names_to_add)
        namesList.layoutManager = GridLayoutManager(this, 3)
        namesAdapter.values!!.clear()

        namesList.adapter = namesAdapter

        initClose()
        initAddNameButton()

        prefixListView.adapter!!.notifyDataSetChanged()

        nameInput?.requestFocus()
    }

    private fun initAutocompleteInput() {
        // initialization autocomplete by dictionary
        nameInput = findViewById(R.id.nameAutoComplete)
        val dictionaryNames = dbi?.dictionaryGetAll()
        val dictionaryNamesString = dictionaryNames!!.map { dictionaryName -> dictionaryName.name }
        val dictionaryAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, dictionaryNamesString)
        nameInput!!.setAdapter(dictionaryAdapter)
        nameInput!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, text, _, _ ->
                // обработчик клика на имя в автозаполнении
                val textStr = (text as TextView).text.toString()
                addNameAndReset(textStr)
            }
    }

    private fun initAddNameButton() {
        activity_add_button_add.setOnClickListener {
            var nameText = nameInput!!.text.toString()
            if (nameText == "")
                return@setOnClickListener

            //  делаем первую букву заглавной
            nameText = nameText.first().toUpperCase() + nameText.drop(1)
            if (!dbi!!.dictionaryContains(nameText)) {
                okCancelDialog("Имени $nameText нет в словаре. Добавить?",
                    getAddActivity(),
                    DialogInterface.OnClickListener(
                        function = { dialogInterface: DialogInterface, i: Int ->
                            dbi?.dictionaryAddName(nameText)
                            initAutocompleteInput()
                        }
                    ),
                    null)
            }
            addNameAndReset(nameText)
        }
    }

    // инициализация диалога закрытия
    private fun initClose() {
        buttonSave.setOnClickListener {
            okCancelDialog("Сохранить имена и выйти?",
                getAddActivity(),
                DialogInterface.OnClickListener(
                    function = { dialogInterface: DialogInterface, i: Int ->
                        addTypedNames(namesAdapter.values!!, nameType!!)
                        getAddActivity().finish()
                    }
                ),
                null)
        }

        buttonDiscard.setOnClickListener {
            okCancelDialog("Выйти без сохранения имён?",
                getAddActivity(),
                DialogInterface.OnClickListener(
                    function = { dialogInterface: DialogInterface, i: Int ->
                        getAddActivity().finish()
                    }
                ),
                null)
        }

    }

    private fun addNameAndReset(textStr: String) {
        namesAdapter.addName((prefixField!!.text.toString() + " " + textStr).trim())
        nameInput!!.text.clear()
        prefixField!!.text = ""
    }


    private var prefixesArray: ArrayList<String>? = null
    private var prefixField: TextView? = null
    private var addActivityInstance: AppCompatActivity? = null
    private val namesAdapter = AddNameAdapter(arrayListOf())
    private var nameInput: AutoCompleteTextView? = null

    private fun getAddActivity(): AppCompatActivity {
        return addActivityInstance!!
    }

    class PrefixAdapter(private val values: ArrayList<String>?, private var prefixField: TextView?) : RecyclerView.Adapter<PrefixTextHolder>() {
        override fun getItemCount(): Int {
            return if (values == null) {
                0
            } else {
                values.size
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrefixTextHolder {
            return PrefixTextHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.textitem_prefix, parent, false)
            )
        }

        private val emptyPrefix = "<нет>"
        override fun onBindViewHolder(holder: PrefixTextHolder, position: Int) {
            if (values == null || values.size <= position) // проверка на всякий случай
                return
            var prefix = values[position]
            if (prefix == "") {
                prefix = emptyPrefix
            }
            holder.textView.text = prefix
            holder.textView.setOnClickListener {
                if (prefix == emptyPrefix) {
                    prefixField?.text = ""
                } else {
                    prefixField?.text = prefixField?.text.toString() + " " + prefix
                }
            }
        }
    }

    class AddNameAdapter(val values: ArrayList<String>?) : RecyclerView.Adapter<TextHolder>() {
        override fun getItemCount(): Int {
            return if (values == null) {
                0
            } else {
                values.size
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextHolder {
            return TextHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.textitem, parent, false)
            )
        }

        override fun onBindViewHolder(holder: TextHolder, position: Int) {
            if (values == null || values.size <= position) // проверка на всякий случай
                return
            val txt = values[position]
            holder.textView.text = txt
            holder.textView.setOnClickListener(
                AddNameClickListener(position,
                    fn1 = {
                        values.removeAt(it)
                        notifyDataSetChanged()
                    })
            )
        }

        fun addName(name: String) {
            values!!.add(name)
            notifyDataSetChanged()
        }
    }

    class AddNameClickListener(private val valuePosition: Int, val fn1: (pos: Int) -> Unit) :
        View.OnClickListener {
        override fun onClick(p0: View?) {
            fn1(valuePosition)
        }

    }
}

class PrefixTextHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var textView: TextView = itemView.findViewById(R.id.text_item_prefix) as TextView
}

