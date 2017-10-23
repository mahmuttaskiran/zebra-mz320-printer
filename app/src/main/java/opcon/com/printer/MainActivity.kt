package opcon.com.printer

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            mDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.oppps))
                    .setMessage(getString(R.string.please_open_the_bluetooth))
                    .setCancelable(true)
                    .setOnCancelListener({
                        grantPermissions()
                    })
                    .show()
        }

    }

  private fun init(){
    if (BluetoothDevicesActivity.getDefaultBluetoothDevice() != null) {
      AddReceiptActivity.go(this)
      finish()
    } else {
      BluetoothDevicesActivity.go(this)
      finish()
    }
  }

  override fun onResume() {
    super.onResume()
    grantPermissions()
  }

  override fun onDestroy() {
      super.onDestroy()
      if (mDialog != null) {
          mDialog?.dismiss()
          mDialog = null
      }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
      if (!checkPermissions()) {
        if (mDialog != null) {
          mDialog = AlertDialog.Builder(this)
              .setTitle(getString(R.string.oppps))
              .setMessage(getString(R.string.please_grant_permissions))
              .setCancelable(true)
              .setOnCancelListener({
                grantPermissions()
              })
              .show()

          permissionRequestIndicator.text = getString(R.string.permissionRequest)
        }
      } else {
        init()
      }
  }

  private fun grantPermissions() {
      if (!checkPermissions()) {
          requestPermissions()
      } else {
        init()
      }
  }

  private fun requestPermissions() {
      ActivityCompat.requestPermissions(
              this,
              arrayOf(
                      Manifest.permission.BLUETOOTH,
                      Manifest.permission.BLUETOOTH_ADMIN,
                      Manifest.permission.ACCESS_COARSE_LOCATION,
                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
                      Manifest.permission.READ_EXTERNAL_STORAGE,
                      Manifest.permission.CALL_PHONE
              ),
              0
      )
  }

  private fun checkPermissions(): Boolean {
      return checkSelfPermission(
          Manifest.permission.BLUETOOTH,
          Manifest.permission.BLUETOOTH_ADMIN,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.CALL_PHONE
      )
  }

  private fun checkSelfPermission(vararg permissions: String): Boolean {
      return permissions.none {
          ContextCompat.checkSelfPermission(
                  baseContext,
                  it
          ) != PackageManager.PERMISSION_GRANTED
      }
  }

}
