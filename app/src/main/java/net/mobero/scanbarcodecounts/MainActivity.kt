
package net.mobero.scanbarcodecounts

import android.animation.ValueAnimator
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.mlkit.vision.barcode.common.Barcode
import net.mobero.scanbarcodecounts.code.CodeScanner
import net.mobero.scanbarcodecounts.databinding.ActivityMainBinding
import net.mobero.scanbarcodecounts.extension.formatString
import net.mobero.scanbarcodecounts.extension.typeString
import net.mobero.scanbarcodecounts.permission.CameraPermission
import net.mobero.scanbarcodecounts.permission.PermissionDialog
import net.mobero.scanbarcodecounts.result.ScanResult
import net.mobero.scanbarcodecounts.result.ScanResultAdapter
import net.mobero.scanbarcodecounts.result.ScanResultDialog
import net.mobero.scanbarcodecounts.setting.Settings
import net.mobero.scanbarcodecounts.util.ReviewRequester
import net.mobero.scanbarcodecounts.util.Updater

class MainActivity : AppCompatActivity(), PermissionDialog.OnCancelListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var codeScanner: CodeScanner
    private var started: Boolean = false
    private val launcher = registerForActivityResult(
        CameraPermission.RequestContract(), ::onPermissionResult
    )
    private lateinit var adapter: ScanResultAdapter
    private lateinit var vibrator: Vibrator
    private val viewModel: MainActivityViewModel by viewModels()
    private val settings: Settings by lazy {
        Settings.get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        adapter = ScanResultAdapter(this) {
            ScanResultDialog.show(this, it)
        }
        binding.resultList.adapter = adapter
        binding.resultList.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        vibrator = getSystemService()!!
        codeScanner = CodeScanner(this, binding.previewView, ::onDetectCode)


        val size = viewModel.resultLiveData.value?.size ?: 0
        if (size >= 2) {
            binding.dummy.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = 0
            }
        }
        viewModel.resultLiveData.observe(this, adapter)
        viewModel.resultLiveData.observe(this) {
            if (it == null) return@observe
            binding.resultList.scrollToPosition(adapter.itemCount - 1)
            if (it.isNotEmpty()) {
                binding.scanning.isGone = true
            }
            if (it.size == 2) {
                expandList()
            }
        }
        if (CameraPermission.hasPermission(this)) {
            startCamera()
            Updater.startIfAvailable(this)
        } else {
            launcher.launch(Unit)
        }
    }

    override fun onRestart() {
        super.onRestart()
        if (!started) {
            if (CameraPermission.hasPermission(this)) {
                startCamera()
            } else {
                toastPermissionError()
                finishByError()
            }
        }
    }

    private fun onPermissionResult(granted: Boolean) {
        when {
            granted ->
                startCamera()
            CameraPermission.deniedWithoutShowDialog(this) ->
                PermissionDialog.show(this)
            else -> {
                toastPermissionError()
                finishByError()
            }
        }
    }

    override fun onPermissionCancel() {
        finishByError()
    }

    private fun finishByError() {
        super.finish()
    }

    override fun finish() {
        if (ReviewRequester.requestIfNecessary(this)) {
            return
        }
        super.finish()
    }

    private fun toastPermissionError() {
        Toast.makeText(this, R.string.toast_permission_required, Toast.LENGTH_LONG).show()
    }


    private fun startCamera() {
        if (started) return
        started = true
        codeScanner.start()
    }

    private fun onDetectCode(codes: List<Barcode>) {
        codes.forEach {
            val value = it.rawValue ?: return@forEach
            val result = ScanResult(
                value = value,
                type = it.typeString(),
                format = it.formatString(),
                isUrl = it.valueType == Barcode.TYPE_URL
            )
            if (viewModel.add(result)) {
                vibrate()
            }
        }
    }

    private fun expandList() {
        ValueAnimator.ofInt(binding.dummy.height, 0)
            .also {
                it.addUpdateListener {
                    binding.dummy.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        height = it.animatedValue as Int
                    }
                }
            }.start()
    }

    private fun vibrate() {
        if (!settings.vibrate) return
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

}
