package opcon.com.printer

import android.app.Application
import android.content.Context

/**
 * Created by aslitaskiran on 13/10/2017.
 */

public class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        mApplicationContext = this
    }

    companion object {
        private lateinit var mApplicationContext: Context
        fun getApplicationContext(): Context {
            return mApplicationContext
        }
    }

}