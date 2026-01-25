package com.ilagent.nativeoutline.utils.activityresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.contract.ActivityResultContract
import android.content.ActivityNotFoundException

class VPNPermissionContract : ActivityResultContract<Unit, Boolean>() {

    override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Boolean>? {
        val prepareIntent = VpnService.prepare(context)
        return if (prepareIntent == null) SynchronousResult(true) else null
    }

   override fun createIntent(context: Context, input: Unit): Intent {
        val intent = VpnService.prepare(context)
            ?: throw IllegalStateException("VPN permission is already granted, createIntent shouldn't be called")

        val pm = context.packageManager
        val canResolve = intent.resolveActivity(pm) != null
        if (!canResolve) {
           throw ActivityNotFoundException("System VPN consent activity not found on this device")
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}