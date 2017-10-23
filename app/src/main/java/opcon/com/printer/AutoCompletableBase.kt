package opcon.com.printer

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by aslitaskiran on 14/10/2017.
 */

class AutoCompletableBase(c: Context):
    SQLiteOpenHelper(c, NAME, null, VERSION) {

  companion object {
    val VERSION = 3
    val NAME = "auto_completable_base.db"
  }

  var db: SQLiteDatabase? = null
  var counter: Int = 0

  fun openConnection(): SQLiteDatabase? {
    counter++
    if (counter == 1) {
      db = writableDatabase
    }
    return db
  }

  fun closeConnection() {
    counter--
    if (counter == 0){
      db?.close()
      db = null
    }
  }

  override fun onCreate(db: SQLiteDatabase?) {
    db?.execSQL("create table items (_id integer primary key not null,name varchar(250) not null,matter varchar(30) not null);")
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    db?.execSQL("drop table if items users")
    onCreate(db)
  }

  fun pushIsNotExists(matter: String, name: String): Int {

    if (isExists(matter, name)) {
      return 0
    }

    val values = ContentValues()
    values.put("matter", matter)
    values.put("name", name)

    val insert = openConnection()?.insert(
        "items",
        null,
        values
    )

    closeConnection()
    return insert?.toInt()?:0
  }

  fun isExists(matter: String, name: String): Boolean {
    val query = openConnection()?.query(
        "items",
        arrayOf("_id"),
        "matter=? and name=?",
        arrayOf(matter, name),
        null,
        null,
        null
    )

    val exists = query != null && query.moveToNext()

    query?.close()
    closeConnection()
    return exists
  }

  fun length(matter: String) : Int {
    val length = DatabaseUtils.queryNumEntries(
        openConnection(),
        "items",
        "matter=?",
        arrayOf(matter)
    )
    closeConnection()
    return length.toInt()
  }

  fun pullLast(matter: String): ArrayList<String> {


    val query = openConnection()?.query(
        "items",
        null,
        "matter=?",
        arrayOf(matter),
        null,
        null,
        null
    )

    val list = ArrayList<String>()

    if (query != null) {
      while (query.moveToNext()) {
        list.add(
          query.getString(
              query.getColumnIndex("name")
          )
        )
      }
    }

    query?.close()
    closeConnection()
    return list
  }

}