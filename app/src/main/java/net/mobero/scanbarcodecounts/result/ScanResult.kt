
package net.mobero.scanbarcodecounts.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScanResult(
    val value: String,
    val type: String,
    val format: String,
    val isUrl: Boolean,
    var count: Int
) : Parcelable
