package opcon.com.printer

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_edit_details.*

fun getBestTitle(name: String): String? {
  return EditDetailsActivity.getInformation(name)
}

class EditDetailsActivity : AppCompatActivity() {

  companion object {

    fun go(ac: Activity) {
      ac.startActivity(
          Intent(
              ac,
              EditDetailsActivity::class.java
          )
      )
    }

    fun getInformation(name: String): String? {

      val inf = GlobalPrefences.getString(name, null)
      if (inf != null) {
        return inf
      }

      val fields = R.string::class.java.fields
      for (field in fields) {
        if (field.name == name) {
          return getString(field.getInt(null))
        }
      }
      return null
    }

    fun saveInformation(name: String, inf: String) {
      GlobalPrefences.setString(name, inf)
    }

  }

  var mDialog: Dialog? = null

  fun isAnyEmpty(vararg texts: TextView): Boolean {
    for (text in texts) {
      if (text.isEmpty()) {
        return true
      }
    }
    return false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_edit_details)

    title = getString(R.string.edit_information)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    best_communication.text = getInformation("best_communication")?.toEditable()
    best_warranty.text = getInformation("best_warranty")?.toEditable()
    best_common_information.text = getInformation("best_common_information")?.toEditable()

    ok.setOnClickListener {
      save()
    }

  }

  fun save() {
    if (
    isAnyEmpty(
        best_communication,
        best_warranty,
        best_common_information
    )
        )
    {
      mDialog = AlertDialog.Builder(this)
          .setTitle(R.string.oppps)
          .setMessage(R.string.blank_text)
          .setCancelable(true)
          .show()
    } else {

      saveInformation("best_communication", best_communication.text.toString())
      saveInformation("best_warranty", best_warranty.text.toString())
      saveInformation("best_common_information", best_common_information.text.toString())

      finish()

    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    val icon = IconicsDrawable(this)
        .icon(GoogleMaterial.Icon.gmd_check)
        .color(Color.WHITE)
        .sizeDp(18)
    val add = menu?.add(0, 0, 0, getString(R.string.print_it))
    add?.icon = icon
    add?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    return super.onCreateOptionsMenu(menu)
  }


  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == android.R.id.home) {
      onBackPressed()
    } else if (item?.itemId == 0) {
      save()
    }
    return super.onOptionsItemSelected(item)
  }

}
