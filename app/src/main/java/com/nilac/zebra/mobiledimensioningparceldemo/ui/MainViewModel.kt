package com.nilac.zebra.mobiledimensioningparceldemo.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nilac.zebra.mobiledimensioningparceldemo.models.Event
import com.symbol.emdk.EMDKResults
import com.zebra.nilac.emdkloader.EMDKLoader
import com.zebra.nilac.emdkloader.ProfileLoader
import com.zebra.nilac.emdkloader.interfaces.EMDKManagerInitCallBack
import com.zebra.nilac.emdkloader.interfaces.ProfileLoaderResultCallback
import com.zebra.nilac.emdkloader.utils.SignatureUtils
import kotlinx.coroutines.Dispatchers

class MainViewModel(private var application: Application) : AndroidViewModel(application) {

    val manageExternalStorageResult: MutableLiveData<Event<Boolean>> = MutableLiveData()

    fun preGrantManageExternalStoragePermission() {
        //Initialising EMDK Manager First...
        Log.i(TAG, "Initialising EMDK Manager")

        if (!EMDKLoader.getInstance().isManagerInit()) {
            EMDKLoader.getInstance()
                .initEMDKManager(application.applicationContext, object : EMDKManagerInitCallBack {
                    override fun onFailed(message: String) {
                        Log.e(TAG, "Failed to initialise EMDK Manager")
                    }

                    override fun onSuccess() {
                        Log.i(TAG, "EMDK Manager was successfully initialised")
                        Log.i(TAG, "Process profile...")

                        val profile = """
                            <wap-provisioningdoc>
                                <!-- External Storage Permission Profile -->
                                <characteristic type="AccessMgr">
                                    <parm name="PermissionAccessPermissionName" value="6" />
                                </characteristic>
                                <characteristic type="Profile">
                                    <parm name="ProfileName" value="ManageExternalStorageAccess" />
                                    <parm name="ModifiedDate" value="2025-04-01 15:07:36" />
                                    <parm name="TargetSystemVersion" value="10.5" />
                                    <characteristic type="AccessMgr" version="10.4">
                                        <parm name="emdk_name" value="" />
                                        <parm name="PermissionAccessAction" value="1" />
                                        <parm name="PermissionAccessPermissionName"
                                            value="android.permission.MANAGE_EXTERNAL_STORAGE" />
                                        <parm name="PermissionAccessPackageName"
                                            value="${application.packageName}" />
                                        <parm name="PermissionAccessSignature"
                                            value="${SignatureUtils.getAppSigningCertificate(application)}" />
                                    </characteristic>
                                </characteristic>
                            </wap-provisioningdoc>"""

                        ProfileLoader().processProfileNow(
                            "ManageExternalStorageAccess",
                            profile,
                            object : ProfileLoaderResultCallback {
                                override fun onProfileLoadFailed(errorObject: EMDKResults) {
                                    //Nothing to see here..
                                }

                                override fun onProfileLoadFailed(message: String) {
                                    Log.e(TAG, "Failed to process profile")
                                    manageExternalStorageResult.postValue(Event(false))
                                }

                                override fun onProfileLoaded() {
                                    manageExternalStorageResult.postValue(Event(true))
                                }
                            })
                    }
                })
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}