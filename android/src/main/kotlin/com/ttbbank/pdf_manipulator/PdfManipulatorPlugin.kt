package com.ttbbank.pdf_manipulator

import android.util.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** PdfManipulatorPlugin */
class PdfManipulatorPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private var pdfManipulator: PdfManipulator? = null
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var activityBinding: ActivityPluginBinding? = null

    companion object {
        const val LOG_TAG = "PdfManipulatorPlugin"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(LOG_TAG, "onAttachedToEngine - IN")

        if (pluginBinding != null) {
            Log.w(LOG_TAG, "onAttachedToEngine - already attached")
        }

        pluginBinding = flutterPluginBinding

        val messenger = pluginBinding?.binaryMessenger
        doOnAttachedToEngine(messenger!!)

        Log.d(LOG_TAG, "onAttachedToEngine - OUT")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(LOG_TAG, "onDetachedFromEngine")
        doOnDetachedFromEngine()
    }

    // note: this may be called multiple times on app startup
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.d(LOG_TAG, "onAttachedToActivity")
        doOnAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        Log.d(LOG_TAG, "onDetachedFromActivity")
        doOnDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.d(LOG_TAG, "onReattachedToActivityForConfigChanges")
        doOnAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.d(LOG_TAG, "onDetachedFromActivityForConfigChanges")
        doOnDetachedFromActivity()
    }

    private fun doOnAttachedToEngine(messenger: BinaryMessenger) {
        Log.d(LOG_TAG, "doOnAttachedToEngine - IN")

        this.channel = MethodChannel(messenger, "pdf_manipulator")
        this.channel.setMethodCallHandler(this)

        Log.d(LOG_TAG, "doOnAttachedToEngine - OUT")
    }

    private fun doOnDetachedFromEngine() {
        Log.d(LOG_TAG, "doOnDetachedFromEngine - IN")

        if (pluginBinding == null) {
            Log.w(LOG_TAG, "doOnDetachedFromEngine - already detached")
        }
        pluginBinding = null

        this.channel.setMethodCallHandler(null)

        Log.d(LOG_TAG, "doOnDetachedFromEngine - OUT")
    }

    private fun doOnAttachedToActivity(activityBinding: ActivityPluginBinding?) {
        Log.d(LOG_TAG, "doOnAttachedToActivity - IN")

        this.activityBinding = activityBinding

        Log.d(LOG_TAG, "doOnAttachedToActivity - OUT")
    }

    private fun doOnDetachedFromActivity() {
        Log.d(LOG_TAG, "doOnDetachedFromActivity - IN")

        if (pdfManipulator != null) {
            pdfManipulator = null
        }
        activityBinding = null

        Log.d(LOG_TAG, "doOnDetachedFromActivity - OUT")
    }


    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.d(LOG_TAG, "onMethodCall - IN , method=${call.method}")
        if (pdfManipulator == null) {
            if (!createPickOrSave()) {
                result.error("init_failed", "Not attached", null)
                return
            }
        }
        when (call.method) {
            "mergePDFs" -> pdfManipulator!!.mergePdfs(
                result,
                sourceFilesPaths = parseMethodCallArrayOfStringArgument(
                    call, "pdfsPaths"
                ),
            )
            "splitPDF" -> pdfManipulator!!.splitPdf(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageCount = call.argument("pageCount") ?: 1,
                byteSize = call.argument("byteSize"),
                pageNumbers = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbers"
                ),
                pageRanges = parseMethodCallArrayOfStringArgument(
                    call, "pageRanges"
                ),
                pageRange = call.argument("pageRange"),
            )
            "pdfPageDeleter" -> pdfManipulator!!.pdfPageDeleter(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageNumbers = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbers"
                ),
            )
            "pdfPageReorder" -> pdfManipulator!!.pdfPageReorder(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageNumbers = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbers"
                ),
            )
            "pdfPageRotator" -> pdfManipulator!!.pdfPageRotator(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pagesRotationInfo = parseMethodCallArrayOfMapArgument(
                    call, "pagesRotationInfo"
                ),
            )
            "pdfPageRotatorDeleterReorder" -> pdfManipulator!!.pdfPageRotatorDeleterReorder(
                result,
                sourceFilePath = call.argument("pdfPath"),
                pageNumbersForReorder = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbersForReorder"
                ) ?: listOf(),
                pageNumbersForDeleter = parseMethodCallArrayOfIntArgument(
                    call, "pageNumbersForDeleter"
                ) ?: listOf(),
                pagesRotationInfo = parseMethodCallArrayOfMapArgument(
                    call, "pagesRotationInfo"
                ) ?: listOf(),
            )
            "pdfCompressor" -> pdfManipulator!!.pdfCompressor(
                result,
                sourceFilePath = call.argument("pdfPath"),
                imageQuality = call.argument("imageQuality"),
                imageScale = call.argument("imageScale"),
                unEmbedFonts = call.argument("unEmbedFonts"),
            )
            "pdfWatermark" -> pdfManipulator!!.watermarkPdf(
                result,
                sourceFilePath = call.argument("pdfPath"),
                text = call.argument("text"),
                fontSize = call.argument("fontSize"),
                watermarkLayer = parseMethodCallWatermarkLayerTypeArgument(call)
                    ?: WatermarkLayer.OverContent,
                opacity = call.argument("opacity"),
                rotationAngle = call.argument("rotationAngle"),
                watermarkColor = call.argument("watermarkColor"),
                positionType = parseMethodCallWatermarkPositionTypeArgument(call)
                    ?: PositionType.Center,
                customPositionXCoordinatesList = parseMethodCallArrayOfDoubleArgument(
                    call, "customPositionXCoordinatesList"
                ),
                customPositionYCoordinatesList = parseMethodCallArrayOfDoubleArgument(
                    call, "customPositionYCoordinatesList"
                ),
            )
            "pdfPagesSize" -> pdfManipulator!!.pdfPagesSize(
                result,
                sourceFilePath = call.argument("pdfPath"),
            )
            "pdfValidityAndProtection" -> pdfManipulator!!.pdfValidityAndProtection(
                result,
                sourceFilePath = call.argument("pdfPath"),
                userOrOwnerPassword = call.argument("password") ?: "",
            )
            "pdfDecryption" -> pdfManipulator!!.pdfDecryption(
                result,
                sourceFilePath = call.argument("pdfPath"),
                userOrOwnerPassword = call.argument("password") ?: "",
            )
            "pdfEncryption" -> pdfManipulator!!.pdfEncryption(
                result,
                sourceFilePath = call.argument("pdfPath"),
                ownerPassword = call.argument("ownerPassword") ?: "",
                userPassword = call.argument("userPassword") ?: "",
                allowPrinting = call.argument("allowPrinting") ?: false,
                allowModifyContents = call.argument("allowModifyContents") ?: false,
                allowCopy = call.argument("allowCopy") ?: false,
                allowModifyAnnotations = call.argument("allowModifyAnnotations") ?: false,
                allowFillIn = call.argument("allowFillIn") ?: false,
                allowScreenReaders = call.argument("allowScreenReaders") ?: false,
                allowAssembly = call.argument("allowAssembly") ?: false,
                allowDegradedPrinting = call.argument("allowDegradedPrinting") ?: false,
                standardEncryptionAES40 = call.argument("standardEncryptionAES40") ?: false,
                standardEncryptionAES128 = call.argument("standardEncryptionAES128") ?: false,
                encryptionAES128 = call.argument("encryptionAES128") ?: false,
                encryptEmbeddedFilesOnly = call.argument("encryptEmbeddedFilesOnly") ?: false,
                doNotEncryptMetadata = call.argument("doNotEncryptMetadata") ?: false,
            )
            "imagesToPdfs" -> pdfManipulator!!.imagesToPdfs(
                result,
                sourceImagesPaths = parseMethodCallArrayOfStringArgument(
                    call, "imagesPaths"
                ),
                createSinglePdf = call.argument("createSinglePdf"),
            )
            "cancelManipulations" -> pdfManipulator!!.cancelManipulations()
            else -> result.notImplemented()
        }
    }

    private fun createPickOrSave(): Boolean {
        Log.d(LOG_TAG, "createPickOrSave - IN")

        var pdfManipulator: PdfManipulator? = null
        if (activityBinding != null) {
            pdfManipulator = PdfManipulator(
                activity = activityBinding!!.activity
            )
        }
        this.pdfManipulator = pdfManipulator

        Log.d(LOG_TAG, "createPickOrSave - OUT")

        return pdfManipulator != null
    }

    private fun parseMethodCallArrayOfStringArgument(
        call: MethodCall, arg: String
    ): List<String>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<String>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfIntArgument(
        call: MethodCall, arg: String
    ): List<Int>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Int>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfDoubleArgument(
        call: MethodCall, arg: String
    ): List<Double>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Double>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallArrayOfMapArgument(
        call: MethodCall, arg: String
    ): List<Map<String, Int>>? {
        if (call.hasArgument(arg)) {
            return call.argument<ArrayList<Map<String, Int>>>(arg)?.toList()
        }
        return null
    }

    private fun parseMethodCallWatermarkLayerTypeArgument(call: MethodCall): WatermarkLayer? {
        val arg = "watermarkLayer"
        if (call.hasArgument(arg)) {
            return if (call.argument<String>(arg)?.toString() == "WatermarkLayer.underContent") {
                WatermarkLayer.UnderContent
            } else {
                WatermarkLayer.OverContent
            }
        }
        return null
    }

    private fun parseMethodCallWatermarkPositionTypeArgument(call: MethodCall): PositionType? {
        val arg = "positionType"

        if (call.hasArgument(arg)) {
            return if (call.argument<String>(arg)?.toString() == "PositionType.topLeft") {
                PositionType.TopLeft
            } else if (call.argument<String>(arg)?.toString() == "PositionType.topCenter") {
                PositionType.TopCenter
            } else if (call.argument<String>(arg)?.toString() == "PositionType.topRight") {
                PositionType.TopRight
            } else if (call.argument<String>(arg)?.toString() == "PositionType.centerLeft") {
                PositionType.CenterLeft
            } else if (call.argument<String>(arg)?.toString() == "PositionType.center") {
                PositionType.Center
            } else if (call.argument<String>(arg)?.toString() == "PositionType.centerRight") {
                PositionType.CenterRight
            } else if (call.argument<String>(arg)?.toString() == "PositionType.bottomLeft") {
                PositionType.BottomLeft
            } else if (call.argument<String>(arg)?.toString() == "PositionType.bottomCenter") {
                PositionType.BottomCenter
            } else if (call.argument<String>(arg)?.toString() == "PositionType.bottomRight") {
                PositionType.BottomRight
            } else {
                PositionType.Custom
            }
        }
        return null
    }
}