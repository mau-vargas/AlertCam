package com.alertcam.aplication.tflite

interface ReturnInterpreter {

    fun classify(confidence:FloatArray,maxConfidence:Int)

}