package opcon.com.printer

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by aslitaskiran on 14/10/2017.
 */
object GlobalPrefences {
    val PREF_NAME = "global_pref"
    lateinit var pref: SharedPreferences

    init {
        pref = getApplicationContext().getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        )
    }

    fun getEditor(): SharedPreferences.Editor {
        return pref.edit()
    }

    fun getString(name: String, default: String?): String? {
        return pref.getString(name, default)
    }

    fun getInt(name: String, default: Int): Int {
        return pref.getInt(name, default)
    }

    fun setInt(name: String, value: Int) {
        val editor = getEditor()
        editor.putInt(name, value)
        editor.commit()
    }

    fun setString(name: String, value: String) {
        val editor = getEditor()
        editor.putString(name, value)
        editor.commit()
    }

    // enough for this application.

}