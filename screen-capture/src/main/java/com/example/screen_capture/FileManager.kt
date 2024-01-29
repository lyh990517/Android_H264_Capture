package com.example.screen_capture

import java.io.File

internal object FileManager {

    lateinit var filePath: String
    fun prepareFile(filePath: String) {
        this.filePath =  File(filePath).apply {
            parentFile?.takeIf { !it.exists() }?.mkdirs()
            if (!exists()) createNewFile()
        }.path
    }
}