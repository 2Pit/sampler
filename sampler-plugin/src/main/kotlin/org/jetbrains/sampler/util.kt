package org.jetbrains.sampler

import com.test.SampleDescription
import com.test.Settings
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.list
import java.io.File
import java.io.IOException

fun downloadSampleDescriptions(): List<SampleDescription> {
    val json = Json(JsonConfiguration.Stable)
    return try {
        val jsonText = File(Settings.storageDir, "meta.json").readText()
        json.parse(SampleDescription.serializer().list, jsonText)
    } catch (e: IOException) {
        emptyList()
    } catch (e: JsonException) {
        emptyList()
    } catch (e: SerializationException) {
        emptyList()
    }
}