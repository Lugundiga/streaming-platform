package com.streaming.platform.utils

import android.app.AlertDialog
import android.content.Context
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

/**
 * Utility helper functions
 */
object Utils {

    /**
     * Show a toast message
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    /**
     * Show a snackbar message
     */
    fun showSnackbar(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration).show()
    }

    /**
     * Show confirmation dialog
     */
    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        positiveAction: () -> Unit,
        negativeAction: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { dialog, _ ->
                positiveAction()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                negativeAction?.invoke()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show alert dialog
     */
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        action: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                action?.invoke()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate password (minimum 6 characters)
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Validate if a string is a valid web URL
     */
    fun isValidUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        
        return Patterns.WEB_URL.matcher(url).matches() && 
                (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url))
    }
}
