package com.nilac.zebra.mobiledimensioningparceldemo.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import com.nilac.zebra.mobiledimensioningparceldemo.AppConstants

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
}