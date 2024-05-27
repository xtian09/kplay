package com.test.kplay.ui

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.test.kplay.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SensorScreen(onItemClick: (vendorSensor: VendorSensor) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val sensors = VendorSensor.entries.filter { it != VendorSensor.NONE }
            items(sensors) {
                Text(
                    text = it.title,
                    modifier = Modifier
                        .clickable { onItemClick(it) }
                        .padding(16.dp)
                )
                ListItem(
                    headlineContent = { Text(text = it.title) },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onItemClick(it) },
                )
            }
        }
    }
}

@Composable
fun SensorNavigation(
    vendorSensor: VendorSensor,
    onItemClick: (vendorSensor: VendorSensor) -> Unit,
) {
    Crossfade(targetState = vendorSensor, label = "") {
        when (it) {
            VendorSensor.NONE -> SensorScreen(onItemClick = onItemClick)
            VendorSensor.ACCELEROMETER -> Accelerometer()
            else -> SensorValue(it)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SensorView(
    vendorSensor: VendorSensor,
    value: String,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(text = vendorSensor.title) }) }) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = value
            )
            content()
        }
    }
}

@Composable
fun Accelerometer() {
    val sensorValue by rememberSensorValueAsState(VendorSensor.ACCELEROMETER.type) {
        it?.values ?: FloatArray(3) { 0f }
    }
    val (x, y, z) = sensorValue
    SensorView(
        vendorSensor = VendorSensor.ACCELEROMETER,
        value = "X: $x\nY: $y\nZ: $z",
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        var center by remember { mutableStateOf(Offset(width / 2, height / 2)) }
        val orientation = LocalConfiguration.current.orientation
        val contentColor = LocalContentColor.current
        val radius = with(LocalDensity.current) { 10.dp.toPx() }
        center = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Offset(
                x = (center.x - x).coerceIn(radius, width - radius),
                y = (center.y + y).coerceIn(radius, height - radius),
            )
        } else {
            Offset(
                x = (center.x + y).coerceIn(radius, width - radius),
                y = (center.y + x).coerceIn(radius, height - radius),
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = contentColor,
                radius = radius,
                center = center,
            )
        }
    }
}

@Composable
fun SensorValue(vendorSensor: VendorSensor) {
    val sensorValue by rememberSensorValueAsState(
        type = vendorSensor.type,
        transformSensorEvent = {
            it?.values ?: FloatArray(vendorSensor.length) { 0f }
        },
    )
    val value = sensorValue
    SensorView(
        vendorSensor = vendorSensor, value = value.joinToString(separator = "\n")
    ) {

    }
}

@Composable
fun rememberSensorValueAsState(
    type: Int,
    transformSensorEvent: (event: SensorEvent?) -> FloatArray,
): State<FloatArray> {
    val context = LocalContext.current
    val sensorEventCallbackFlow = remember {
        sensorEventCallbackFlow(
            context = context,
            type = type,
            samplingPeriodUs = SensorManager.SENSOR_DELAY_NORMAL,
        )
    }
    val sensorEvent by sensorEventCallbackFlow.collectAsStateWithLifecycle(
        initialValue = ComposableSensorEvent(),
        minActiveState = Lifecycle.State.RESUMED,
    )
    return remember { derivedStateOf { transformSensorEvent(sensorEvent.event) } }
}

internal fun sensorEventCallbackFlow(
    context: Context,
    type: Int,
    samplingPeriodUs: Int,
): Flow<ComposableSensorEvent> = callbackFlow {
    val sensorManager = ContextCompat.getSystemService(context, SensorManager::class.java)
        ?: throw RuntimeException("SensorManager is null")
    val sensor = sensorManager.getDefaultSensor(type)
        ?: throw RuntimeException("Sensor of type $type is not available, use one of the isSensorAvailable functions")
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val composableEvent = ComposableSensorEvent(event = event)
            trySend(composableEvent)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // TODO: Handle sensor accuracy changes?
        }
    }
    val successful = sensorManager.registerListener(listener, sensor, samplingPeriodUs)
    if (!successful) throw RuntimeException("Failed to register listener for sensor ${sensor.name}")
    awaitClose { sensorManager.unregisterListener(listener) }
}

internal data class ComposableSensorEvent(
    val event: SensorEvent? = null,
    val timestamp: Long = event?.timestamp ?: SystemClock.elapsedRealtimeNanos(),
)