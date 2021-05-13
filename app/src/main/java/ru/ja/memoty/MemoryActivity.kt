package ru.ja.memoty

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_memory.*
import ru.ja.memoty.db.dbi
import ru.ja.memoty.model.*
import java.time.Duration
import java.time.LocalTime
import java.util.ArrayDeque
import kotlin.collections.*


class MemoryActivity : AppCompatActivity() {

    private var memoryNames: ArrayList<MemoryName> = arrayListOf()
    private val memoryNameAdapter = MemoryNameAdapter(::nextClick, ::renameDialog)
    private lateinit var namesGrid : RecyclerView
    private lateinit var layoutManager : GridLayoutManager
    private var memoryType : Int = -1

    private lateinit var currentActivity:MemoryActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        currentActivity = this

        dbi?.updateMemoriesTime()

        preInitData()
        initGrid()
        initButtons()
        runNextMemoryType()
    }

    private var prevNextClickTime  = LocalTime.now()

    private fun initButtons() {
        button_memory_next.setOnClickListener { nextClick() }

        button_memory_exit.setOnClickListener {
            okCancelDialog("Выйти из поминовения?",
                this,
                DialogInterface.OnClickListener(
                    function = { dialogInterface: DialogInterface, i: Int ->
                        currentActivity.finish()
                    }
                ),
                null)
        }
    }

    private fun nextClick() {
        // предотвращение случайного двойного клика
        val now = LocalTime.now()
        if (now.minus(Duration.ofSeconds(3)).isBefore(prevNextClickTime)) {
            Toast.makeText(currentActivity, "Слишком быстрый клик", Toast.LENGTH_SHORT).show()
            return
        }
        prevNextClickTime = now
        var pos = layoutManager.findLastCompletelyVisibleItemPosition() + 3
        if (pos >= memoryNameAdapter.values.size)
            pos = memoryNameAdapter.values.size-1
        if (memoryNameAdapter.values[pos].id == -1) {
            runNextMemoryType()
            return
        }
        layoutManager.scrollToPositionWithOffset(pos, 0)
    }

    private fun runNextMemoryType() {
        initData()
        text_memory_caption.text = CAPTIONS[memoryType]
        memoryNameAdapter.notifyDataSetChanged()
        layoutManager.scrollToPositionWithOffset(0,0)
    }

    private fun initGrid() {
        namesGrid = findViewById(R.id.memory_grid)
        layoutManager = GridLayoutManager(this, 3)
        namesGrid.layoutManager = layoutManager
        namesGrid.adapter = memoryNameAdapter
    }

    private val memoryQueue = ArrayDeque<Int>()

    private fun preInitData() {
        for(i in 0..5) {
            if(intent.extras.getBoolean(i.toString(), false))
                memoryQueue.push(i)
        }
    }

    private fun initData() {
        if(memoryQueue.isEmpty()) {
            finishIt()
            return
        }
        memoryType = memoryQueue.pollLast()
        memoryNames = dbi?.memoryGetAll(memoryType)!!
        // чтобы был "хвост" из пустых имён, чтобы на последнкй странице не повторялись имена с предпоследней
        val nameNil = MemoryName(-1, "",-1,-1,-1)
        for(i in 1..100)
            memoryNames.add(nameNil)

        memoryNameAdapter.values = memoryNames
    }

    private fun finishIt() {
        Toast.makeText(this, "Сохраняются изменения", Toast.LENGTH_LONG).show()
        dbi?.updateMemoriesCount()
        finish()
    }

    private fun renameDialog(position: Int) {
        if (memoryNameAdapter.values[position].id == -1)
            return

        val li = LayoutInflater.from(this)
        val promptsView: View = li.inflate(R.layout.fragment_enter_text, null)

        val mDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)

        mDialogBuilder.setView(promptsView)

        val userInput = promptsView.findViewById<View>(R.id.input_text) as EditText

        userInput.setText(memoryNameAdapter.values[position].name)
        mDialogBuilder
            .setCancelable(false)
            .setPositiveButton("OK"
            ) { _, _ -> //Вводим текст и отображаем в строке ввода на основном экране:
                memoryNameAdapter.values[position].name = userInput.text.toString()
                memoryNameAdapter.notifyDataSetChanged()
                dbi?.memoryUpdateName(
                    memoryNameAdapter.values[position].id,
                    memoryNameAdapter.values[position].name
                )
            }
            .setNeutralButton("Удалить"
            ) { _, _ ->
                dbi?.memoryDeleteName(memoryNameAdapter.values[position].id)
                memoryNameAdapter.values.removeAt(position)
                memoryNameAdapter.notifyDataSetChanged()
            }
            .setNegativeButton("Отмена" ) { dialog, id -> dialog.cancel() }

        val alertDialog: AlertDialog = mDialogBuilder.create()

        alertDialog.show()
    }

    class MemoryNameAdapter(val nextClick: () -> Unit, val renameDlg: (Int) -> Unit) : RecyclerView.Adapter<TextHolder>() {
        var values: ArrayList<MemoryName> = arrayListOf()
        override fun getItemCount(): Int {
            return values.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextHolder {
            val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.textitem, parent, false)
            return TextHolder(viewHolder)
        }

        override fun onBindViewHolder(holder: TextHolder, position: Int) {
            if (values.size <= position) // проверка на всякий случай
                return
            val txt = values[position]
            holder.textView.text = txt.name
            holder.textView.setOnClickListener{
                nextClick()
            }
            holder.textView.setOnLongClickListener {
                renameDlg(position)
                true
            }
        }
    }

}

