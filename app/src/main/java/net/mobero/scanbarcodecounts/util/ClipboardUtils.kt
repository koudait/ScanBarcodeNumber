
package net.mobero.scanbarcodecounts.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.getSystemService
import net.mobero.scanbarcodecounts.R

object ClipboardUtils {
    fun copyToClipboard(context: Context, label: String, text: String) {
        context.getSystemService<ClipboardManager>()?.let {
            it.setPrimaryClip(ClipData.newPlainText(label, text))
            Toast.makeText(context, R.string.toast_copy_to_clipboard, Toast.LENGTH_SHORT)
                .show()
        }
    }
}
