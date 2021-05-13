package ru.ja.memoty

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_true_main.*
import ru.ja.memoty.db.dbi
import ru.ja.memoty.db.initMemoryDbManager
import ru.ja.memoty.model.*

class TrueMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMemoryDbManager(this)

        setContentView(R.layout.activity_true_main)

        initMemoryAll()
    }

//    private fun corr() {
//        val allNames = dbi?.memoryGetAll(null)
//        allNames?.sortBy { it.id }
//
//        var i = 0
//        while(i<allNames!!.size-5) {
//            val (pos, len) = getCopyPos(allNames, i, 5)
//            if(len > 0) {
//                val ids1 = allNames!![pos].id
//                val ide1 = allNames!![pos+len-1].id
//                println("$i: $pos, $len, $ids1, $ide1")
//                i += len
//            } else
//                i++
//        }
//        println("qwe")
//    }
//
//    private fun getCopyPos(allNames: ArrayList<MemoryName>?, start: Int, minCorLen:Int): Pair<Int,Int> {
//        var pos = 0
//        var len = 0
//        val stop = allNames!!.size-minCorLen
//        var iter = start+minCorLen
//        mainLoop@
//        while(start < stop) {
//            while((allNames[start].name != allNames[iter].name)) {
//                iter++
//                if(iter >= stop)
//                    break@mainLoop
//            }
//            pos = iter
//            innerLoop@
//            while((iter+len < allNames.size) && (allNames[start+len].name == allNames[iter+len].name)) {
//                len++
//            }
//            if(len >= minCorLen) {
//                break@mainLoop
//            }
//            iter++
//            pos = 0
//            len = 0
//        }
//        return Pair(pos, len)
//    }

    private fun initMemoryAll() {
        button_all_memory.setOnClickListener {
            val memoryActivity = Intent(this, MemoryActivity::class.java)
            memoryActivity.putExtra(LIVE_40.toString(), true)
            memoryActivity.putExtra(LIVE_YEAR.toString(), true)
            memoryActivity.putExtra(LIVE_HALFYEAR.toString(), true)
            memoryActivity.putExtra(PEACE_40.toString(), true)
            memoryActivity.putExtra(PEACE_YEAR.toString(), true)
            memoryActivity.putExtra(PEACE_HALFYEAR.toString(), true)

            startActivity(memoryActivity)
        }
    }

    fun onClickAddNameMain(view: View) {
        val nameType: Int = when (view.id) {
            R.id.btn_live_40 -> LIVE_40
            R.id.btn_peace_40 -> PEACE_40
            R.id.btn_live_half -> LIVE_HALFYEAR
            R.id.btn_peace_half -> PEACE_HALFYEAR
            R.id.btn_live_year -> LIVE_YEAR
            R.id.btn_peace_year -> PEACE_YEAR
            else -> {
                Toast.makeText(this, "Ошибка: неизвестный тип имени: " + view.id.toString(), Toast.LENGTH_SHORT).show()
                return
            }
        }
        val addNamesIntent = Intent(this, ActivityAdd::class.java)
        addNamesIntent.putExtra("nameType", nameType)
        startActivity(addNamesIntent)
    }

    fun onClickDictionary(view:View) {
        System.gc()
        val dictionaryIntent = Intent(this, DictionaryActivity::class.java)
        startActivity(dictionaryIntent)
    }
}