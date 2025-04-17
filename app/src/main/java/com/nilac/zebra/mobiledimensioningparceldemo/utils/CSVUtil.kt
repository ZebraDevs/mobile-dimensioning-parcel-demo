package com.nilac.zebra.mobiledimensioningparceldemo.utils

import android.util.Log
import android.widget.Toast
import com.nilac.zebra.mobiledimensioningparceldemo.AppConstants
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

object CSVUtil {
    private const val TAG = "CSVUtil"

    fun writeNewParcelInformation(
        id: String,
        date: String,
        time: String,
        length: String,
        width: String,
        height: String,
        weight: String,
        notes: String,
        callBacks: WritingStatus
    ) {
        try {
            val dir = File(AppConstants.PARCELS_FOLDER_PATH)
            if (!dir.exists()) dir.mkdirs()

            val csvFile = File(dir, AppConstants.PARCELS_FILE_NAME)
            val isNewFile = !csvFile.exists()

            MainScope().launch(Dispatchers.IO) {

                CSVWriter(FileWriter(csvFile, true)).use { writer ->
                    if (isNewFile) {
                        writer.writeNext(
                            arrayOf(
                                "Parcel ID",
                                "Date",
                                "Time",
                                "Length",
                                "Width",
                                "Height",
                                "Weight",
                                "Notes"
                            )
                        )
                    }

                    writer.writeNext(
                        arrayOf(
                            id,
                            date,
                            time,
                            length,
                            width,
                            height,
                            weight,
                            notes
                        )
                    )
                }
                callBacks.onFinished()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing CSV", e)
            callBacks.onFailed(e.toString())
        }
    }

    interface WritingStatus {
        fun onFinished()

        fun onFailed(errorMessage: String)
    }
}