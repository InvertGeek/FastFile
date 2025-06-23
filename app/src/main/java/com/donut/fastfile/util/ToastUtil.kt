package com.donut.fastfile.util

import android.content.Context
import android.widget.Toast
import com.donut.fastfile.app
import com.donut.fastfile.appScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


var toast: Toast? = null

fun showToast(msg: String, context: Context = app, length: Int = Toast.LENGTH_LONG) {
    appScope.launch(Dispatchers.Main) {
        toast?.cancel()
        toast = Toast.makeText(context, msg, length).apply {
            show()
        }
    }
}
