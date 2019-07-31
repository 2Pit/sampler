package com.test

import kotlinx.serialization.*

@Serializable
data class SampleDescription(
        val name: String,
        val url: String,
        val readme: String,
        val tags: List<String>
)

