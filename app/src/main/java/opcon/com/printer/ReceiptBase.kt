package opcon.com.printer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class Receipt(
    var _id: Int,
    var user_id: Int,
    var timestamp: Long,
    var customerName: String,
    var customerPhone: String
) {

  var data = JSONObject()

  fun getAddress(): String? {
    return data.getString("address")
  }
  fun setAddress(address: String) {
    data.put("address", address)
  }

  fun getStaff(): String{
    return data.getString("staff")
  }
  fun setStaff(staff: String) {
    data.put("staff", staff)
  }

  fun getNote(): String{
    return data.getString("note")
  }
  fun setNote(note: String) {
    data.put("note", note)
  }

  fun getTask(): String{
    return data.getString("task")
  }
  fun setTask(task: String) {
    data.put("task", task)
  }

  fun getAmount(): Double{
    return data.getDouble("amount")
  }
  fun setAmount(amount: Double) {
    data.put("amount", amount)
  }

  fun getDevice(): String {
    return data.getString("device")
  }
  fun setDevice(device: String) {
    data.put("device", device)
  }

  fun getTimeAsText(): String {
    return SimpleDateFormat("dd.MM.yyyy").format(timestamp)
  }

  fun getFullTimeAsText(): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm").format(timestamp)
  }

  fun hasNote(): Boolean{
    return data.has("note")
  }

}

data class User(var _id: Int, var name: String, var phone: String)

class ReceiptBase(c: Context):
        SQLiteOpenHelper(c, NAME, null, VERSION) {

  companion object {
    val VERSION = 7
    val NAME = "receipts.db"
  }

  var db: SQLiteDatabase? = null
  var counter: Int = 0

  fun openConnection(): SQLiteDatabase? {
    ++counter
    logi(TAG(), "openConnection", counter)
    if (counter == 1) {
      logi(TAG(), "openConnection", "success")
      db = writableDatabase
    }
    return db
  }

  fun closeConnection() {
    --counter
    logi(TAG(), "closeConnection", counter)
    if (counter == 0){
      logi(TAG(), "closeConnection", "success")
      db?.close()
      db = null
    }
  }

  override fun onCreate(db: SQLiteDatabase?) {
    db?.execSQL("create table users (_id integer primary key not null,name varchar(50) not null,phone varchar(40) not null);")

    db?.execSQL("create table receipts (_id integer primary key not null, customer_name varchar(50) not null, customer_phone varchar(25) not null,timestamp integer not null,user_id integer not null,data integer not null);")
  }

  override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    db?.execSQL("""drop table if exists users;""")
    db?.execSQL("""drop table if exists receipts;""")
    onCreate(db)
  }

  enum class SearchResult {
    with_customer_name,
    with_customer_phone,
    with_date,
    with_month,
    with_month_and_year,

    invalid_format
  }

  fun emptySearchResult():
      Pair<SearchResult, ArrayList<Receipt>> {
    return Pair(SearchResult.invalid_format, ArrayList())
  }

  fun searchInReceipts(text: String): Pair<SearchResult, ArrayList<Receipt>> {

    val list = ArrayList<Receipt>()
    val sr: SearchResult

    // accept month: 03 01

    if (text.length == 2 && text.toIntOrNull() != null) {
      sr = SearchResult.with_month
    } else if (text.matches(Regex("([0-9]{1,2})[./-]20[0-9]{2}"))) {
      sr = SearchResult.with_month_and_year
    } else if (text.matches(Regex("([0-9]{1,2})[./-]([0-9]{1,2})[./-](20[0-9]{2})"))) {
      sr = SearchResult.with_date
    } else if (text.matches(Regex("([0-9]{6,})"))) {
      sr = SearchResult.with_customer_phone
    } else {
      sr = SearchResult.with_customer_name
    }

    var from: Long = 0
    var to: Long = 0
    val now = Calendar.getInstance()

    if (sr == SearchResult.with_date) {
      val re = Regex("([0-9]{1,2})[./-]([0-9]{1,2})[./-](20[0-9]{2})")
      val find = re.find(text)

      if (find != null) {
        val day = find.groups[1]?.value
            ?:return emptySearchResult()
        val month = find.groups[2]?.value
            ?:return emptySearchResult()
        val year = find.groups[3]?.value
            ?:return emptySearchResult()

        logi(TAG(), day, month, year)

        val calendar = newCalendar(
            day = day.toInt(),
            month = month.toInt(),
            year = year.toInt(),
            hour = 0,
            minute = 0
        )

        from = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        to = calendar.timeInMillis
      } else{
        return emptySearchResult()
      }

    }else if (sr == SearchResult.with_month_and_year) {

      val rre = Regex("([0-9]{1,2})[./-](20[0-9]{2})")
      val ffind = rre.find(text)
          ?: return emptySearchResult()

      val month = ffind.groups[1]?.value
          ?:return emptySearchResult()
      val year = ffind.groups[2]?.value
          ?:return emptySearchResult()

      val calendar = newCalendar(
          month = month.toInt(),
          year = year.toInt(),
          day = 1,
          hour = 24,
          minute = 1
      )

      from = calendar.timeInMillis
      calendar.add(Calendar.MONTH, 1)
      to = calendar.timeInMillis

    } else if (sr == SearchResult.with_month) {
      val month = text.toInt()
      logi(TAG(), "month", month)
      val calendar = newCalendar (
          month = month,
          year = now.get(Calendar.YEAR),
          day = 1,
          hour = 24,
          minute = 1
      )
      from = calendar.timeInMillis
      calendar.add(Calendar.MONTH, 1)
      to = calendar.timeInMillis
    }

    val timely = sr == SearchResult.with_month ||
        sr == SearchResult.with_month_and_year ||
        sr == SearchResult.with_date

    logi(TAG(), "from", from, "to", to, to - from)

    val sql = "select * from receipts where ${if (timely) "timestamp >= $from and timestamp <= $to" else if (sr == SearchResult.with_customer_phone) "customer_phone like '%$text%'" else "customer_name like '%$text%'"}"

    // wow!


    val query = openConnection()?.rawQuery(sql, null)

    logi(TAG(), "searchInReceipts", "sql:", sql, sr, "count:" + query?.count)
    while (query != null && query.moveToNext()) {
      /* i don't remember names :)
      (_id integer primary key not null, customer_name varchar(50) not null, customer_phone varchar(25) not null,timestamp integer not null,user_id integer not null,amount integer not null,data integer not null)
       */

      val customerName = query.getString(
          query.getColumnIndex("customer_name")
      )

      val customerPhone = query.getString(
          query.getColumnIndex("customer_phone")
      )

      val _id = query.getInt(
          query.getColumnIndex("_id")
      )

      val timestamp = query.getLong(
          query.getColumnIndex("timestamp")
      )

      val user_id = query.getInt(
          query.getColumnIndex("user_id")
      )


      val data = query.getString(
          query.getColumnIndex("data")
      )

      val element = Receipt(
          _id,
          user_id,
          timestamp,
          customerName,
          customerPhone
      )

      element.data = JSONObject(data)

      list.add(
          element
      )

    }

    query?.close()
    closeConnection()
    return Pair(sr, list)
  }

  fun deleteReceipt(index: Int) {
    val delete = openConnection()?.delete(
        "receipts",
        "_id=?",
        arrayOf(index.toString())
    )
    logi(TAG(), "delete?", delete?:0)
    closeConnection()
  }

  fun addReceipt(receipt: Receipt): Int {

    val user_id = receipt.user_id

    val values = ContentValues()
    values.put("timestamp", receipt.timestamp)
    values.put("user_id", user_id)
    values.put("data", receipt.data.toString())
    values.put("customer_name", receipt.customerName)
    values.put("customer_phone", receipt.customerPhone)


    val insert = openConnection()?.insert(
        "receipts",
        null,
        values
    )

    logi(TAG(), "insert?", insert?:0, receipt.timestamp)

    closeConnection()
    return insert?.toInt()?:0
  }

  fun searchInUsers(text: String): Pair<Boolean, ArrayList<User>> {

    synchronized(ReceiptBase::class.java, {

      logi(TAG(), "search", text)
      val withPhone = (text.toIntOrNull() != null) ||
          (text.startsWith("+") && text.substring(1).toIntOrNull() != null)
      val sql = "select * from users where ${if (withPhone) "phone" else "name"}" +
          " like '%$text%'"
      logi(TAG(), "search", "sql", sql)
      val query = openConnection()?.rawQuery(sql, null)
      logi(TAG(), "search", "connection opened")
      val list = ArrayList<User>()

      while (query != null && query.moveToNext()) {
        logi(TAG(), "search", "next")
        val name = query.getString(query.getColumnIndex("name"))
        logi(TAG(), "search", name)
        val phone = query.getString(query.getColumnIndex("phone"))
        val _id = query.getInt(query.getColumnIndex("_id"))
        list.add(User(_id, name, phone))
      }

      logi(TAG(), "search", "searched")

      query?.close()
      logi(TAG(), "search","connection will closed")
      closeConnection()
      return Pair(withPhone, list)

    })

  }

  fun getUserFromName(name: String): User? {
    val query = openConnection()?.query(
        "users",
        null,
        "name=?",
        arrayOf(name),
        null,
        null,
        null
    )

    var user: User? = null

    if (query != null && query.moveToFirst()) {
      val _id = query.getInt(query.getColumnIndex("_id"))
      val phone = query.getString(query.getColumnIndex("phone"))
      user = User(_id, name, phone)
    }

    query?.close()
    closeConnection()
    return user
  }

  fun putUser(name: String, phone: String): User? {
    val user: User?
    val values = ContentValues()
    values.put("name", name)
    values.put("phone", phone)

    val connection = openConnection()
    logi(TAG(), "connection is null?", (connection == null).toString())
    val insert = connection?.insert(
        "users",
        null,
        values
    )

    if (insert == null || insert.toInt() == -1) {
      logi(TAG(), "insert is null")
      closeConnection()
      return null
    }

    user = User(insert.toInt(), name, phone)
    closeConnection()
    logi(TAG(), "putUser", name, phone, insert)
    return user
  }

}