package ru.ja.memoty

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import ru.ja.memoty.db.dbi
import ru.ja.memoty.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.ArrayList

fun okCancelDialog(title: String, ctx : Context, okFn: DialogInterface.OnClickListener?,
                   cancelFn: DialogInterface.OnClickListener?) {
    val okText = "Да"
    val cancelText = "Нет"
    val nothingToDo = DialogInterface.OnClickListener(function = { dialogInterface: DialogInterface, i: Int -> })
    val dlg = AlertDialog.Builder(ctx).setMessage(title)
    if (okFn != null) {
        dlg.setPositiveButton(okText, okFn)
    } else {
        dlg.setPositiveButton(okText, nothingToDo)
    }
    if (cancelFn != null) {
        dlg.setNegativeButton(cancelText, cancelFn)
    } else {
        dlg.setNegativeButton(cancelText, nothingToDo)
    }
    dlg.create().show()
}

fun addTypedNames(names :ArrayList<String>, type: Int) {
    var expCount = -1
    var expDate = -1L
    when(type) {
        LIVE_40, PEACE_40 -> expCount = 40
        LIVE_HALFYEAR, PEACE_HALFYEAR -> expDate = LocalDateTime.now(ZoneOffset.UTC)
            .plusDays(183).toEpochSecond(ZoneOffset.UTC)
        LIVE_YEAR, PEACE_YEAR -> expDate = LocalDateTime.now(ZoneOffset.UTC)
            .plusDays(365).toEpochSecond(ZoneOffset.UTC)
    }
    for (name in names) {
        dbi?.memoryAddName(MemoryName(-1, name, type, remainCount = expCount, expDate = expDate))
    }
}

val CAPTIONS = mapOf(
    LIVE_40 to "О здравии сорокауст",
    PEACE_40 to "Об упокоении сорокауст",
    LIVE_HALFYEAR to "О здравии полугодовое",
    PEACE_HALFYEAR to "Об упокоении полугодовое",
    LIVE_YEAR to "О здравии годовое",
    PEACE_YEAR to "Об упокоении годовое"
)