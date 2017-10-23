package opcon.com.printer

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.Toast
import com.google.firebase.database.*
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.zebra.sdk.comm.BluetoothConnectionInsecure
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.*

class AddReceiptActivity : AppCompatActivity() {

  var mAutoCompletableBase: AutoCompletableBase? = null
  var mCustomerId: Int = 0
  var mCustomerName: String? = null
  var mCustomerPhone: String? = null

  lateinit var database: FirebaseDatabase
  lateinit var printlnRef: DatabaseReference
  var printMessage: String? = null

  companion object {
    fun go(ac: Activity) {
      ac.startActivity(
          Intent(
              ac,
              AddReceiptActivity::class.java
          )
      )
    }
  }

  var mDialog: AlertDialog? = null
  var mProgressDialog: ProgressDialog? = null
  override fun onResume() {
    super.onResume()
    if (BluetoothDevicesActivity.getDefaultBluetoothDevice() == null)
      BluetoothDevicesActivity.go(this)
  }

  val mPrintlnListener = object : ValueEventListener {
    override fun onCancelled(p0: DatabaseError?) {}
    override fun onDataChange(p0: DataSnapshot?) {
      printMessage = p0?.value?.toString()
      logi(TAG(), "printMessage is", printMessage?:"null")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_receipt)

    database = FirebaseDatabase.getInstance()
    printlnRef = database.getReference("println")
    printlnRef.keepSynced(true)

    printlnRef.addListenerForSingleValueEvent(object: ValueEventListener {
      override fun onDataChange(p0: DataSnapshot?) {
        printMessage = p0?.getValue(String::class.java)?:"ok"
        GlobalPrefences.setString("printMessage", printMessage?:"ok")
      }
      override fun onCancelled(p0: DatabaseError?) {
        logi(TAG(), "error", p0?.toException()?:"null error")
      }
    })



    if(savedInstanceState != null) {

      mCustomerName = savedInstanceState.getString("customerName")
      mCustomerPhone = savedInstanceState.getString("customerPhone")
      mCustomerId = savedInstanceState.getInt("customerId", 0)

      if (mCustomerId != 0) {
        customer.text = mCustomerName
      }

    }

    mAutoCompletableBase = AutoCompletableBase(baseContext)
    title = getString(R.string.add_receipt)

    modify_more.setOnClickListener {
      EditDetailsActivity.go(this)
    }

    select_name.setOnClickListener {
      SelectCustomerActivity.goForResult(this, 0)
    }

    print.setOnClickListener {
      print()
    }

    plus_amount.setOnClickListener {
      var amount = amounts.text.toString().toFloatOrNull()
      if (amount == null) {
        amount = 100.toFloat()
      } else {
        amount += 20
      }
      amounts.setText(amount.toString())
    }

    extract_amount.setOnClickListener {
      var amount = amounts.text.toString().toFloatOrNull()
      if (amount == null) {
        amount = 90.toFloat()
      } else {
        amount = ((amount?.toInt()?:90) - 20).toFloat()
      }
      amounts.setText(amount.toString())
    }

    select_device.setOnClickListener {
      mDialog = AutoCompletableDialog(this, "devices", {
        device.setText(it)
        mDialog?.dismiss()
      })
      mDialog?.show()
    }

    select_staff.setOnClickListener {
      mDialog = AutoCompletableDialog(this, "staffs", {
        staffs.setText(it)
        mDialog?.dismiss()
      })
      mDialog?.show()
    }

    val calendar = Calendar.getInstance()
    dates.text = "${calendar[Calendar.DAY_OF_MONTH]}/${calendar[Calendar.MONTH] + 1}/${calendar[Calendar.YEAR]}"

    select_date.setOnClickListener {
      val picker = DatePickerDialog(this, { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
        val m = month + 1
        dates.text = "$dayOfMonth/$m/$year"
      }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
      picker.show()
    }

  }

  override fun onSaveInstanceState(outState: Bundle?) {
    super.onSaveInstanceState(outState)

    outState?.putString("customerName", mCustomerName)
    outState?.putString("customerPhone", mCustomerPhone)
    outState?.putInt("customerId", mCustomerId)

  }

  fun print() {

    logi(TAG(), "result: ", mCustomerId, mCustomerName?:"null", mCustomerPhone?:"null", "device:",
        device.text, "amount:", amounts.text, "address", addresses.text,"staff:", staffs.text,
        "date:", dates.text)

    if (
    mCustomerId == 0 ||
        isAnyEmpty(
            device,
            amounts,
            addresses,
            staffs,
            dates
        )
        ) {

      mDialog = AlertDialog.Builder(this)
          .setTitle(R.string.oppps)
          .setMessage(R.string.please_enter_valid_values)
          .setPositiveButton(R.string.ok, null)
          .show()

      return
    }

    // name: "Mehmet Çevik", phone: "+90 0506 691 08 43"

    val receipt = Receipt(
        0, // we will fill it.
        mCustomerId,
        System.currentTimeMillis(),
        mCustomerName?:"",
        mCustomerPhone?:""
    )

    receipt.setAddress(addresses.text.toString())
    if (!notes.text.isEmpty())
      receipt.setNote(notes.text.toString())
    receipt.setStaff(staffs.text.toString())
    receipt.setDevice(device.text.toString())
    receipt.setTask(tasks.text.toString())
    receipt.setAmount(amounts.text.toString().toDouble())





    GlobalPrefences.setString("last_staff", staffs.text.toString())
    mAutoCompletableBase?.pushIsNotExists("devices", device.text.toString().trimLastSpaces())
    mAutoCompletableBase?.pushIsNotExists("staffs", staffs.text.toString().trimLastSpaces())

    PrintUtils.print(receipt, this)

  }

  override fun onDestroy() {
    super.onDestroy()
    mDialog?.dismiss()
    mAutoCompletableBase?.close()
    mProgressDialog?.dismiss()
    printlnRef.removeEventListener(mPrintlnListener)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

      mCustomerId = data.getIntExtra("user_id", 0)
      mCustomerName = data.getStringExtra("name")
      mCustomerPhone = data.getStringExtra("phone")

      customer.text = mCustomerName
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val icon = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_print)
        .color(Color.WHITE)
        .sizeDp(18)
    val add = menu?.add(0, 0, 0, getString(R.string.print_it))
    add?.icon = icon
    add?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    addOptions(menu)

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == 0) {
      print()
    } else if (item?.itemId == 1) {
      ReceiptsActivity.go(this)
    } else {
      BluetoothDevicesActivity.go(this)
    }
    return super.onOptionsItemSelected(item)
  }

  fun addOptions(menu: Menu?) {

    val options = arrayListOf<String>(
        getString(R.string.receipts),
        getString(R.string.change_writer)
    )

    for (i in 0..options.size -1)
      menu?.add(1, i + 1, i, options[i])

  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
  }

}
