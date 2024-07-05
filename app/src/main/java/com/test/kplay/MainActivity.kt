package com.test.kplay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.UserHandle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.test.kplay.ui.DemoTheme
import com.test.kplay.ui.SensorNavigation
import com.test.kplay.ui.VendorSensor


class MainActivity : ComponentActivity() {

    companion object {
        private const val KEY_SENSOR = "None"

//        init {
//            System.loadLibrary("demo")
//        }
    }

    private var mCurrentSensor by mutableStateOf(VendorSensor.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        mCurrentSensor =
            VendorSensor.entries.toTypedArray()[savedInstanceState?.getInt(KEY_SENSOR) ?: 0]
        setContent {
            DemoTheme {
//                BodySensorPermissionScreen()
                ButtonScreen()
            }
        }
        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
//                    if (mCurrentSensor != VendorSensor.NONE) mCurrentSensor =
//                        VendorSensor.NONE else finish()
                }
            },
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SENSOR, mCurrentSensor.ordinal)
    }

    @Composable
    fun ButtonScreen() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = { launchUrl("com.cmri.universalapp://param?7B0A20202020202275726C223A2022636D63633A2F2F6469676974616C686F6D652F736D617274686F6D652F636F6D6D6F6E4465766963653F6469643D434D43432D3538393430352D37373633313730303030323337353031220A7D") }) {
                Text("light")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { launchUrl("com.cmri.universalapp://param?7B0A20202020202275726C223A2022636D63633A2F2F6469676974616C686F6D652F736D617274686F6D652F636F6D6D6F6E4465766963653F6469643D434D43432D3538393831322D6E71654F615831373236373337343038313330220A7D") }) {
                Text("tv")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { launchUrl("com.cmri.universalapp://param?7B0A20202020202275726C223A2022636D63633A2F2F6469676974616C686F6D652F736D617274686F6D652F636F6D6D6F6E4465766963653F6469643D434D43432D3538393838332D6E71654F615831373236323132323936373833220A7D") }) {
                Text("fan")
            }
        }
    }

    private fun launchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    @Composable
    fun BodySensorPermissionScreen() {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.BODY_SENSORS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            hasPermission = isGranted
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasPermission) {
                SensorNavigation(
                    vendorSensor = mCurrentSensor,
                    onItemClick = { mCurrentSensor = it },
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.BODY_SENSORS) }) {
                    Text("Request Body Sensors Permission")
                }
            }
        }
    }

//    private external fun stringFromJNI(): String
}