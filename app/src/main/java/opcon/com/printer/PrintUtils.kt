package opcon.com.printer

import android.app.Activity
import android.app.ProgressDialog
import android.support.v7.app.AlertDialog
import com.zebra.sdk.comm.BluetoothConnectionInsecure
import com.zebra.sdk.util.internal.CPCLUtilities
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.ArrayList

/**
 * Created by aslitaskiran on 22/10/2017.
 */


object PrintUtils {

  fun print(receipt: Receipt, activity: Activity) {

    AlertDialog.Builder(activity)
        .setTitle(R.string.are_u_sure)
        .setMessage(R.string.are_u_sure_to_print_this_receipt)
        .setPositiveButton(R.string.print_it, { _,_ ->

          val receiptBase = ReceiptBase(activity.baseContext)
          val re = receiptBase.addReceipt(receipt)
          receiptBase.close()


          val mProgressDialog = ProgressDialog.show(activity, getString(R.string.please_wait), getString(R.string.we_writing), true, false)


          PrintUtils.print(receipt, {
            activity.runOnUiThread {
              mProgressDialog?.dismiss()
            }
          },{
            mProgressDialog?.dismiss()

            AlertDialog.Builder(activity)
                .setTitle(R.string.oppps)
                .setMessage(R.string.an_error_occurs_when_printing)
                .setPositiveButton(R.string.print_it, { _,_ ->})
                .setNegativeButton("VAZGEÇ", { _,_ ->})
                .show()
          })

        })
        .setNegativeButton("VAZGEÇ", { _,_ ->})
        .show()

  }

  fun print(receipt: Receipt, onSuccessAction: () -> Unit, onErrorAction: () -> Unit) {

    val builder = CpclBuilder(receipt)


    logi(TAG(), "cpcl_builder: title","\n",  builder.buildTitle())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildContent())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildCommonInfTitle())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildCommonInf())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildCommonWarrentyTitle())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildCommonWarrenty())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildCommunicationTitle())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildCommunication())
    logi(TAG(), "cpcl_builder: title","\n",  builder.buildSignificant())


    val list = arrayListOf(
        builder.buildTitle(),
        builder.buildContent(),
        builder.buildCommonInfTitle(),
        builder.buildCommonInf(),
        builder.buildCommonWarrentyTitle(),
        builder.buildCommonWarrenty(),
        builder.buildCommunicationTitle(),
        builder.buildCommunication(),
        builder.buildSignificant()
    )

    printAsync(list, onSuccessAction, onErrorAction)

  }

  private fun printAsync(cpcl: ArrayList<String>, onSuccessAction: () -> Unit, onErrorAction: () -> Unit) {

    logi(TAG(), "print", cpcl)

    async {
      try {

        var address = "00:22:58:02:37:99"
        val defaultDevice = BluetoothDevicesActivity.getDefaultBluetoothDevice()

        if (defaultDevice != null) {
          address = defaultDevice.address
        }
        // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
        val thePrinterConn = BluetoothConnectionInsecure(address)
        // Open the connection - physical connection is established here.
        thePrinterConn.open()
        // Send the data to printer as a byte array.

        val printMessage = GlobalPrefences.getString("printMessage", "ok")
        if (printMessage == "ok") {
          for (str in cpcl) {
            thePrinterConn.write(str.toByteArray())
            // Make sure the data got to the printer before closing the connection

            delay(100)
          }
        } else {
            thePrinterConn.write(CpclBuilder(null).buildPrintMessage(printMessage?:"... call me.").toByteArray())
        }

        // Make sure the data got to the printer before closing the connection
        delay(3000)
        // Close the insecure connection to release resources.
        thePrinterConn.close()
        logi(TAG(), "printed")

        onSuccessAction()

      } catch (e: Exception) {
        e.printStackTrace()
        onErrorAction()
      }

    }

  }

}