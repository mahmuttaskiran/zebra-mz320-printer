package opcon.com.printer

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_select_customer.*
import kotlinx.coroutines.experimental.async
import android.os.Looper
import com.mikepenz.iconics.context.IconicsContextWrapper
import com.zebra.sdk.comm.BluetoothConnectionInsecure
import org.w3c.dom.Text


class SelectCustomerActivity : AppCompatActivity() {

  var mUsers: ArrayList<User> = ArrayList()
  lateinit var mAdapter: UserAdapter
  var mSearchLock = Any()
  lateinit var mUserBase: ReceiptBase
  var mDialog: Dialog? = null

  companion object {
    var searchedText = ""
    var searchedWithPhone = false
    var eventManager: EventManager? = null
    fun go(ac: Activity) {
      ac.startActivity(
          Intent(
              ac,
              SelectCustomerActivity::class.java
          )
      )
    }

    fun goForResult(ac: Activity, rc: Int) {
      ac.startActivityForResult(
          Intent(
              ac,
              SelectCustomerActivity::class.java
          ),
          rc
      )
    }
  }

  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_select_customer)
    eventManager = EventManager()
    mUserBase = ReceiptBase(baseContext)

    setSupportActionBar(toolbar)
    title = ""
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val icon = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_keyboard_backspace)
        .color(Color.WHITE)
        .sizeDp(18)

    supportActionBar?.setHomeAsUpIndicator(icon)
    supportActionBar?.elevation = 5.toFloat()

    eventManager?.on("callToUser", 0, {
      val intent = Intent(Intent.ACTION_CALL)
      intent.data = Uri.parse("tel:" + (mUsers[it].phone))
      startActivity(intent)
    })

    eventManager?.on("messageToUser", 0, {
      val uri = Uri.parse("smsto:${mUsers[it].phone}")
      val intent = Intent(Intent.ACTION_SENDTO, uri)
      startActivity(intent)
    })

    eventManager?.on("userSelected", 0, {

      val user: User = mUsers[it]

      val data = Intent()
      data.putExtra("name", user.name)
      data.putExtra("phone", user.phone)
      data.putExtra("user_id", user._id)

      setResult(Activity.RESULT_OK, data)
      finish()

    })

    mAdapter = UserAdapter(mUsers)
    recyclerUsers.layoutManager = LinearLayoutManager(this)
    recyclerUsers.adapter = mAdapter

    search.afterTextChanged { it ->

      if (!it.isBlank() || !it.isEmpty()) {

        searchAsync(it)

      } else {

        mUsers.clear()
        mAdapter.notifyDataSetChanged()

        searchedText = it
      }
    }
  }



  fun searchAsync(text: String) {
    async {
      synchronized(mSearchLock, {
        val filtered = mUserBase.searchInUsers(text)
        searchedWithPhone = filtered.first
        if (recyclerUsers != null) {
          runOnUiThread {
            synchronized(mSearchLock, {
              if (recyclerUsers != null) {
                mUsers.clear()
                mUsers.addAll(filtered.second)
                mAdapter.notifyDataSetChanged()
              }
            })
          }
        }
      })
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    eventManager?.release()
    eventManager = null
    mUserBase.close()
    mDialog?.dismiss()
  }

  class UserAdapter(var users: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserHolder>() {

    override fun getItemCount(): Int {
      return users.size
    }

    override fun onBindViewHolder(holder: UserHolder?, position: Int) {
      holder?.forUser(users[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserHolder {
      return UserHolder(
          LayoutInflater.from(parent?.context).inflate(
              R.layout.row_user,
              parent,
              false
          )
      )
    }

    class UserHolder(view: View): RecyclerView.ViewHolder(view) {
      var name: TextView = view.findViewById(R.id.name)
      var phone: TextView = view.findViewById(R.id.phone)
      var call: ImageView = view.findViewById(R.id.call)
      var message: ImageView = view.findViewById(R.id.message)
      var root: RelativeLayout = view.findViewById(R.id.rootUser)

      fun forUser(user: User) {

        if (searchedText != "") {
          val spannableName = SpannableString(user.name)
          val spannablePhone = SpannableString(user.phone)

          if (searchedWithPhone) {
            val index = user.phone.toLowerCase().indexOf(searchedText.toLowerCase())
            spannablePhone.setSpan(BackgroundColorSpan(Color.RED),
                index, index + searchedText.length, 0)
          } else {
            val index = user.name.toLowerCase().indexOf(searchedText.toLowerCase())
            spannableName.setSpan(BackgroundColorSpan(Color.RED),
                index, index + searchedText.length, 0)
          }

          name.text = spannableName
          phone.text = spannablePhone

        } else {
          name.text = user.name
          phone.text = user.phone
        }

        call.setOnClickListener {
          eventManager?.dispatch("callToUser", adapterPosition)
        }

        message.setOnClickListener {
          eventManager?.dispatch("messageToUser", adapterPosition)
        }

        root.setOnClickListener {
          eventManager?.dispatch("userSelected", adapterPosition)
        }

      }

    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == android.R.id.home) {
      onBackPressed()
    } else if (item?.itemId == 0) { // ok
      Toast.makeText(baseContext, "est", Toast.LENGTH_LONG).show()
      if (search.text.toString().isEmpty() || search.text.toString().isBlank()) {
        mDialog = AlertDialog.Builder(this)
            .setTitle(R.string.oppps)
            .setMessage(R.string.please_enter_a_user_name_to_save)
            .setCancelable(true)
            .show()
      } else {

        val view = LayoutInflater.from(this).inflate(R.layout.save_user_dialog, null)

        val name: TextView  = view.findViewById(R.id.name)
        name.text = search.text.toString().trimSpaces()
        val phone: TextView  = view.findViewById(R.id.phone)
        val alert: TextView = view.findViewById(R.id.alert)

        mDialog = AlertDialog.Builder(this)
            .setView(view)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save_customer, { _, _ ->
              if (name.text.toString().isEmpty() || phone.text.toString().isEmpty()) {
                alert.text = getString(R.string.please_enter_a_user_name_and_phone_to_save)
                alert.visible()
              } else {
                val data = Intent()
                data.putExtra("name", name.text.toString())
                data.putExtra("phone", phone.text.toString())
                val newUser = mUserBase.putUser(name.text.toString(), phone.text.toString())
                logi(TAG(), "newUser", newUser?:"fuck!")
                if (newUser != null) {
                  data.putExtra("user_id", newUser._id)
                  setResult(Activity.RESULT_OK, data)
                } else {
                  setResult(Activity.RESULT_CANCELED)
                }
                finish()
              }
            }).create()

        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.show()

      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val icon = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_person_add)
        .color(Color.WHITE)
        .sizeDp(18)
    val add = menu?.add(0, 0, 0, getString(R.string.print_it))
    add?.icon = icon
    add?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

    return super.onCreateOptionsMenu(menu)
  }



}
