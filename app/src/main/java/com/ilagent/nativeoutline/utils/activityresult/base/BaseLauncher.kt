package com.ilagent.nativeoutline.utils.activityresult.base

import android.content.ActivityNotFoundException
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * A `ActivityResultAPI` launcher specifying that an activity can be called with an input of type [I]
 * and produce an output of type [O].
 */
abstract class BaseLauncher<I, O>(
    private val contract: ActivityResultContract<I, O>
) : ActivityResultCallback<O>, DefaultLifecycleObserver {

    private val resultBuilder = ResultBuilder<O>()
    private var resultLauncher: ActivityResultLauncher<I>? = null

    open fun launch(input: I, callbackBuilder: ResultBuilder<O>.() -> Unit) {
        resultBuilder.callbackBuilder()
        try {
            resultLauncher?.launch(input)
                ?: run { resultBuilder.failed.invoke() }
        } catch (e: ActivityNotFoundException) {
            resultBuilder.failed.invoke()
        } catch (t: Throwable) {
            resultBuilder.failed.invoke()
        }
    }

    fun register(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    final override fun onCreate(owner: LifecycleOwner) {
        resultLauncher = when (owner) {
            is ComponentActivity -> owner.registerForActivityResult(contract, this)
            is Fragment -> owner.registerForActivityResult(contract, this)
            else -> null
        }
    }

    final override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        runCatching { resultLauncher?.unregister() }
        resultLauncher = null
    }

    override fun onActivityResult(result: O) {
        if (result != null) resultBuilder.success.invoke(result)
        else resultBuilder.failed.invoke()
    }
}

@JvmName("launchUnit")
fun <O> BaseLauncher<Unit, O>.launch(callbackBuilder: ResultBuilder<O>.() -> Unit) {
    launch(Unit, callbackBuilder)
}