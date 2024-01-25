# Screen Recording Module using H.264 and MediaProjection
<img src="https://github.com/lyh990517/Android_H264_Encoder/assets/45873564/8f068289-de82-4486-b236-e167c5bb0266" width="400" height="800">
<img src="https://github.com/lyh990517/Android_H264_Encoder/assets/45873564/2fc0283b-e519-4a3e-950a-f60028004396" width="400" height="800">


## Overview
This module provides a screen recording solution for Android using H.264 encoding and MediaProjection. It allows you to easily capture the device's screen and encode it into H.264 video format. Your support is greatly appreciated, so please consider giving us a  ⭐star⭐ if you find this module useful!

## How to Use
To use the Screen Recording Module, follow these steps:

1. Add the module as a dependency in your Android project.

2. Use the `H264EncoderCapture` interface to start and stop screen recording.

## Methods

1. `startCapture(filePath: String, code: Int, data: Intent, context: Context)`: Initiates the screen recording process with the provided parameters.
   - `filePath`: A string representing the file path where the recorded video will be saved.
   - `code`: An integer obtained from the MediaProjection indicating the permission to capture the screen.
   - `data`: An `Intent` obtained from the MediaProjection with additional screen capture data.
   - `context`: The Android application context.

2. `stopCapture()`: Stops the ongoing screen recording session, finalizing the recorded video.

This interface simplifies the process of integrating screen recording capabilities into your Android app using H.264 encoding and MediaProjection. It allows you to initiate and terminate screen recording with ease, making it a valuable tool for various applications.

## Customization
You have the freedom to customize and tailor the screen recording module to your specific requirements. Feel free to modify the recording settings, file paths, or any other aspects of the module to best fit your application.

## Usage Example
Here's an example of how to use the `H264EncoderCapture` interface to start screen recording:

```kotlin
// Start screen recording
val filePath = "your_file_path_here.mp4"
val code = // Obtain code from MediaProjection
val data = // Obtain ScreenCaptureIntent from MediaProjection

H264EncoderCapture.startCapture(filePath, code, data, context)

// Stop screen recording
H264EncoderCapture.stopCapture()
```
Please refer to the detailed usage code in the project :)
