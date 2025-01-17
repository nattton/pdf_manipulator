package com.ttbbank.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import androidx.core.net.toUri
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// For removing pages from pdf.
suspend fun getPDFPageDeleter(
    sourceFilePath: String,
    pageNumbers: List<Int>,
    context: Activity,
): String? {

    val resultPDFPath: String?

    withContext(Dispatchers.IO) {

        val utils = Utils()

        val begin = System.nanoTime()

        val contentResolver: ContentResolver = context.contentResolver

        val uri = Utils().getURI(sourceFilePath)

        val pdfReaderFile: File = File.createTempFile("readerTempFile", ".pdf")
        utils.copyDataFromSourceToDestDocument(
            sourceFileUri = uri,
            destinationFileUri = pdfReaderFile.toUri(),
            contentResolver = contentResolver
        )

        val pdfReader = PdfReader(pdfReaderFile).setUnethicalReading(true)
        pdfReader.setMemorySavingMode(true)

        val pdfWriterFile: File = File.createTempFile("writerTempFile", ".pdf")

        val pdfWriter = PdfWriter(pdfWriterFile)

        pdfWriter.setSmartMode(true)
        pdfWriter.compressionLevel = 9

        val pdfDocument = PdfDocument(pdfReader, pdfWriter)

        //Important to use descending as we remove a page the number of pages in your PDF will change
        val descendingListOfPageNumbers = pageNumbers.sortedDescending()

        for (pageNumber in descendingListOfPageNumbers) {
            pdfDocument.removePage(pageNumber)
        }

        pdfDocument.close()

        pdfReader.close()
        pdfWriter.close()

        utils.deleteTempFiles(listOfTempFiles = listOf(pdfReaderFile))

        val end = System.nanoTime()
        println("Elapsed time in nanoseconds: ${end - begin}")

        resultPDFPath = pdfWriterFile.path
    }

    return resultPDFPath
}