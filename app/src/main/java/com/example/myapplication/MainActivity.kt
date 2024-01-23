package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScreenCaptureUI() {
                        finish()
                        Toast.makeText(this, "화면 캡처가 시작됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenCaptureUI(onFinish: () -> Unit) {
    val context = LocalContext.current
    val fileName = remember { mutableStateOf("") }
    val manageExternalStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode != RESULT_OK)
                Toast.makeText(context, "권한을 허용해야 캡처가 가능합니다.", Toast.LENGTH_SHORT).show()
        })
    val writeExternalStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted)
                Toast.makeText(context, "권한을 허용해야 캡처가 가능합니다.", Toast.LENGTH_SHORT).show()
        })
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = Intent(context, CaptureService::class.java)
                intent.putExtra("code", result.resultCode)
                intent.putExtra("data", result.data)
                intent.putExtra(
                    "filePath",
                    Environment.getExternalStorageDirectory().absolutePath + "/H264Encoder/${fileName.value}.mp4"
                )
                context.startForegroundService(intent)
                onFinish()
            }
        }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION_CODES.R <= Build.VERSION.SDK_INT) {
            if (!Environment.isExternalStorageManager()) {
                manageExternalStorageLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            )
                writeExternalStorageLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    Scaffold {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FileNameInputView({ fileName.value }) { fileName.value = it }
            Spacer(modifier = Modifier.height(30.dp))
            ButtonView(context, mediaProjectionLauncher)
        }
    }
}

@Composable
fun ButtonView(
    context: Context,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    Button(onClick = {
        val mediaProjectionManager =
            context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        launcher.launch(captureIntent)
    }) {
        Text("Start Capture")
    }
    Button(onClick = {
        CaptureManager.stopCapture()
    }) {
        Text("Stop Capture")
    }
}

@Composable
fun FileNameInputView(fileName: () -> String, onChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            value = fileName(),
            onValueChange = onChange,
            placeholder = { Text(text = "Input File Name...") },
            label = {
                Text(
                    text = "File Name",
                    maxLines = 1
                )
            })
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "CurrentPath: ${Environment.getExternalStorageDirectory().absolutePath}/H264Encoder/${fileName()}.mp4",
            fontSize = 12.sp
        )
    }
}
