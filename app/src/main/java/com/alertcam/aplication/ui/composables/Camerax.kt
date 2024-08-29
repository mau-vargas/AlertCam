package com.alertcam.aplication.ui.composables

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.util.concurrent.Executors


@Composable
fun CameraPreviewScreen() {
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        CameraPreviewWithAnalysis()
    }
}








/*
fun test (){
    val model = Model.newInstance(context)

// Creates inputs for reference.
    val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 64, 64, 3), DataType.FLOAT32)
    inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
    val outputs = model.process(inputFeature0)
    val outputFeature0 = outputs.outputFeature0AsTensorBuffer

// Releases model resources if no longer used.
    model.close()

}*/


//###########################
@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithAnalysis() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var predictions by remember { mutableStateOf<FloatArray?>(null) }


    // State for capturing image
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build()

        val imageAnalyzer = ImageAnalysis.Builder().setTargetResolution(Size(1280, 720)).build()

        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
            // Process image here
            val mediaImage = imageProxy
            mediaImage.let { image ->
                val bitmap = mediaImage.toBitmap() // Convert Image to Bitmap
                imageBitmap = bitmap.asImageBitmap() // Update the state
            }

            // Preprocess image and perform inference
            //val imageByteBuffer = preprocessImage(mediaImage.toBitmap())
            //predictions = loadModelAndPredict(context, imageByteBuffer)
            //classifyImage(mediaImage.toBitmap(),classifyImageTf)

            imageProxy.close() // Close the image proxy when done
        }
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            context as ComponentActivity, cameraSelector, preview, imageAnalyzer
        )
    }

    // Display the camera preview
    Column(modifier = Modifier.padding(20.dp)) {
        AndroidView(
            { previewView }, modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        )
       imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
                    .graphicsLayer(
                        rotationZ = 90f // Ángulo de rotación en grados
                    )
            )
        }
        predictions?.let {
            ShowResults(predictions = it)
        }
    }
}

@Composable
fun ShowResults(predictions: FloatArray) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Predictions:")
        predictions.forEachIndexed { index, prediction ->
            Text("Class $index: $prediction")
        }
    }
}
