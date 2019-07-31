package org.jetbrains.sampler

import java.io.File

object Settings {
    private val rootProjectDir = File("/home/peter.bogdanov/IdeaProjects/sampler/")
    val storageDir = File(rootProjectDir, "_storage")
}