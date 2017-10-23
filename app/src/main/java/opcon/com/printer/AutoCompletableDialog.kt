package opcon.com.printer


import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView

/**
 * Created by aslitaskiran on 15/10/2017.
 */

class AutoCompletableDialog(c: Context, matter: String, callback: (str: String) -> Unit): AlertDialog(c) {

  var mBase: AutoCompletableBase
  var mRecyclerView: RecyclerView
  var mItems: ArrayList<String> = ArrayList()
  var mAdapter: ItemAdapter

  init {

    requestWindowFeature(Window.FEATURE_NO_TITLE)

    mBase = AutoCompletableBase(c)

    setOnDismissListener {
      mBase.close()
      mItems.clear()
    }

    val view = LayoutInflater.from(c).inflate(
        R.layout.auto_completable_dialog,
        null
    )

    setView(view)

    mItems = mBase.pullLast(matter)
    mAdapter = ItemAdapter(mItems, callback)

    mRecyclerView = view.findViewById(R.id.recyclerView)
    mRecyclerView.layoutManager = LinearLayoutManager(c)
    mRecyclerView.adapter = mAdapter
    setCancelable(true)

    setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), { _,_ ->
      dismiss()
    })

  }

  class ItemAdapter(val items: ArrayList<String>, var callback: (str: String) -> Unit): RecyclerView.Adapter<ItemAdapter.ItemHolder>() {

    override fun onBindViewHolder(holder: ItemHolder?, position: Int) {
      holder?.setName(items[position])
    }

    override fun getItemCount(): Int {
      return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemHolder {
      return ItemHolder(
          LayoutInflater.from(parent?.context).inflate(
              R.layout.row_item,
              parent,
              false
          ), callback
      )
    }

    class ItemHolder(v: View, var callback: (str: String) -> Unit): RecyclerView.ViewHolder(v) {
      var name: TextView = v.findViewById(R.id.name)
      fun setName(name: String) {
        this.name.text = name
        this.name.setOnClickListener { callback(this.name.text.toString()) }
      }
    }
  }

}