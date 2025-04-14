package com.nilac.zebra.mobiledimensioningparceldemo.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.nilac.zebra.mobiledimensioningparceldemo.AppConstants
import com.zebra.nilac.dwconfigurator.configuration.ProfileConfigurator
import com.zebra.nilac.dwconfigurator.configuration.plugins.BarcodePlugin
import com.zebra.nilac.dwconfigurator.configuration.plugins.IntentOutputPlugin
import com.zebra.nilac.dwconfigurator.configuration.plugins.KeystrokeOutputPlugin
import com.zebra.nilac.dwconfigurator.models.ConfigMode
import com.zebra.nilac.dwconfigurator.models.intent.IntentOutputDelivery

object DWUtil {

    private const val TAG = "DWUtil"

    fun generateDWProfileIntent(context: Context): Bundle {
        Log.i(TAG, "Creating DW Profile unless it doesn't exists already")

        val barcodePlugin = BarcodePlugin.Builder()
            .setEnabled(true)
            .create()

        val intentPlugin = IntentOutputPlugin.Builder()
            .setIntentAction(AppConstants.DW_SCANNER_INTENT_ACTION)
            .setIntentOutputDelivery(IntentOutputDelivery.BROADCAST)
            .setEnabled(true)
            .create()

        val keystrokeOutputPlugin = KeystrokeOutputPlugin.Builder()
            .setEnabled(false)
            .create()

        return ProfileConfigurator.Builder()
            .setProfileName("MD Parcel Demo")
            .setProfileEnabled(true)
            .addAppAssociation(
                context.packageName,
                "com.nilac.zebra.mobiledimensioningparceldemo.ui.MainActivity"
            )
            .addPlugin(barcodePlugin)
            .addPlugin(intentPlugin)
            .addPlugin(keystrokeOutputPlugin)
            .setConfigMode(ConfigMode.CREATE_IF_NOT_EXIST)
            .create()
    }
}