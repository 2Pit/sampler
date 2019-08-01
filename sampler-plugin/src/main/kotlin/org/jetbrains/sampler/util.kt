package org.jetbrains.sampler

import com.test.SampleDescription
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import java.io.File

fun downloadSampleDescriptions(): List<SampleDescription> {
    val json = Json(JsonConfiguration.Stable)
    val jsonText = File(Settings.storageDir, "meta.json").readText()
    return json.parse(SampleDescription.serializer().list, jsonText)
}