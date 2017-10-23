package opcon.com.printer

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView

/**
 * Created by aslitaskiran on 13/10/2017.
 */

fun getApplicationContext(): Context {
    return MyApplication.getApplicationContext()
}

fun getString(id: Int) : String {
    return getApplicationContext().getString(id)
}

inline fun Any.TAG() : String {
    return this::class.java.simpleName
}

fun logi(tag: String, vararg logs: Any) {
    val builder = StringBuilder()
    for (l in logs) {
        builder.append(l).append(' ')
    }
    Log.i(tag, builder.toString())
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun String.toEditable(): Editable {
  return SpannableStringBuilder(this)
}

fun TextView.isEmpty(): Boolean {
  return this.text.toString().isEmpty() || this.text.toString().isBlank()
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable.toString())
    }
  })
}

fun isAnyEmpty(vararg texts: TextView): Boolean {
  for (text in texts) {
    if (text.isEmpty()) {
      return true
    }
  }
  return false
}

private fun String.toen(): String {
  return this.replace('Ç', 'C')
      .replace('ç', 'c')
      .replace('Ş', 'S')
      .replace('ş', 's')
      .replace('Ğ', 'G')
      .replace('ğ', 'g')
      .replace('İ', 'I')
      .replace('ı', 'i')
      .replace('Ö', 'O')
      .replace('Ü', 'U')
}

fun String.trimLastSpaces(): String {
  var x = this.length
  for (i in (this.length -1).downTo(0)) {
    if (this[i] == ' ' || this[i] == '\n') {
      x = i
    } else {
      break
    }
  }
  if (x != this.length) {
    return this.substring(0, x)
  }
  return this
}

fun String.trimFirstSpaces(): String {
  val length = this.length
  var x = -1
  for (i in 0 until length) {
    if (this[i] == ' ' || this[i] == '\n') {
      x = i
    } else {
      break
    }
  }
  if (x != -1) {
    return this.substring(x+1, length)
  }
  return this
}

fun String.trimSpaces(): String{
  return this.trimLastSpaces().trimFirstSpaces()
}