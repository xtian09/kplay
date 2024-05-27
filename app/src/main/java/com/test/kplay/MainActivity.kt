package com.test.kplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.test.kplay.ui.DemoTheme
import com.test.kplay.ui.SensorNavigation
import com.test.kplay.ui.VendorSensor

class MainActivity : ComponentActivity() {

    companion object {
        private const val KEY_SENSOR = "None"

        init {
//            System.loadLibrary("demo")
        }
    }

    private var mCurrentSensor by mutableStateOf(VendorSensor.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        mCurrentSensor =
            VendorSensor.entries.toTypedArray()[savedInstanceState?.getInt(KEY_SENSOR) ?: 0]
        setContent {
            DemoTheme {
                SensorNavigation(
                    vendorSensor = mCurrentSensor,
                    onItemClick = { mCurrentSensor = it },
                )
            }
        }
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    if (mCurrentSensor != VendorSensor.NONE) mCurrentSensor =
                        VendorSensor.NONE else finish()
                }
            },
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SENSOR, mCurrentSensor.ordinal)
    }

//    private external fun stringFromJNI(): String
}