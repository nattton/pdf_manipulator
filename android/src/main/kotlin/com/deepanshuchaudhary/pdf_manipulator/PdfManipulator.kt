package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val LOG_TAG = "PdfManipulator"

class PdfManipulator(
    private val activity: Activity
) {

    private var pendingResult: MethodChannel.Result? = null

    private var job: Job? = null

    // For merging multiple pdf files.
    fun mergePdfs(
        result: MethodChannel.Result,
        sourceFilesPaths: List<String>?,
    ) {
        Log.d(
            LOG_TAG,
            "mergePdfs - IN, sourceFilesPaths=$sourceFilesPaths"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val mergedPDFPath: String? = getMergedPDFPath(sourceFilesPaths!!, activity)

                finishSuccessfullyWithString(mergedPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "mergePdfs_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "mergePdfs_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "mergePdfs - OUT")
    }

    // For merging multiple pdf files.
    fun splitPdf(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        pageCount: Int,
        byteSize: Number?,
        pageNumbers: List<Int>?,
        pageRanges: List<String>?,
        pageRange: String?,
    ) {
        Log.d(
            LOG_TAG,
            "splitPdf - IN, sourceFilePath=$sourceFilePath, pageCount=$pageCount, byteSize=$byteSize, pageNumbers=$pageNumbers, pageRanges=$pageRanges, pageRange=$pageRange"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val splitPDFPaths: List<String>? = if (byteSize != null) {
                    getSplitPDFPathsByByteSize(
                        sourceFilePath!!,
                        byteSize.toLong(), activity
                    )
                } else if (pageNumbers != null) {
                    getSplitPDFPathsByPageNumbers(sourceFilePath!!, pageNumbers, activity)
                } else if (pageRanges != null) {
                    getSplitPDFPathsByPageRanges(sourceFilePath!!, pageRanges, activity)
                } else if (pageRange != null) {
                    getSplitPDFPathsByPageRange(sourceFilePath!!, pageRange, activity)
                } else {
                    getSplitPDFPathsByPageCount(sourceFilePath!!, pageCount, activity)
                }
                finishSplitSuccessfullyWithListOfString(splitPDFPaths)
            } catch (e: Exception) {
                finishWithError(
                    "splitPdf_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "splitPdf_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "splitPdf - OUT")
    }

    // For removing pages from pdf.
    fun pdfPageDeleter(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        pageNumbers: List<Int>?,
    ) {
        Log.d(
            LOG_TAG,
            "removePdfPages - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? =
                    getPDFPageDeleter(sourceFilePath!!, pageNumbers!!, activity)

                finishSuccessfullyWithString(resultPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "removePdfPages_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "removePdfPages_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "removePdfPages - OUT")
    }

    // For reordering pages of pdf.
    fun pdfPageReorder(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        pageNumbers: List<Int>?,
    ) {
        Log.d(
            LOG_TAG,
            "pdfPageReorder - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? =
                    getPDFPageReorder(sourceFilePath!!, pageNumbers!!, activity)

                finishSuccessfullyWithString(resultPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "pdfPageReorder_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "pdfPageReorder_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "pdfPageReorder - OUT")
    }

    // For rotating pages of pdf.
    fun pdfPageRotator(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        pagesRotationInfo: List<Map<String, Int>>?,
    ) {
        Log.d(
            LOG_TAG,
            "pdfPageRotator - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val newPagesRotationInfo: MutableList<PageRotationInfo> = mutableListOf()

                pagesRotationInfo!!.forEach {
                    val temp = PageRotationInfo(
                        pageNumber = it["pageNumber"]!!,
                        rotationAngle = it["rotationAngle"]!!
                    )
                    newPagesRotationInfo.add(temp)
                }

                val resultPDFPath: String? =
                    getPDFPageRotator(sourceFilePath!!, newPagesRotationInfo, activity)

                finishSuccessfullyWithString(resultPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "pdfPageRotator_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "pdfPageRotator_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "pdfPageRotator - OUT")
    }

    // For reordering, deleting, rotating pages of pdf.
    fun pdfPageRotatorDeleterReorder(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        pageNumbersForReorder: List<Int>,
        pageNumbersForDeleter: List<Int>,
        pagesRotationInfo: List<Map<String, Int>>,
    ) {
        Log.d(
            LOG_TAG,
            "pdfPageRotatorDeleterReorder - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val newPagesRotationInfo: MutableList<PageRotationInfo> = mutableListOf()

                pagesRotationInfo.forEach {
                    val temp = PageRotationInfo(
                        pageNumber = it["pageNumber"]!!,
                        rotationAngle = it["rotationAngle"]!!
                    )
                    newPagesRotationInfo.add(temp)
                }

                val resultPDFPath: String? =
                    getPDFPageRotatorDeleterReorder(
                        sourceFilePath!!,
                        pageNumbersForReorder,
                        pageNumbersForDeleter,
                        newPagesRotationInfo,
                        activity
                    )

                finishSuccessfullyWithString(resultPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "pdfPageRotatorDeleterReorder_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "pdfPageRotatorDeleterReorder_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "pdfPageRotatorDeleterReorder - OUT")
    }


    // For compressing pdf.
    fun pdfCompressor(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        imageQuality: Int?,
        imageScale: Double?,
        unEmbedFonts: Boolean?,
    ) {
        Log.d(
            LOG_TAG,
            "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? =
                    getCompressedPDFPath(
                        sourceFilePath!!,
                        imageQuality!!,
                        imageScale!!,
                        unEmbedFonts!!,
                        activity
                    )

                finishSuccessfullyWithString(resultPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "pdfCompressor_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "pdfCompressor_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "pdfCompressor - OUT")
    }

    // For compressing pdf.
    fun watermarkPdf(
        result: MethodChannel.Result,
        sourceFilePath: String?,
        text: String?,
        fontSize: Double?,
        watermarkLayer: WatermarkLayer?,
        opacity: Double?,
        rotationAngle: Double?,
        watermarkColor: String?,
        positionType: PositionType?,
        customPositionXCoordinatesList: List<Double>?,
        customPositionYCoordinatesList: List<Double>?,
    ) {
        Log.d(
            LOG_TAG,
            "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val resultPDFPath: String? =
                    getWatermarkedPDFPath(
                        sourceFilePath!!,
                        text!!,
                        fontSize!!,
                        watermarkLayer!!,
                        opacity!!,
                        rotationAngle!!,
                        watermarkColor!!,
                        positionType!!,
                        customPositionXCoordinatesList ?: listOf(),
                        customPositionYCoordinatesList ?: listOf(),
                        activity
                    )

                finishSuccessfullyWithString(resultPDFPath)
            } catch (e: Exception) {
                finishWithError(
                    "pdfCompressor_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "pdfCompressor_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "pdfCompressor - OUT")
    }

    // For pdf pages size.
    fun pdfPagesSize(
        result: MethodChannel.Result,
        sourceFilePath: String?,
    ) {
        Log.d(
            LOG_TAG,
            "pdfCompressor - IN, sourceFilePath=$sourceFilePath"
        )

        if (!setPendingResult(result)) {
            finishWithAlreadyActiveError(result)
            return
        }

        val uiScope = CoroutineScope(Dispatchers.Main)
        job = uiScope.launch {
            try {
                val result: List<List<Double>> =
                    getPDFPagesSize(
                        sourceFilePath!!,
                        activity
                    )
                if (result.isEmpty()) {
                    finishSplitSuccessfullyWithListOfListOfDouble(null)
                } else {
                    finishSplitSuccessfullyWithListOfListOfDouble(result)
                }
            } catch (e: Exception) {
                finishWithError(
                    "pdfCompressor_exception",
                    e.stackTraceToString(),
                    null
                )
            } catch (e: OutOfMemoryError) {
                finishWithError(
                    "pdfCompressor_OutOfMemoryError",
                    e.stackTraceToString(),
                    null
                )
            }
        }
        Log.d(LOG_TAG, "pdfCompressor - OUT")
    }

    fun cancelManipulations(
    ) {
        job?.cancel()
//        finishSuccessfully(null)
//        clearPendingResult()
        Log.d(LOG_TAG, "Canceled Manipulations")
    }

    private fun setPendingResult(
        result: MethodChannel.Result
    ): Boolean {
//        if (pendingResult != null) {
//            return false
//        }
        pendingResult = result
        return true
    }

    private fun finishWithAlreadyActiveError(result: MethodChannel.Result) {
        result.error("already_active", "Merging is already active", null)
    }

    private fun clearPendingResult() {
        pendingResult = null
    }

    private fun finishSuccessfullyWithString(result: String?) {
        pendingResult?.success(result)
        clearPendingResult()
    }

    private fun finishSplitSuccessfullyWithListOfString(result: List<String>?) {
        pendingResult?.success(result)
        clearPendingResult()
    }

    private fun finishSplitSuccessfullyWithListOfListOfDouble(result: List<List<Double>>?) {
        pendingResult?.success(result)
        clearPendingResult()
    }

    private fun finishWithError(errorCode: String, errorMessage: String?, errorDetails: String?) {
        pendingResult?.error(errorCode, errorMessage, errorDetails)
        clearPendingResult()
    }
}
