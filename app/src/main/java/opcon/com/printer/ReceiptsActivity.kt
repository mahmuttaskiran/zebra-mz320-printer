package opcon.com.printer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.TextView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.context.IconicsContextWrapper
import kotlinx.android.synthetic.main.activity_receipts.*
import kotlinx.android.synthetic.main.content_receipts.*
import java.text.SimpleDateFormat
import java.util.*

data class Totals(var amount: String, var tasks: Int)

class ReceiptsActivity : AppCompatActivity() {

  companion object {

    var eventManager: EventManager? = null

    fun go(ac: Activity) {
      ac.startActivity(
          Intent(
              ac,
              ReceiptsActivity::class.java
          )
      )
    }
  }

  lateinit var mAdapter: ReceiptsAdapter

  var miSearch: MenuItem? = null
  var miClose: MenuItem? = null

  val mReceipts = ArrayList<Receipt?>()


  var mSearchLock = Any()
  lateinit var mReceiptBase: ReceiptBase

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    eventManager = EventManager()

    eventManager?.on("onDelete", 0, { id ->

      var findIndex = -1
      mReceipts.forEachIndexed { index, it ->
        if (it != null && id == it._id) {
          findIndex = index
        }
      }

      if (findIndex != -1) {
        AlertDialog.Builder(this)
            .setTitle(R.string.are_u_sure)
            .setMessage(R.string.are_u_sure_to_delete)
            .setPositiveButton(getString(R.string.delete).toUpperCase(), { _,_ ->
              mReceipts.removeAt(findIndex)

              val receiptBase = ReceiptBase(baseContext)
              receiptBase.deleteReceipt(id)
              receiptBase.close()

              mAdapter.notifyDataSetChanged()
            })
            .setNegativeButton(R.string.cancel, null)
            .show()
      }


    })

    eventManager?.on("onReprint", 0, { id ->
      mReceipts.forEach {
        if (it != null && id == it._id) {
          PrintUtils.print(it, this)
        }
      }
    })

    setContentView(R.layout.activity_receipts)

    mReceiptBase = ReceiptBase(baseContext)

    mAdapter = ReceiptsAdapter(mReceipts)
    receiptsRecycler.layoutManager = LinearLayoutManager(this)
    receiptsRecycler.adapter = mAdapter
    // init default this date.

    search_receipt.afterTextChanged {
      searchAsync(it)
    }

    search_receipt.setText(SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()))

    val icon = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_keyboard_backspace)
        .color(Color.WHITE)
        .sizeDp(18)

    title = ""

    setSupportActionBar(toolbar1)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeAsUpIndicator(icon)
    supportActionBar?.elevation = 5.toFloat()

  }

  override fun onDestroy() {
    super.onDestroy()
    mReceiptBase.close()
    eventManager?.release()
    eventManager = null
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val search = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_search)
        .color(Color.WHITE)
        .sizeDp(18)
    val close = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_close)
        .color(Color.WHITE)
        .sizeDp(18)
    miSearch = menu?.add(0, 0, 0, getString(R.string.date_or_name))
    miSearch?.icon = search
    miSearch?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    miSearch?.isVisible = false
    miClose = menu?.add(0, 1, 1, getString(R.string.delete))
    miClose?.icon = close
    miClose?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    return super.onCreateOptionsMenu(menu)
  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == android.R.id.home) {
      onBackPressed()
    } else if (item?.itemId == 0) { }
    else {
      search_receipt.setText("")
    }
    return super.onOptionsItemSelected(item)
  }

  fun searchAsync(text: String) {
    if (text.isEmpty()) {
      miSearch?.isVisible = true
      miClose?.isVisible = false
    } else {
      miSearch?.isVisible = false
      miClose?.isVisible = true
      synchronized(mSearchLock, {
        val result = mReceiptBase.searchInReceipts(text)
        synchronized(mSearchLock, {
          runOnUiThread {
            mReceipts.clear()
            mReceipts.addAll(result.second)
            if (result.first != ReceiptBase.SearchResult.invalid_format &&
                result.first != ReceiptBase.SearchResult.with_customer_phone &&
                result.first != ReceiptBase.SearchResult.with_customer_name &&
                mReceipts.size > 0) {
              val amount = 0.toDouble() + mReceipts
                  .asSequence()
                  .filterNotNull()
                  .sumByDouble { it.getAmount() }
              mAdapter.totals = Totals(amount.toString(), result.second.size)

              mReceipts.add(0, null)

            } else {
              mAdapter.totals = null
            }
            if (receiptsRecycler != null) {
              mAdapter.notifyDataSetChanged()
            }
          }
        })
      })
    }
  }



  class ReceiptsAdapter(var receipts: ArrayList<Receipt?>):
      RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var totals: Totals? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {


      if (holder is TotalsHolder) {
        holder.forTotal(totals!!)
      } else (holder as? ReceiptHolder)?.forReceipt(receipts[position]!!)


    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
      val inflater = LayoutInflater.from(parent?.context)
      return if (viewType == 0) {
        TotalsHolder (
            inflater.inflate(
            R.layout.row_totals,
            parent,
            false
          )
        )
      } else {
        ReceiptHolder (
            inflater.inflate(
                R.layout.row_receipt,
                parent,
                false
            )
        )
      }
    }

    override fun getItemCount(): Int {
      return if (totals == null) receipts.size else receipts.size
    }

    override fun getItemViewType(position: Int): Int {
      return if (totals != null && position == 0) position else 1
    }

    class TotalsHolder(v: View): RecyclerView.ViewHolder(v) {
      var totalAmount: TextView = v.findViewById(R.id.totalAmount)
      var totalTask: TextView = v.findViewById(R.id.totalTask)

      fun forTotal(totals: Totals) {
        totalAmount.text = totals.amount
        totalTask.text = totals.tasks.toString()
      }
    }

    class ReceiptHolder(var v: View): RecyclerView.ViewHolder(v) {

      var name: TextView = v.findViewById(R.id.name)
      var number: TextView = v.findViewById(R.id.phone)
      var amount: TextView = v.findViewById(R.id.amount)
      var task: TextView = v.findViewById(R.id.task)
      var address: TextView = v.findViewById(R.id.address)
      var note: TextView = v.findViewById(R.id.note)
      var staff: TextView = v.findViewById(R.id.staff)
      var date: TextView = v.findViewById(R.id.date)
      var delete: Button = v.findViewById(R.id.delete)
      var reprint: Button = v.findViewById(R.id.reprint)

      fun forReceipt(receipt: Receipt) {
        name.text = receipt.customerName
        number.text = receipt.customerPhone
        amount.text = receipt.getAmount().toString() + " ₺"
        task.text = receipt.getTask()
        address.text = receipt.getAddress()

        reprint.setOnClickListener {
          eventManager?.dispatch("onReprint", receipt._id)
        }

        delete.setOnClickListener {
          eventManager?.dispatch("onDelete", receipt._id)
        }

        if (receipt.hasNote()) {
          note.visible()
          note.text = receipt.getNote()
        } else {
          v.findViewById<View>(R.id.addressIndicator).gone()
          note.gone()
        }
        staff.text = receipt.getStaff()
        date.text = receipt.getFullTimeAsText()
      }

    }

  }

}
