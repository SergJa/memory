package ru.ja.memoty.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import ru.ja.memoty.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset

fun initMemoryDbManager(context: Context) {
    if (dbi != null) {
        return
    }
    dbi = MemoryDbManager(context)
}

var dbi: MemoryDbManager? = null

class MemoryDbManager(context: Context) {
    private val dbName = "MemoryDB"
    private val dbVersion = 1

    private val createQueries =  arrayOf(
        "CREATE TABLE IF NOT EXISTS Prefixes(prefix TEXT PRIMARY KEY)",
        "CREATE TABLE IF NOT EXISTS Dictionary(name TEXT PRIMARY KEY)",

        // fields:
        // type: look in model/Constants.kt
        // remainCount - remained remembrance count
        // expDate - date after remembrance stops; UnixTime is liked :)
        "CREATE TABLE IF NOT EXISTS Memory(name TEXT, type INTEGER, remainCount INTEGER, expDate INTEGER)",
        "CREATE INDEX IF NOT EXISTS typeIndex ON Memory(type)"
        )

    private val defaultPrefixes = arrayOf (
        "",
        "мл.",
        "отр.",
        "бол.",
        "непр.",
        "прот.",
        "свящ.",
        "иером.",
        "иг.",
        "архим.",
        "еп.",
        "архиеп.",
        "митр.",
        "патр.",
        "мон.",
        "ин.",
        "посл.",
        "воина",
        "новопр.",
        "уб."
    )

    private var db: SQLiteDatabase? = null

    init {
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
    }

    fun memoryAddName(name: MemoryName) {
        db!!.execSQL("INSERT INTO Memory VALUES(?,?,?,?)", arrayOf(name.name, name.type, name.remainCount, name.expDate))
    }

    fun memoryUpdateName(id: Int, name: String) {
        db!!.execSQL("update Memory set name=? where rowid=?", arrayOf(name, id))
    }

    fun memoryDeleteName(id: Int) {
        db!!.execSQL("DELETE FROM Memory where rowid=?", arrayOf(id))
    }

    fun memoryGetAll(type: Int?): ArrayList<MemoryName> {
        val cursor : Cursor = if (type == null) {
            db!!.rawQuery("SELECT rowid as id, * FROM Memory", arrayOf())
        } else {
            db!!.rawQuery("SELECT rowid as id, * FROM Memory WHERE type = ?", arrayOf(type.toString()))
        }

        val ret = ArrayList<MemoryName>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex("id"))
                    val name = cursor.getString(cursor.getColumnIndex("name"))
                    val count = cursor.getInt(cursor.getColumnIndex("remainCount"))
                    val type = cursor.getInt(cursor.getColumnIndex("type"))
                    val expDate = cursor.getLong(cursor.getColumnIndex("expDate"))
                    ret.add(MemoryName(id, name, type, count, expDate))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return ret
    }


    fun dictionaryAddName(name: String) {
        db!!.execSQL("INSERT INTO Dictionary VALUES(?)", arrayOf(name))
    }

    fun dictionaryDeleteName(id: Int) {
        db!!.execSQL("DELETE FROM Dictionary where rowid=?", arrayOf(id))
    }

    fun prefixGetAll(): ArrayList<String> {
        val cursor = db!!.rawQuery("SELECT prefix FROM Prefixes", arrayOf())

        val ret = ArrayList<String>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val prefix = cursor.getString(cursor.getColumnIndex("prefix"))
                    ret.add(prefix)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return ret
    }


    fun dictionaryGetAll(): ArrayList<DictionaryName> {
        val cursor = db!!.rawQuery("SELECT rowid as id, name FROM Dictionary", arrayOf())

        val ret = ArrayList<DictionaryName>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex("id"))
                    val name = cursor.getString(cursor.getColumnIndex("name"))
                    ret.add(DictionaryName(id, name))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return ret
    }

    fun dictionaryContains(name: String): Boolean {
        val cursor = db!!.rawQuery("SELECT * FROM Dictionary WHERE UPPER(name) like ?", arrayOf(name.toUpperCase()))
        var ret = false
        if (cursor != null) {
            ret = cursor.moveToFirst()
            cursor.close()
        }
        return ret
    }

    fun updateMemoriesCount() {
        db!!.execSQL("UPDATE Memory SET remainCount=remainCount-1 WHERE type in ($LIVE_40, $PEACE_40)")
        db!!.execSQL("DELETE FROM Memory WHERE remainCount < 0 AND type in ($LIVE_40, $PEACE_40)")
    }

    fun updateMemoriesTime() {
        val now = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)
        db!!.execSQL("DELETE FROM Memory WHERE expDate < $now AND type in ($LIVE_YEAR,$LIVE_HALFYEAR, $PEACE_YEAR, $PEACE_HALFYEAR)")
        db!!.execSQL("DELETE FROM Memory WHERE name = ''")
    }

    inner class DatabaseHelper(context: Context) :
        SQLiteOpenHelper(context, dbName, null, dbVersion) {

        var context: Context? = context

        override fun onCreate(db: SQLiteDatabase?) {
            for(query in createQueries) {
                db!!.execSQL(query)
            }
            initPrefixes(db)
            Log.i("db", "database created")
        }

        private fun initPrefixes(db: SQLiteDatabase?) {
            for(prefix in defaultPrefixes) {
                db!!.execSQL("INSERT INTO Prefixes VALUES(?)", arrayOf(prefix))
            }
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            Log.w("db", "Called onUpgrade, but nothing to do")
        }
    }
}