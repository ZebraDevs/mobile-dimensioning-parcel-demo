package com.nilac.zebra.mobiledimensioningparceldemo

import android.os.Environment
import java.io.File

object AppConstants {

    const val ZEBRA_DIMENSIONING_PACKAGE = "com.zebra.dimensioning"

    const val DW_SCANNER_INTENT_ACTION = "com.nilac.zebra.mobiledimensioningparceldemo.SCANNER"
    const val DW_DATA_STRING_TAG = "com.symbol.datawedge.data_string"

    private const val FOLDER_NAME = "md-dimensioning-demo"

    const val PARCELS_FILE_NAME = "parcels_list.csv"
    val PARCELS_FOLDER_PATH = Environment.getExternalStorageDirectory().absolutePath + "/$FOLDER_NAME"

    const val APPLICATION_PACKAGE = "APPLICATION_PACKAGE"

    const val MODULE = "MODULE"
    const val PARCEL_MODULE = "parcel"

    const val DIMENSIONING_PARAMS_LENGTH = "LENGTH"
    const val DIMENSIONING_PARAMS_LENGTH_STATUS = "LENGTH_STATUS"

    const val DIMENSIONING_PARAMS_WIDTH = "WIDTH"
    const val DIMENSIONING_PARAMS_WIDTH_STATUS = "WIDTH_STATUS"

    const val DIMENSIONING_PARAMS_HEIGHT = "HEIGHT"
    const val DIMENSIONING_PARAMS_HEIGHT_STATUS = "HEIGHT_STATUS"

    const val DIMENSIONING_TIMESTAMP = "TIMESTAMP"

    const val DIMENSIONING_SIZE_BELOW_RANGE = "BelowRange"
    const val DIMENSIONING_SIZE_IN_RANGE = "InRange"
    const val DIMENSIONING_SIZE_ABOVE_RANGE = "AboveRange"
    const val DIMENSIONING_SIZE_NO_DIM = "NoDim"

    const val INTENT_ACTION_ENABLE_DIMENSION = "com.zebra.dimensioning.ENABLE_DIMENSION"
    const val INTENT_ACTION_DISABLE_DIMENSION = "com.zebra.dimensioning.DISABLE_DIMENSION"
    const val INTENT_ACTION_GET_DIMENSION = "com.zebra.parceldimensioning.GET_DIMENSION"

    const val RESULT_MESSAGE = "RESULT_MESSAGE"
    const val RESULT_CODE = "RESULT_CODE"
    const val RESULT_DEF_VALUE = 1
}