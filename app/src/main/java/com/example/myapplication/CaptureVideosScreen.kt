package com.example.myapplication

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun CaptureVideosScreen() {
    val files =
        remember { File("${Environment.getExternalStorageDirectory().absolutePath}/H264Encoder/").listFiles() }
    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        files?.let { files ->
            items(files, key = { it.absolutePath }) { file ->
                FileItem(file)
            }
        }
    }
}

@Composable
fun FileItem(file: File) {
    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .background(Color(0xFFD3E4EC))
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(id = R.drawable.video),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = file.name)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = file.length().toString() + " bytes")
        }
    }
}
