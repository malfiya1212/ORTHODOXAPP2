package com.example.orthodoxapp.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.orthodoxapp.data.model.Income
import com.example.orthodoxapp.data.model.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportExportUtility {

    fun exportToPdf(context: Context, income: List<Income>, expenses: List<Expense>, title: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Header
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Ethiopian Orthodox Tewahedo Church", 50f, 50f, paint)
        
        paint.textSize = 16f
        canvas.drawText(title, 50f, 80f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 50f, 100f, paint)

        // Summary
        val totalIncome = income.sumOf { it.amount }
        val totalExpense = expenses.sumOf { it.amount }
        canvas.drawText("Total Income: ETB ${String.format("%,.2f", totalIncome)}", 50f, 140f, paint)
        canvas.drawText("Total Expenses: ETB ${String.format("%,.2f", totalExpense)}", 50f, 160f, paint)
        canvas.drawText("Net Balance: ETB ${String.format("%,.2f", totalIncome - totalExpense)}", 50f, 180f, paint)

        // Table Header
        paint.isFakeBoldText = true
        canvas.drawText("Recent Transactions", 50f, 220f, paint)
        canvas.drawLine(50f, 225f, 545f, 225f, paint)
        
        var y = 250f
        paint.isFakeBoldText = false
        
        // List combined records
        (income.take(10) + expenses.take(10)).forEach { record ->
            if (y > 800) return@forEach // Simple page break protection
            val desc = if (record is Income) record.source else (record as Expense).category
            val amount = if (record is Income) "+${record.amount}" else "-${(record as Expense).amount}"
            canvas.drawText(desc, 50f, y, paint)
            canvas.drawText(amount, 450f, y, paint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Church_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF Saved: ${file.name}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun exportToExcel(context: Context, income: List<Income>, expenses: List<Expense>, title: String) {
        val fileName = "Church_Report_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        try {
            val writer = FileOutputStream(file).bufferedWriter()
            writer.write("Ethiopian Orthodox Tewahedo Church - Financial Report\n")
            writer.write("Report Type:,$title\n")
            writer.write("Generated At:,${Date()}\n\n")
            
            writer.write("TYPE,CATEGORY/SOURCE,AMOUNT,STATUS,DATE\n")
            
            income.forEach { 
                writer.write("INCOME,${it.source},${it.amount},${it.status},${Date(it.date)}\n")
            }
            
            expenses.forEach { 
                writer.write("EXPENSE,${it.category},${it.amount},${it.status},${Date(it.date)}\n")
            }
            
            writer.close()
            Toast.makeText(context, "Excel (CSV) Saved: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving Excel", Toast.LENGTH_SHORT).show()
        }
    }
}
