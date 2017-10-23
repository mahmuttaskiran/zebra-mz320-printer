package opcon.com.printer

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.context.IconicsContextWrapper
import kotlinx.android.synthetic.main.activity_bluetooth_devices.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

data class BluetoothDevice(var name: String, var address: String)

class BluetoothDevicesActivity : AppCompatActivity() {

    lateinit var mAdapter: Adapter
    lateinit var mBluetoothDevices: ArrayList<BluetoothDevice>
    var mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    var mDeviceFinderTimer: Timer? = null

    companion object {
        private var defaultDevice: BluetoothDevice? = null
        private lateinit var eventManager: EventManager

        init {
            defaultDevice = getDefaultBluetoothDevice()
        }

        fun getDefaultBluetoothDevice(): BluetoothDevice? {

            if (defaultDevice != null) {
                return defaultDevice
            }

            val name = GlobalPrefences.getString(
                    "default_device_name",
                    null
            ) ?: return null

            val macAddress = GlobalPrefences.getString(
                    "default_device_address",
                    null
            ) ?: return null

            return BluetoothDevice(name, macAddress)
        }

        fun setDefaultBluetoothDevice(device: BluetoothDevice) {
            GlobalPrefences.setString(
                    "default_device_name",
                    device.name
            )

            GlobalPrefences.setString(
                    "default_device_address",
                    device.address
            )

            defaultDevice = device
        }

        fun go(activity: Activity) {
            activity.startActivity(
                    Intent(
                            activity,
                            BluetoothDevicesActivity::class.java
                    )
            )
        }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_bluetooth_devices)

      eventManager = EventManager()
      recyclerBluetooth.layoutManager = LinearLayoutManager(this)

      if (mBluetoothAdapter == null) {
          devices_root.gone()
          indicator_root.gone()
          bluetooth_not_supported.visible()
      } else {
          bluetooth_not_supported.gone()
          init()
      }

      title = getString(R.string.select_a_printer)
      // supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val icon = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_check)
        .color(Color.WHITE)
        .sizeDp(18)
    val add = menu?.add(0, 0, 0, getString(R.string.save))
    add?.icon = icon
    add?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
      if (item?.itemId == android.R.id.home) {
          // onBackPressed()
      } else if (item?.itemId == 0) {
        if (getDefaultBluetoothDevice() == null) {
          Toast.makeText(this,
              getString(R.string.please_select_an_device),
              Toast.LENGTH_LONG).show()
          return false
        } else {
          finish()
        }
      }
      return super.onOptionsItemSelected(item)
  }

  override fun onBackPressed() {
    if (BluetoothDevicesActivity.getDefaultBluetoothDevice() != null) {
      super.onBackPressed()
    }
  }

    fun init() {
        logi(TAG(), "init")

        eventManager.on("itemClicked", 1, {
            mAdapter.notifyDataSetChanged()
        })

        mBluetoothDevices = getDevices()
        mAdapter = Adapter(mBluetoothDevices)
        recyclerBluetooth.adapter = mAdapter

        mDeviceFinderTimer = timer(
                "device_finder_timer",
                true,
                TimeUnit.SECONDS.toMillis(5),
                TimeUnit.SECONDS.toMillis(5),
                {
                    val devices = getDevices()
                    mBluetoothDevices.clear()
                    mBluetoothDevices.addAll(devices)

                    var dataChanged = devices.size != mBluetoothDevices.size

                    if (!dataChanged) {
                        devices.mapIndexed { index, device ->
                            if (device != (mBluetoothDevices[index])) {
                                dataChanged = true
                            }
                        }
                    }

                    if (dataChanged && recyclerBluetooth != null && baseContext != null) {
                        runOnUiThread {
                            if (recyclerBluetooth != null && baseContext != null) {
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mDeviceFinderTimer?.cancel()
        eventManager.release()
    }

    private fun getDevices(): ArrayList<BluetoothDevice> {
        val list = ArrayList<BluetoothDevice>()
        mBluetoothAdapter?.bondedDevices?.
                mapTo(list) { BluetoothDevice(it.name, it.address) }
        return list
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    class Adapter(var devices: ArrayList<BluetoothDevice>):
            RecyclerView.Adapter<Adapter.BluetoothDeviceHolder>() {

        override fun onBindViewHolder(holder: Adapter.BluetoothDeviceHolder, position: Int) {
            holder.initFor(devices[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): Adapter.BluetoothDeviceHolder{
            return BluetoothDeviceHolder(
                    LayoutInflater.from(parent?.context).inflate(
                            R.layout.device_row,
                            parent,
                            false
                    )
            )
        }

        override fun getItemCount(): Int {
            return devices.size
        }

        class BluetoothDeviceHolder(view: View): RecyclerView.ViewHolder(view) {
            var name: TextView = view.findViewById(R.id.device_name)
            var macAddress: TextView = view.findViewById(R.id.device_mac_address)
            var defaultDeviceIndicator: ImageView = view.findViewById(R.id.device_default_indicator)
            var deviceRoot: RelativeLayout = view.findViewById(R.id.root_of_device)

            fun initFor(device: BluetoothDevice) {
                name.text = device.name
                macAddress.text = device.address

                if (defaultDevice == device) {
                    defaultDeviceIndicator.visible()
                } else {
                    defaultDeviceIndicator.gone()
                }

                deviceRoot.setOnClickListener {
                    setDefaultBluetoothDevice(device)
                    eventManager.dispatch("itemClicked", adapterPosition)
                }

            }
        }

    }

}
