package com.nilac.zebra.mobiledimensioningparceldemo.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.nilac.zebra.mobiledimensioningparceldemo.AppConstants
import com.nilac.zebra.mobiledimensioningparceldemo.R
import com.nilac.zebra.mobiledimensioningparceldemo.databinding.ActivityMainBinding
import com.nilac.zebra.mobiledimensioningparceldemo.utils.DWUtil
import com.nilac.zebra.mobiledimensioningparceldemo.utils.DimensioningUtils
import com.zebra.nilac.dwconfigurator.Constants
import com.zebra.nilac.dwconfigurator.DataWedgeWrapper
import com.zebra.nilac.dwconfigurator.interfaces.OnScanIntentListener
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(), OnScanIntentListener {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private lateinit var binding: ActivityMainBinding

    private lateinit var originalStrokeColorStateList: ColorStateList

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
        stopDimensioningService()
        super.onDestroy()
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

            if (dimResultCode == 1 || dimResultCode == 2) {
                Toast.makeText(this, "Error!\n$dimResultMessage", Toast.LENGTH_LONG).show()
                return
            }

            if (actionName == AppConstants.INTENT_ACTION_GET_DIMENSION && dimResultCode == 0) {
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
                val lengthBoxStrokeColor = getColorStateList(
                    when (lengthStatus) {
                        AppConstants.DIMENSIONING_SIZE_BELOW_RANGE -> R.color.dimensioning_input_field_stoke_color_orange
                        AppConstants.DIMENSIONING_SIZE_ABOVE_RANGE -> R.color.dimensioning_input_field_stoke_color_red
                        else -> R.color.dimensioning_input_field_stoke_color_green
                    }
                )
                binding.parcelLengthInputLayout.apply {
                    setBoxStrokeColorStateList(lengthBoxStrokeColor)
                    hintTextColor = lengthBoxStrokeColor
                }
                binding.parcelLengthInput.setText(length.toString())

                val widthBoxStrokeColor = getColorStateList(
                    when (widthStatus) {
                        AppConstants.DIMENSIONING_SIZE_BELOW_RANGE -> R.color.dimensioning_input_field_stoke_color_orange
                        AppConstants.DIMENSIONING_SIZE_ABOVE_RANGE -> R.color.dimensioning_input_field_stoke_color_red
                        else -> R.color.dimensioning_input_field_stoke_color_green
                    }
                )
                binding.parcelWidthInputLayout.apply {
                    setBoxStrokeColorStateList(widthBoxStrokeColor)
                    hintTextColor = widthBoxStrokeColor
                }
                binding.parcelWidthInput.setText(width.toString())

                val heightBoxStrokeColor = getColorStateList(
                    when (heightStatus) {
                        AppConstants.DIMENSIONING_SIZE_BELOW_RANGE -> R.color.dimensioning_input_field_stoke_color_orange
                        AppConstants.DIMENSIONING_SIZE_ABOVE_RANGE -> R.color.dimensioning_input_field_stoke_color_red
                        else -> R.color.dimensioning_input_field_stoke_color_green
                    }
                )
                binding.parcelHeightInputLayout.apply {
                    setBoxStrokeColorStateList(heightBoxStrokeColor)
                    hintTextColor = heightBoxStrokeColor
                }
                binding.parcelHeightInput.setText(height.toString())
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

    private fun prepareUI() {
        binding.getDimensionsButton.setOnClickListener {
            startDimensioning()
        }

        originalStrokeColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf()
            ),
            intArrayOf(
                Color.GRAY,
                Color.GRAY,
                Color.GRAY
            )
        )
    }

    private fun clearMeasurements() {
        binding.parcelWidthInputLayout.apply {
            setBoxStrokeColorStateList(originalStrokeColorStateList)
            hintTextColor = originalStrokeColorStateList
        }
        binding.parcelWidthInput.setText("")

        binding.parcelLengthInputLayout.apply {
            setBoxStrokeColorStateList(originalStrokeColorStateList)
            hintTextColor = originalStrokeColorStateList
        }
        binding.parcelLengthInput.setText("")

        binding.parcelHeightInputLayout.apply {
            setBoxStrokeColorStateList(originalStrokeColorStateList)
            hintTextColor = originalStrokeColorStateList
        }
        binding.parcelHeightInput.setText("")

        binding.parcelWeightInput.setText("")
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


    private fun sendDimensioningBroadcast(broadcastAction: String) {
        val dimensionServiceIntent = Intent().apply {
            action = broadcastAction
            setPackage(AppConstants.ZEBRA_DIMENSIONING_PACKAGE)
            putExtra(AppConstants.APPLICATION_PACKAGE, packageName)
            putExtra(AppConstants.MODULE, AppConstants.PARCEL_MODULE)
        }

        val lobPendingIntent =
            createPendingResult(REQUEST_CODE, Intent(), PendingIntent.FLAG_UPDATE_CURRENT)
        dimensionServiceIntent.putExtra("CALLBACK_RESPONSE", lobPendingIntent)

        if (broadcastAction == AppConstants.INTENT_ACTION_ENABLE_DIMENSION) {
            startForegroundService(dimensionServiceIntent)
            return
        }

        sendBroadcast(dimensionServiceIntent)
    }

    companion object {
        const val TAG = "MainActivity"

        const val REQUEST_CODE = 21210
    }
}