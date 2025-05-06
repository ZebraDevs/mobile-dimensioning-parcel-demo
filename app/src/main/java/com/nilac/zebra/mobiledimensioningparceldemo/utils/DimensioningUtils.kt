package com.nilac.zebra.mobiledimensioningparceldemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Bitmap
import android.os.Build
import com.nilac.zebra.mobiledimensioningparceldemo.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object DimensioningUtils {

    fun isDimensioningServiceAvailable(context: Context): Boolean {
        val pm: PackageManager = context.packageManager

        val appInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) pm.getApplicationInfo(
                AppConstants.ZEBRA_DIMENSIONING_PACKAGE,
                ApplicationInfoFlags.of(GET_META_DATA.toLong())
            ) else pm.getApplicationInfo(AppConstants.ZEBRA_DIMENSIONING_PACKAGE, 0)
        } catch (ex: NameNotFoundException) {
            null
        }
        return appInfo != null
    }

    suspend fun saveBitMapImageToFile(bitmap: Bitmap, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            val dir = File(AppConstants.PARCELS_FOLDER_PATH)
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "$fileName.png")
            runCatching {
                file.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }.isSuccess
        }
}