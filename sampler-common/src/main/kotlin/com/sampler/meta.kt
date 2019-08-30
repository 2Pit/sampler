package com.sampler

import kotlinx.serialization.*

@Serializable
data class SampleDescription(
        val name: String,
        val url: String,
        val readme: String,
        val tags: List<String>
) {
    override fun toString(): String {
        return name
    }
}
