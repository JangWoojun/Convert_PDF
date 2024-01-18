package com.woojun.convertpdf

import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.woojun.convertpdf.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_PERMISSION = 1
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.imageView.setImageURI(uri)
        } else {
            Toast.makeText(this@MainActivity, "오류 다시 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            Toast.makeText(this@MainActivity, "화면을 클릭해서 PDF를 다운로드 해보세요", Toast.LENGTH_SHORT).show()

            layoutToPrint.post {
                layoutToPrint.setOnClickListener {
                    checkPermissionAndSave()
                }
            }

            selectImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }

    private fun checkPermissionAndSave() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_PERMISSION
            )
        } else {
            binding.apply {
                Toast.makeText(this@MainActivity, "다운로드", Toast.LENGTH_SHORT).show()
                convertLayoutToPDF(layoutToPrint)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            binding.apply {
                Toast.makeText(this@MainActivity, "다운로드", Toast.LENGTH_SHORT).show()
                convertLayoutToPDF(layoutToPrint)
            }
        } else {
            Toast.makeText(this, "권한을 거부하면 사용하실 수 없습니다", Toast.LENGTH_SHORT).show()
        }
    }


    private fun convertLayoutToPDF(view: View) {
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("res1", PRINT_SERVICE, 300, 300))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        val pdfDocument = PrintedPdfDocument(this, printAttributes)
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        view.draw(page.canvas)
        pdfDocument.finishPage(page)

        val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "myPDF.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}