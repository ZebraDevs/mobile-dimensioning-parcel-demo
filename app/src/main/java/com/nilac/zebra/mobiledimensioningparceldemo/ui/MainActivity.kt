package com.nilac.zebra.mobiledimensioningparceldemo.ui

import android.app.PendingIntent
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputLayout
import com.nilac.zebra.mobiledimensioningparceldemo.AppConstants
import com.nilac.zebra.mobiledimensioningparceldemo.R
import com.nilac.zebra.mobiledimensioningparceldemo.databinding.ActivityMainBinding
import com.nilac.zebra.mobiledimensioningparceldemo.utils.CSVUtil
import com.nilac.zebra.mobiledimensioningparceldemo.utils.DWUtil
import com.nilac.zebra.mobiledimensioningparceldemo.utils.DimensioningUtils
import com.zebra.nilac.dwconfigurator.Constants
import com.zebra.nilac.dwconfigurator.DataWedgeWrapper
import com.zebra.nilac.dwconfigurator.interfaces.OnScanIntentListener
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), OnScanIntentListener, CSVUtil.WritingStatus {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var originalStrokeColorStateList: ColorStateList

    private var mLastCapturedImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        //Start Dimension Service
        startDimensionService()

        //Check DW Profile
        DataWedgeWrapper.sendIntent(
            this,
            Constants.IntentType.SET_CONFIG,
            DWUtil.generateDWProfileIntent(this)
        )

        mainViewModel.manageExternalStorageResult.observe(this) {
            val result = it.contentIfNotHandled ?: return@observe
            runOnUiThread {
                Toast.makeText(
                    this,
                    if (result) "Manage External Storage Successfully Granted" else "Failed To Grant Manage External Storage Permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        prepareUI()
    }

    override fun onResume() {
        super.onResume()
        DataWedgeWrapper.registerScanReceiver(
            this,
            AppConstants.DW_SCANNER_INTENT_ACTION,
            this
        )
    }

    override fun onPause() {
        super.onPause()
        DataWedgeWrapper.unregisterScanReceiver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDimensioningService()

        mainViewModel.manageExternalStorageResult.removeObservers(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == REQUEST_CODE && intent != null) {
            val actionName = intent.action
            val dimResultCode =
                intent.getIntExtra(AppConstants.RESULT_CODE, AppConstants.RESULT_DEF_VALUE)
            var dimResultMessage = intent.getStringExtra(AppConstants.RESULT_MESSAGE)
            if (dimResultMessage == null) dimResultMessage = ""
            Log.d(
                TAG,
                "onActivityResult: $actionName, $dimResultCode, $dimResultMessage"
            )

            if (actionName == AppConstants.INTENT_ACTION_ENABLE_DIMENSION && dimResultCode == 0) {
                //Enable Image Export
                enableImageOutputForDimensioning()
            } else if (actionName == AppConstants.INTENT_ACTION_GET_DIMENSION) {
                val length =
                    intent.getSerializableExtra(AppConstants.DIMENSIONING_PARAMS_LENGTH)!! as BigDecimal
                val lengthStatus =
                    intent.getStringExtra(AppConstants.DIMENSIONING_PARAMS_LENGTH_STATUS)!!

                val width =
                    intent.getSerializableExtra(AppConstants.DIMENSIONING_PARAMS_WIDTH) as BigDecimal
                val widthStatus =
                    intent.getStringExtra(AppConstants.DIMENSIONING_PARAMS_WIDTH_STATUS)!!

                val height =
                    intent.getSerializableExtra(AppConstants.DIMENSIONING_PARAMS_HEIGHT) as BigDecimal
                val heightStatus: String =
                    intent.getStringExtra(AppConstants.DIMENSIONING_PARAMS_HEIGHT_STATUS)!!

                //Grab Image Info
                mLastCapturedImage =
                    intent.getParcelableExtra(AppConstants.DIMENSIONING_PARAMS_IMAGE)

                if (heightStatus == AppConstants.DIMENSIONING_SIZE_NO_DIM || lengthStatus == AppConstants.DIMENSIONING_SIZE_NO_DIM || widthStatus == AppConstants.DIMENSIONING_SIZE_NO_DIM) {
                    Toast.makeText(
                        this,
                        "Error while performing the dimensioning, please try again",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                Log.i(
                    TAG,
                    "Acquired dimensioning with dimensions: Length: $length Width: $width Height: $height"
                )

                //Fill UI
                val lengthBoxStrokeColor = when (lengthStatus) {
                    AppConstants.DIMENSIONING_SIZE_BELOW_RANGE -> R.color.dimensioning_input_field_stoke_color_orange
                    AppConstants.DIMENSIONING_SIZE_ABOVE_RANGE -> R.color.dimensioning_input_field_stoke_color_red
                    else -> R.color.dimensioning_input_field_stoke_color_green
                }
                binding.parcelLengthInputLayout.applyStrokeAndHintColor(
                    ContextCompat.getColorStateList(
                        this,
                        lengthBoxStrokeColor
                    )!!
                )
                binding.parcelLengthInput.setText(length.toString())

                val widthBoxStrokeColor = when (widthStatus) {
                    AppConstants.DIMENSIONING_SIZE_BELOW_RANGE -> R.color.dimensioning_input_field_stoke_color_orange
                    AppConstants.DIMENSIONING_SIZE_ABOVE_RANGE -> R.color.dimensioning_input_field_stoke_color_red
                    else -> R.color.dimensioning_input_field_stoke_color_green
                }
                binding.parcelWidthInputLayout.applyStrokeAndHintColor(
                    ContextCompat.getColorStateList(
                        this,
                        widthBoxStrokeColor
                    )!!
                )
                binding.parcelWidthInput.setText(width.toString())

                val heightBoxStrokeColor = when (heightStatus) {
                    AppConstants.DIMENSIONING_SIZE_BELOW_RANGE -> R.color.dimensioning_input_field_stoke_color_orange
                    AppConstants.DIMENSIONING_SIZE_ABOVE_RANGE -> R.color.dimensioning_input_field_stoke_color_red
                    else -> R.color.dimensioning_input_field_stoke_color_green
                }
                binding.parcelHeightInputLayout.applyStrokeAndHintColor(
                    ContextCompat.getColorStateList(
                        this,
                        heightBoxStrokeColor
                    )!!
                )
                binding.parcelHeightInput.setText(height.toString())

            } else if (dimResultCode != 0) {
                Toast.makeText(this, "Error!\n$dimResultMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onScanEvent(intent: Intent) {
        Log.d(TAG, "Received a DataWedge scanner intent: $intent")
        val decodedBarcode = intent.getStringExtra(AppConstants.DW_DATA_STRING_TAG)!!

        binding.parcelIdInput.setText(decodedBarcode)

        LocalDateTime.now().let {
            binding.parcelDateInput.setText(it.format(dateFormatter))
            binding.parcelTimeInput.setText(it.format(timeFormatter))
        }

        clearMeasurements()
    }

    override fun onFinished() {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.success_writing_message), Toast.LENGTH_LONG)
                .show()
            clearEverything()
        }
    }

    override fun onFailed(errorMessage: String) {
        runOnUiThread {
            Toast.makeText(this, getString(R.string.error_writing_message), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun prepareUI() {
        binding.getDimensionsButton.setOnClickListener {
            startDimensioning()
        }

        binding.saveImage.setOnClickListener {
            if (mLastCapturedImage == null) {
                Toast.makeText(
                    this,
                    getString(R.string.dimensioning_save_captured_image_null),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val operation = DimensioningUtils.saveBitMapImageToFile(
                    mLastCapturedImage!!,
                    "${binding.parcelIdInput.text}_${binding.parcelDateInput.text}_${
                        binding.parcelTimeInput.text!!.replace(
                            Regex(":"),
                            ""
                        )
                    }"
                )

                Toast.makeText(
                    this@MainActivity,
                    if (operation) getString(R.string.dimensioning_save_captured_image_success) else getString(
                        R.string.dimensioning_save_captured_image_failed
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.confirmButton.setOnClickListener {
            if (validateFields(
                    binding.parcelIdInputLayout,
                    binding.parcelDateInputLayout,
                    binding.parcelTimeInputLayout,
                    binding.parcelLengthInputLayout,
                    binding.parcelWidthInputLayout,
                    binding.parcelHeightInputLayout,
                    binding.parcelWeightInputLayout
                )
            ) {
                CSVUtil.writeNewParcelInformation(
                    binding.parcelIdInput.text.toString(),
                    binding.parcelDateInput.text.toString(),
                    binding.parcelTimeInput.text.toString(),
                    binding.parcelLengthInput.text.toString(),
                    binding.parcelWidthInput.text.toString(),
                    binding.parcelHeightInput.text.toString(),
                    binding.parcelWeightInput.text.toString(),
                    binding.parcelNotesInput.text.toString(),
                    this
                )
            }
        }

        val defaultColor = MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorControlNormal,
            Color.LTGRAY
        )

        originalStrokeColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled, android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled)
            ),
            intArrayOf(
                defaultColor,
                defaultColor,
                defaultColor
            )
        )

        //Check Manage External Storage Permission and apply it
        if (!Environment.isExternalStorageManager()) {
            mainViewModel.preGrantManageExternalStoragePermission()
            return
        }
    }

    private fun clearMeasurements() {
        clearFields(
            binding.parcelWidthInputLayout,
            binding.parcelLengthInputLayout,
            binding.parcelHeightInputLayout,
            binding.parcelWeightInputLayout
        )
        mLastCapturedImage = null
    }

    private fun clearEverything() {
        clearFields(
            binding.parcelIdInputLayout,
            binding.parcelDateInputLayout,
            binding.parcelTimeInputLayout,
            binding.parcelNotesInputLayout
        )
        clearMeasurements()
    }

    private fun startDimensioning() {
        Log.i(TAG, "Requesting Dimensioning for a new Capture")
        sendDimensioningBroadcast(AppConstants.INTENT_ACTION_GET_DIMENSION)
    }

    private fun startDimensionService() {
        if (!DimensioningUtils.isDimensioningServiceAvailable(this)) {
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Dimensioning Service is not available!",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        Log.i(TAG, "Starting Dimensioning Service")
        sendDimensioningBroadcast(AppConstants.INTENT_ACTION_ENABLE_DIMENSION)
    }

    private fun stopDimensioningService() {
        Log.i(TAG, "Stopping Dimensioning Service")
        sendDimensioningBroadcast(AppConstants.INTENT_ACTION_DISABLE_DIMENSION)
    }

    private fun enableImageOutputForDimensioning() {
        Log.i(TAG, "Enabling image output for dimensioning captures")
        sendDimensioningBroadcast(
            AppConstants.INTENT_ACTION_SET_DIMENSION_PARAMETER,
            Bundle().apply {
                putBoolean(AppConstants.DIMENSIONING_REPORT_IMAGE, true)
            })
    }

    private fun sendDimensioningBroadcast(broadcastAction: String, extras: Bundle? = null) {
        val dimensionServiceIntent = Intent().apply {
            action = broadcastAction
            setPackage(AppConstants.ZEBRA_DIMENSIONING_PACKAGE)
            putExtra(AppConstants.APPLICATION_PACKAGE, packageName)
            if (extras != null) {
                putExtras(extras)
            }
        }

        val lobPendingIntent =
            createPendingResult(REQUEST_CODE, Intent(), PendingIntent.FLAG_UPDATE_CURRENT)
        dimensionServiceIntent.putExtra("CALLBACK_RESPONSE", lobPendingIntent)

        if (broadcastAction == AppConstants.INTENT_ACTION_ENABLE_DIMENSION) {
            dimensionServiceIntent.putExtra(AppConstants.MODULE, AppConstants.PARCEL_MODULE)

            startForegroundService(dimensionServiceIntent)
            return
        }

        sendBroadcast(dimensionServiceIntent)
    }

    private fun validateFields(vararg fields: TextInputLayout): Boolean {
        var allValid = true

        for (field in fields) {
            val text = field.editText?.text?.toString()?.trim()
            if (text.isNullOrEmpty()) {
                allValid = false
                Toast.makeText(
                    this,
                    getString(R.string.error_validation_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        return allValid
    }

    private fun TextInputLayout.applyStrokeAndHintColor(colorStateList: ColorStateList) {
        setBoxStrokeColorStateList(colorStateList)
        hintTextColor = colorStateList
    }

    private fun clearFields(vararg fields: TextInputLayout) {
        fields.forEach {
            it.editText?.setText("")
            it.error = null
            it.applyStrokeAndHintColor(originalStrokeColorStateList)
        }
    }

    companion object {
        const val TAG = "MainActivity"

        const val REQUEST_CODE = 21210
    }
}