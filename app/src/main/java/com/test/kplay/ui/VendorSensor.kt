package com.test.kplay.ui

import android.hardware.Sensor

enum class VendorSensor(
    val title: String,
    val type: Int,
    val length: Int
) {
    NONE("None", 0, 0),
    ACCELEROMETER("Accelerometer", Sensor.TYPE_ACCELEROMETER, 3),
    GYROSCOPE("Gyroscope", Sensor.TYPE_GYROSCOPE, 3),
    PRESSURE("Pressure", Sensor.TYPE_PRESSURE, 1),
    TEMPERATURE("Temperature", Sensor.TYPE_TEMPERATURE, 1),
    STEP("Step", Sensor.TYPE_STEP_COUNTER, 1),
    HR("Heart Rate", Sensor.TYPE_HEART_RATE, 1),
    SPO("SPO", 0x10002, 1),
    SPO_RAW("SPO_RAW", 0x10003, 8),
    HR_RAW("HR_RAW", 0x10004, 4),
    ECG("ECG", 0x10005, 1),
}
