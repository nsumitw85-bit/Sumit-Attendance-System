package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.DailySummary
import com.example.data.RateMode
import com.example.data.SalaryWageConfig
import com.example.data.WorkerAttendanceItem
import com.example.data.WorkerMonthlyStat
import com.example.data.WorkerSalaryComputation
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    // A4 dimensions at 72 dpi = 595 x 842 pt
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 30f

    /**
     * Generates a colorful Daily Attendance PDF
     */
    fun generateDailyAttendancePdf(
        context: Context,
        date: String,
        items: List<WorkerAttendanceItem>,
        summary: DailySummary
    ): File? {
        val document = PdfDocument()
        val itemsPerPage = 22
        val totalPages = maxOf(1, (items.size + itemsPerPage - 1) / itemsPerPage)

        try {
            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(items.size, startIndex + itemsPerPage)
                val pageItems = items.subList(startIndex, endIndex)

                drawBrandedHeader(
                    canvas = canvas,
                    reportTitle = "DAILY ATTENDANCE REPORT",
                    subTitle = "Date: $date | Generated on: ${currentTimestamp()}",
                    pageNumber = pageIndex + 1,
                    totalPages = totalPages
                )

                var currentY = 115f

                // Table Header
                currentY = drawDailyTableHeader(canvas, currentY)

                // Table Rows
                val paintText = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }

                val paintAltRow = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }

                val paintBorder = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 0.6f
                    style = Paint.Style.STROKE
                }

                pageItems.forEachIndexed { i, item ->
                    val rowY = currentY
                    val rowHeight = 22f
                    val globalIndex = startIndex + i + 1

                    if (i % 2 == 1) {
                        canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintAltRow)
                    }
                    canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintBorder)

                    // Columns: Sr | ID | Worker Name | Sanitation Role | Status
                    canvas.drawText("$globalIndex", MARGIN + 8, rowY + 15, paintText)
                    canvas.drawText(item.worker.workerCode, MARGIN + 35, rowY + 15, paintText)
                    
                    val trimmedName = if (item.worker.name.length > 22) item.worker.name.take(20) + ".." else item.worker.name
                    canvas.drawText(trimmedName, MARGIN + 105, rowY + 15, paintText)
                    
                    val trimmedRole = if (item.worker.roleCategory.length > 20) item.worker.roleCategory.take(18) + ".." else item.worker.roleCategory
                    canvas.drawText(trimmedRole, MARGIN + 260, rowY + 15, paintText)

                    // Status Badge
                    drawStatusBadge(canvas, item.status, PAGE_WIDTH - MARGIN - 65, rowY + 3, 55f, 16f)

                    currentY += rowHeight
                }

                // If last page, draw summary and signatures
                if (pageIndex == totalPages - 1) {
                    currentY += 12f
                    drawDailySummaryBox(canvas, currentY, summary)
                    currentY += 60f
                    drawSignatureSection(canvas, currentY)
                }

                document.finishPage(page)
            }

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "Daily_Attendance_${date.replace("-", "")}.pdf"
            val file = File(reportsDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            return file
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating daily PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    /**
     * Generates a colorful Monthly Attendance PDF
     */
    fun generateMonthlyAttendancePdf(
        context: Context,
        yearMonth: String,
        stats: List<WorkerMonthlyStat>
    ): File? {
        val document = PdfDocument()
        val itemsPerPage = 20
        val totalPages = maxOf(1, (stats.size + itemsPerPage - 1) / itemsPerPage)

        try {
            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(stats.size, startIndex + itemsPerPage)
                val pageItems = stats.subList(startIndex, endIndex)

                drawBrandedHeader(
                    canvas = canvas,
                    reportTitle = "MONTHLY ATTENDANCE MASTER SHEET",
                    subTitle = "Month: $yearMonth | Generated on: ${currentTimestamp()}",
                    pageNumber = pageIndex + 1,
                    totalPages = totalPages
                )

                var currentY = 115f

                // Table Header
                currentY = drawMonthlyAttendanceHeader(canvas, currentY)

                val paintText = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }

                val paintAltRow = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }

                val paintBorder = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 0.6f
                    style = Paint.Style.STROKE
                }

                pageItems.forEachIndexed { i, stat ->
                    val rowY = currentY
                    val rowHeight = 22f
                    val globalIndex = startIndex + i + 1

                    if (i % 2 == 1) {
                        canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintAltRow)
                    }
                    canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintBorder)

                    // Columns: Sr | Code | Worker Name | Role | P | A | H | D | Total Days
                    canvas.drawText("$globalIndex", MARGIN + 6, rowY + 15, paintText)
                    canvas.drawText(stat.worker.workerCode, MARGIN + 28, rowY + 15, paintText)

                    val trimmedName = if (stat.worker.name.length > 18) stat.worker.name.take(16) + ".." else stat.worker.name
                    canvas.drawText(trimmedName, MARGIN + 90, rowY + 15, paintText)

                    val trimmedRole = if (stat.worker.roleCategory.length > 15) stat.worker.roleCategory.take(13) + ".." else stat.worker.roleCategory
                    canvas.drawText(trimmedRole, MARGIN + 215, rowY + 15, paintText)

                    // Counters with pill colors
                    drawMiniBadge(canvas, "${stat.presentCount}", MARGIN + 310, rowY + 3, Color.rgb(46, 125, 50), Color.rgb(232, 245, 233))
                    drawMiniBadge(canvas, "${stat.absentCount}", MARGIN + 355, rowY + 3, Color.rgb(198, 40, 40), Color.rgb(255, 235, 238))
                    drawMiniBadge(canvas, "${stat.halfDayCount}", MARGIN + 400, rowY + 3, Color.rgb(239, 108, 0), Color.rgb(255, 243, 224))
                    drawMiniBadge(canvas, "${stat.doubleDutyCount}", MARGIN + 445, rowY + 3, Color.rgb(123, 31, 162), Color.rgb(243, 229, 245))

                    // Total Working Days
                    val daysText = String.format(Locale.ENGLISH, "%.1f", stat.calculatedWorkingDays)
                    val paintBold = Paint(paintText).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
                    canvas.drawText(daysText, MARGIN + 495, rowY + 15, paintBold)

                    currentY += rowHeight
                }

                if (pageIndex == totalPages - 1) {
                    currentY += 12f
                    drawMonthlySummaryBox(canvas, currentY, stats)
                    currentY += 55f
                    drawSignatureSection(canvas, currentY)
                }

                document.finishPage(page)
            }

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "Monthly_Attendance_${yearMonth.replace("-", "_")}.pdf"
            val file = File(reportsDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            return file
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating monthly attendance PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    /**
     * Generates a colorful Monthly Salary PDF with automatic calculations
     */
    fun generateMonthlySalaryPdf(
        context: Context,
        yearMonth: String,
        stats: List<WorkerMonthlyStat>,
        dailyWage: Double
    ): File? {
        val document = PdfDocument()
        val itemsPerPage = 20
        val totalPages = maxOf(1, (stats.size + itemsPerPage - 1) / itemsPerPage)

        try {
            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(stats.size, startIndex + itemsPerPage)
                val pageItems = stats.subList(startIndex, endIndex)

                drawBrandedHeader(
                    canvas = canvas,
                    reportTitle = "MONTHLY SALARY & WAGE STATEMENT",
                    subTitle = "Month: $yearMonth | Daily Wage Rate: ₹${dailyWage.toInt()} | Generated: ${currentTimestamp()}",
                    pageNumber = pageIndex + 1,
                    totalPages = totalPages
                )

                var currentY = 115f

                // Table Header
                currentY = drawSalaryTableHeader(canvas, currentY, dailyWage)

                val paintText = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }

                val paintAltRow = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }

                val paintBorder = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 0.6f
                    style = Paint.Style.STROKE
                }

                pageItems.forEachIndexed { i, stat ->
                    val rowY = currentY
                    val rowHeight = 22f
                    val globalIndex = startIndex + i + 1

                    if (i % 2 == 1) {
                        canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintAltRow)
                    }
                    canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintBorder)

                    // Columns: Sr | Code | Worker Name | Wage | P | A | H | D | Days | Net Salary (₹)
                    canvas.drawText("$globalIndex", MARGIN + 5, rowY + 15, paintText)
                    canvas.drawText(stat.worker.workerCode, MARGIN + 26, rowY + 15, paintText)

                    val trimmedName = if (stat.worker.name.length > 17) stat.worker.name.take(15) + ".." else stat.worker.name
                    canvas.drawText(trimmedName, MARGIN + 82, rowY + 15, paintText)

                    canvas.drawText("₹${stat.dailyWage.toInt()}", MARGIN + 195, rowY + 15, paintText)

                    // Mini Counts
                    canvas.drawText("${stat.presentCount}", MARGIN + 250, rowY + 15, paintText)
                    canvas.drawText("${stat.absentCount}", MARGIN + 285, rowY + 15, paintText)
                    canvas.drawText("${stat.halfDayCount}", MARGIN + 320, rowY + 15, paintText)
                    canvas.drawText("${stat.doubleDutyCount}", MARGIN + 355, rowY + 15, paintText)

                    val daysText = String.format(Locale.ENGLISH, "%.1f", stat.calculatedWorkingDays)
                    canvas.drawText(daysText, MARGIN + 395, rowY + 15, paintText)

                    // Final Net Salary in bold green pill
                    val salaryText = "₹" + String.format(Locale.ENGLISH, "%,d", stat.finalSalary.toInt())
                    drawSalaryBadge(canvas, salaryText, PAGE_WIDTH - MARGIN - 80, rowY + 2, 75f, 18f)

                    currentY += rowHeight
                }

                if (pageIndex == totalPages - 1) {
                    currentY += 12f
                    drawSalarySummaryBox(canvas, currentY, stats)
                    currentY += 60f
                    drawSignatureSection(canvas, currentY)
                }

                document.finishPage(page)
            }

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "Monthly_Salary_${yearMonth.replace("-", "_")}.pdf"
            val file = File(reportsDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            return file
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating monthly salary PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    /**
     * Generates a Comprehensive Configured Payroll PDF with configurable day/half-day rates,
     * role adjustments, advance deductions, bonuses, and net payable salaries.
     */
    fun generateConfiguredMonthlyPayrollPdf(
        context: Context,
        yearMonth: String,
        computations: List<WorkerSalaryComputation>,
        config: SalaryWageConfig
    ): File? {
        val document = PdfDocument()
        val itemsPerPage = 18
        val totalPages = maxOf(1, (computations.size + itemsPerPage - 1) / itemsPerPage)

        try {
            val halfDayRule = when (config.halfDayMode) {
                RateMode.MULTIPLIER -> "${(config.halfDayMultiplier * 100).toInt()}% (0.5x)"
                RateMode.FIXED -> "Fixed ₹${config.halfDayFixedRate.toInt()}"
            }
            val doubleRule = when (config.doubleDutyMode) {
                RateMode.MULTIPLIER -> "${(config.doubleDutyMultiplier * 100).toInt()}% (2.0x)"
                RateMode.FIXED -> "Fixed ₹${config.doubleDutyFixedRate.toInt()}"
            }

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                val startIndex = pageIndex * itemsPerPage
                val endIndex = minOf(computations.size, startIndex + itemsPerPage)
                val pageItems = computations.subList(startIndex, endIndex)

                drawBrandedHeader(
                    canvas = canvas,
                    reportTitle = "MONTHLY COMPREHENSIVE SALARY & PAYROLL STATEMENT",
                    subTitle = "Month: $yearMonth | Base Daily Rate: ₹${config.baseDailyWage.toInt()} | Half-Day: $halfDayRule | Double: $doubleRule",
                    pageNumber = pageIndex + 1,
                    totalPages = totalPages
                )

                var currentY = 115f
                currentY = drawConfiguredPayrollTableHeader(canvas, currentY)

                val paintText = Paint().apply {
                    color = Color.rgb(30, 41, 59)
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }

                val paintAltRow = Paint().apply {
                    color = Color.rgb(248, 250, 252)
                    style = Paint.Style.FILL
                }

                val paintBorder = Paint().apply {
                    color = Color.rgb(226, 232, 240)
                    strokeWidth = 0.6f
                    style = Paint.Style.STROKE
                }

                pageItems.forEachIndexed { i, comp ->
                    val rowY = currentY
                    val rowHeight = 24f
                    val globalIndex = startIndex + i + 1

                    if (i % 2 == 1) {
                        canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintAltRow)
                    }
                    canvas.drawRect(MARGIN, rowY, PAGE_WIDTH - MARGIN, rowY + rowHeight, paintBorder)

                    // Columns: Sr | Code | Worker Name | Day Rate | P/H/D | Gross | Adv (-) | Bonus (+) | Net Salary
                    canvas.drawText("$globalIndex", MARGIN + 4, rowY + 15, paintText)
                    canvas.drawText(comp.worker.workerCode, MARGIN + 22, rowY + 15, paintText)

                    val trimmedName = if (comp.worker.name.length > 15) comp.worker.name.take(13) + ".." else comp.worker.name
                    canvas.drawText(trimmedName, MARGIN + 68, rowY + 15, paintText)

                    canvas.drawText("₹${comp.appliedDailyWage.toInt()}", MARGIN + 160, rowY + 15, paintText)

                    // Attendance summary pill: e.g. "22P / 2H / 1D"
                    val attSummary = "${comp.presentCount}P/${comp.halfDayCount}H/${comp.doubleDutyCount}D"
                    canvas.drawText(attSummary, MARGIN + 208, rowY + 15, paintText)

                    // Gross
                    canvas.drawText("₹${comp.grossSalary.toInt()}", MARGIN + 280, rowY + 15, paintText)

                    // Advance
                    val advText = if (comp.advanceDeduction > 0) "-₹${comp.advanceDeduction.toInt()}" else "₹0"
                    val paintAdv = Paint(paintText).apply {
                        if (comp.advanceDeduction > 0) color = Color.rgb(198, 40, 40)
                    }
                    canvas.drawText(advText, MARGIN + 338, rowY + 15, paintAdv)

                    // Bonus
                    val bonusText = if (comp.bonusAllowance > 0) "+₹${comp.bonusAllowance.toInt()}" else "₹0"
                    val paintBonus = Paint(paintText).apply {
                        if (comp.bonusAllowance > 0) color = Color.rgb(46, 125, 50)
                    }
                    canvas.drawText(bonusText, MARGIN + 395, rowY + 15, paintBonus)

                    // Net Salary badge
                    val salaryText = "₹" + String.format(Locale.ENGLISH, "%,d", comp.netPayableSalary.toInt())
                    drawSalaryBadge(canvas, salaryText, PAGE_WIDTH - MARGIN - 80, rowY + 3, 75f, 18f)

                    currentY += rowHeight
                }

                if (pageIndex == totalPages - 1) {
                    currentY += 12f
                    drawConfiguredPayrollSummaryBox(canvas, currentY, computations)
                    currentY += 60f
                    drawSignatureSection(canvas, currentY)
                }

                document.finishPage(page)
            }

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val fileName = "Payroll_Report_${yearMonth.replace("-", "_")}.pdf"
            val file = File(reportsDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            return file
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating configured payroll PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    /**
     * Generates a formal Individual Worker Salary Slip PDF
     */
    fun generateWorkerSalarySlipPdf(
        context: Context,
        yearMonth: String,
        comp: WorkerSalaryComputation,
        config: SalaryWageConfig
    ): File? {
        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            drawBrandedHeader(
                canvas = canvas,
                reportTitle = "OFFICIAL SALARY & WAGE PAYSLIP (वेतन पावती)",
                subTitle = "Muster Month: $yearMonth | Generated on: ${currentTimestamp()}",
                pageNumber = 1,
                totalPages = 1
            )

            var currentY = 120f

            // 1. Worker Identity Card Box
            val idBoxHeight = 70f
            val paintBox = Paint().apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + idBoxHeight), 6f, 6f, paintBox)

            val paintBoxBorder = Paint().apply {
                color = Color.rgb(203, 213, 225)
                strokeWidth = 1f
                style = Paint.Style.STROKE
            }
            canvas.drawRoundRect(RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + idBoxHeight), 6f, 6f, paintBoxBorder)

            val paintLabel = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9f
                isAntiAlias = true
            }
            val paintValBold = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            // Left Col: Worker Code & Name
            canvas.drawText("Worker Code (आयडी):", MARGIN + 14, currentY + 22, paintLabel)
            canvas.drawText(comp.worker.workerCode, MARGIN + 14, currentY + 38, paintValBold)

            canvas.drawText("Full Name (पूर्ण नाव):", MARGIN + 14, currentY + 54, paintLabel)
            canvas.drawText(comp.worker.name, MARGIN + 14, currentY + 66, paintValBold)

            // Center Col: Role Category & Mobile
            val midX = MARGIN + 220f
            canvas.drawText("Sanitation Role (पद):", midX, currentY + 22, paintLabel)
            canvas.drawText(comp.worker.roleCategory, midX, currentY + 38, paintValBold)

            canvas.drawText("Contact Mobile (मोबाईल):", midX, currentY + 54, paintLabel)
            val phone = if (comp.worker.phone.isNotBlank()) comp.worker.phone else "Not registered"
            canvas.drawText(phone, midX, currentY + 66, paintValBold)

            // Right Col: Payment Status Badge
            val rightX = PAGE_WIDTH - MARGIN - 110f
            canvas.drawText("Payment Status:", rightX, currentY + 22, paintLabel)
            val statusColor = if (comp.paymentStatus.startsWith("PAID")) Color.rgb(46, 125, 50) else Color.rgb(239, 108, 0)
            val statusBg = if (comp.paymentStatus.startsWith("PAID")) Color.rgb(232, 245, 233) else Color.rgb(255, 243, 224)
            drawMiniBadge(canvas, comp.paymentStatus.replace("_", " "), rightX, currentY + 30, statusColor, statusBg)

            currentY += idBoxHeight + 20f

            // 2. Attendance Summary Grid
            val paintSectionHeader = Paint().apply {
                color = Color.rgb(21, 101, 192)
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("1. Monthly Attendance Summary (हजेरी तपशील)", MARGIN, currentY, paintSectionHeader)
            currentY += 8f

            val attGridHeight = 46f
            canvas.drawRoundRect(RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + attGridHeight), 4f, 4f, paintBox)
            canvas.drawRoundRect(RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + attGridHeight), 4f, 4f, paintBoxBorder)

            val colW = (PAGE_WIDTH - 2 * MARGIN) / 5f
            // Present (P)
            drawStatCell(canvas, "Present (P)", "${comp.presentCount} Days", MARGIN + 10, currentY, Color.rgb(46, 125, 50))
            // Half Day (H)
            drawStatCell(canvas, "Half Day (H)", "${comp.halfDayCount} Days", MARGIN + colW + 10, currentY, Color.rgb(239, 108, 0))
            // Double Duty (D)
            drawStatCell(canvas, "Double (D)", "${comp.doubleDutyCount} Days", MARGIN + colW * 2 + 10, currentY, Color.rgb(123, 31, 162))
            // Absent (A)
            drawStatCell(canvas, "Absent (A)", "${comp.absentCount} Days", MARGIN + colW * 3 + 10, currentY, Color.rgb(198, 40, 40))
            // Net Calculated Man-Days
            val daysText = String.format(Locale.ENGLISH, "%.1f Days", comp.calculatedManDays)
            drawStatCell(canvas, "Total Man-Days", daysText, MARGIN + colW * 4 + 10, currentY, Color.rgb(21, 101, 192))

            currentY += attGridHeight + 24f

            // 3. Earnings & Deductions Dual Table
            canvas.drawText("2. Wage Calculation & Salary Structure (पगार तपशील)", MARGIN, currentY, paintSectionHeader)
            currentY += 10f

            val halfTableW = (PAGE_WIDTH - 2 * MARGIN - 12f) / 2f
            val earningsX = MARGIN
            val deductionsX = MARGIN + halfTableW + 12f

            // Earnings Table Header (Green)
            val paintEarningHeader = Paint().apply { color = Color.rgb(46, 125, 50); style = Paint.Style.FILL }
            canvas.drawRoundRect(RectF(earningsX, currentY, earningsX + halfTableW, currentY + 22f), 4f, 4f, paintEarningHeader)
            val paintHeaderWhite = Paint().apply { color = Color.WHITE; textSize = 9.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            canvas.drawText("Earnings (जमा)", earningsX + 10, currentY + 15, paintHeaderWhite)
            canvas.drawText("Amount (₹)", earningsX + halfTableW - 65, currentY + 15, paintHeaderWhite)

            // Deductions Table Header (Red)
            val paintDeductHeader = Paint().apply { color = Color.rgb(198, 40, 40); style = Paint.Style.FILL }
            canvas.drawRoundRect(RectF(deductionsX, currentY, deductionsX + halfTableW, currentY + 22f), 4f, 4f, paintDeductHeader)
            canvas.drawText("Deductions (कपात)", deductionsX + 10, currentY + 15, paintHeaderWhite)
            canvas.drawText("Amount (₹)", deductionsX + halfTableW - 65, currentY + 15, paintHeaderWhite)

            var earnRowY = currentY + 22f
            var deductRowY = currentY + 22f
            val itemRowH = 20f

            // Earnings rows
            earnRowY = drawSlipRow(canvas, "Full Day Base Wages (${comp.presentCount} × ₹${comp.appliedDailyWage.toInt()})", "₹${comp.presentEarnings.toInt()}", earningsX, earnRowY, halfTableW, itemRowH)
            earnRowY = drawSlipRow(canvas, "Half-Day Wages (${comp.halfDayCount} × ₹${comp.appliedHalfDayWage.toInt()})", "₹${comp.halfDayEarnings.toInt()}", earningsX, earnRowY, halfTableW, itemRowH)
            earnRowY = drawSlipRow(canvas, "Double Shift Wages (${comp.doubleDutyCount} × ₹${comp.appliedDoubleDutyWage.toInt()})", "₹${comp.doubleDutyEarnings.toInt()}", earningsX, earnRowY, halfTableW, itemRowH)
            if (comp.bonusAllowance > 0) {
                earnRowY = drawSlipRow(canvas, "Incentive / Bonus (भत्ता)", "+₹${comp.bonusAllowance.toInt()}", earningsX, earnRowY, halfTableW, itemRowH)
            }

            // Deductions rows
            deductRowY = drawSlipRow(canvas, "Monthly Advance / Loan (उचल)", "₹${comp.advanceDeduction.toInt()}", deductionsX, deductRowY, halfTableW, itemRowH)
            deductRowY = drawSlipRow(canvas, "Other Deductions (इतर कपात)", "₹0", deductionsX, deductRowY, halfTableW, itemRowH)

            // Total Gross & Total Deductions Footers
            val maxRowY = maxOf(earnRowY, deductRowY) + 4f

            // Total Gross Box
            val paintGrossBg = Paint().apply { color = Color.rgb(232, 245, 233); style = Paint.Style.FILL }
            canvas.drawRoundRect(RectF(earningsX, maxRowY, earningsX + halfTableW, maxRowY + 24f), 4f, 4f, paintGrossBg)
            val paintGrossText = Paint().apply { color = Color.rgb(46, 125, 50); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            canvas.drawText("Total Gross Earnings (A):", earningsX + 10, maxRowY + 16, paintGrossText)
            val grossFormatted = "₹" + String.format(Locale.ENGLISH, "%,d", (comp.grossSalary + comp.bonusAllowance).toInt())
            canvas.drawText(grossFormatted, earningsX + halfTableW - 75, maxRowY + 16, paintGrossText)

            // Total Deductions Box
            val paintDeductBg = Paint().apply { color = Color.rgb(255, 235, 238); style = Paint.Style.FILL }
            canvas.drawRoundRect(RectF(deductionsX, maxRowY, deductionsX + halfTableW, maxRowY + 24f), 4f, 4f, paintDeductBg)
            val paintDeductText = Paint().apply { color = Color.rgb(198, 40, 40); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
            canvas.drawText("Total Deductions (B):", deductionsX + 10, maxRowY + 16, paintDeductText)
            val deductFormatted = "₹" + String.format(Locale.ENGLISH, "%,d", comp.advanceDeduction.toInt())
            canvas.drawText(deductFormatted, deductionsX + halfTableW - 75, maxRowY + 16, paintDeductText)

            currentY = maxRowY + 36f

            // 4. Net Payable Salary Big Callout Box
            val netBoxHeight = 60f
            val paintNetBox = Paint().apply { color = Color.rgb(21, 101, 192); style = Paint.Style.FILL }
            canvas.drawRoundRect(RectF(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + netBoxHeight), 8f, 8f, paintNetBox)

            val paintNetLabel = Paint().apply { color = Color.rgb(227, 242, 253); textSize = 11f; isAntiAlias = true }
            val paintNetFormula = Paint().apply { color = Color.rgb(187, 222, 251); textSize = 9f; isAntiAlias = true }
            val paintNetVal = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            canvas.drawText("NET PAYABLE SALARY (अंतिम देय पगार)", MARGIN + 18, currentY + 24, paintNetLabel)
            canvas.drawText("Formula: Net Pay = Gross Earnings (A) - Total Deductions (B)", MARGIN + 18, currentY + 44, paintNetFormula)

            val netFormatted = "₹" + String.format(Locale.ENGLISH, "%,d", comp.netPayableSalary.toInt())
            val netMeasure = paintNetVal.measureText(netFormatted)
            canvas.drawText(netFormatted, PAGE_WIDTH - MARGIN - 20 - netMeasure, currentY + 38, paintNetVal)

            currentY += netBoxHeight + 35f

            // 5. Signature Section
            drawSignatureSection(canvas, currentY)

            document.finishPage(page)

            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val cleanName = comp.worker.name.replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val fileName = "SalarySlip_${yearMonth.replace("-", "_")}_${cleanName}.pdf"
            val file = File(reportsDir, fileName)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            return file
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error generating salary slip PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    private fun drawStatCell(canvas: Canvas, label: String, value: String, x: Float, y: Float, valColor: Int) {
        val paintL = Paint().apply { color = Color.rgb(100, 116, 139); textSize = 8f; isAntiAlias = true }
        val paintV = Paint().apply { color = valColor; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }
        canvas.drawText(label, x, y + 16f, paintL)
        canvas.drawText(value, x, y + 34f, paintV)
    }

    private fun drawSlipRow(canvas: Canvas, label: String, amount: String, x: Float, y: Float, width: Float, height: Float): Float {
        val paintRow = Paint().apply { color = Color.rgb(248, 250, 252); style = Paint.Style.FILL }
        canvas.drawRect(x, y, x + width, y + height, paintRow)

        val paintBorder = Paint().apply { color = Color.rgb(226, 232, 240); strokeWidth = 0.5f; style = Paint.Style.STROKE }
        canvas.drawRect(x, y, x + width, y + height, paintBorder)

        val paintText = Paint().apply { color = Color.rgb(30, 41, 59); textSize = 8.5f; isAntiAlias = true }
        val paintVal = Paint().apply { color = Color.rgb(15, 23, 42); textSize = 8.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }

        val trimmed = if (label.length > 28) label.take(26) + ".." else label
        canvas.drawText(trimmed, x + 8, y + 14f, paintText)
        canvas.drawText(amount, x + width - 65, y + 14f, paintVal)

        return y + height
    }

    private fun drawConfiguredPayrollTableHeader(canvas: Canvas, y: Float): Float {
        val height = 24f
        val paintBg = Paint().apply {
            color = Color.rgb(21, 101, 192)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height), 4f, 4f, paintBg)

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Sr", MARGIN + 4, y + 16, paintText)
        canvas.drawText("ID", MARGIN + 22, y + 16, paintText)
        canvas.drawText("Worker Name", MARGIN + 68, y + 16, paintText)
        canvas.drawText("Day Rate", MARGIN + 160, y + 16, paintText)
        canvas.drawText("P/H/D", MARGIN + 208, y + 16, paintText)
        canvas.drawText("Gross", MARGIN + 280, y + 16, paintText)
        canvas.drawText("Adv (-)", MARGIN + 338, y + 16, paintText)
        canvas.drawText("Bonus (+)", MARGIN + 395, y + 16, paintText)
        canvas.drawText("Net Salary", PAGE_WIDTH - MARGIN - 72, y + 16, paintText)

        return y + height
    }

    private fun drawConfiguredPayrollSummaryBox(canvas: Canvas, y: Float, list: List<WorkerSalaryComputation>) {
        val boxHeight = 44f
        val paintBox = Paint().apply { color = Color.rgb(241, 245, 249); style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight), 6f, 6f, paintBox)

        val paintBorder = Paint().apply { color = Color.rgb(203, 213, 225); strokeWidth = 1f; style = Paint.Style.STROKE }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight), 6f, 6f, paintBorder)

        val totalGross = list.sumOf { it.grossSalary }
        val totalAdv = list.sumOf { it.advanceDeduction }
        val totalBonus = list.sumOf { it.bonusAllowance }
        val totalNet = list.sumOf { it.netPayableSalary }
        val totalManDays = list.sumOf { it.calculatedManDays }

        val paintLabel = Paint().apply { color = Color.rgb(71, 85, 105); textSize = 8f; isAntiAlias = true }
        val paintVal = Paint().apply { textSize = 10.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); isAntiAlias = true }

        val colW = (PAGE_WIDTH - 2 * MARGIN) / 5f

        // Total Staff & Man-days
        paintVal.color = Color.rgb(15, 23, 42)
        canvas.drawText("Total Staff / Days", MARGIN + 8, y + 16, paintLabel)
        val staffDaysText = "${list.size} / " + String.format(Locale.ENGLISH, "%.1f d", totalManDays)
        canvas.drawText(staffDaysText, MARGIN + 8, y + 33, paintVal)

        // Total Gross
        paintVal.color = Color.rgb(21, 101, 192)
        canvas.drawText("Total Gross Wages", MARGIN + colW + 8, y + 16, paintLabel)
        val grossText = "₹" + String.format(Locale.ENGLISH, "%,d", totalGross.toInt())
        canvas.drawText(grossText, MARGIN + colW + 8, y + 33, paintVal)

        // Total Advances
        paintVal.color = Color.rgb(198, 40, 40)
        canvas.drawText("Advances Deducted", MARGIN + colW * 2 + 8, y + 16, paintLabel)
        val advText = "₹" + String.format(Locale.ENGLISH, "%,d", totalAdv.toInt())
        canvas.drawText(advText, MARGIN + colW * 2 + 8, y + 33, paintVal)

        // Total Bonuses
        paintVal.color = Color.rgb(46, 125, 50)
        canvas.drawText("Bonus / Allowances", MARGIN + colW * 3 + 8, y + 16, paintLabel)
        val bonusText = "₹" + String.format(Locale.ENGLISH, "%,d", totalBonus.toInt())
        canvas.drawText(bonusText, MARGIN + colW * 3 + 8, y + 33, paintVal)

        // Total Net Payout
        paintVal.color = Color.rgb(46, 125, 50)
        canvas.drawText("Net Total Payout", MARGIN + colW * 4 + 8, y + 16, paintLabel)
        val netText = "₹" + String.format(Locale.ENGLISH, "%,d", totalNet.toInt())
        canvas.drawText(netText, MARGIN + colW * 4 + 8, y + 33, paintVal)
    }

    // Helper UI Elements for PDF

    private fun drawBrandedHeader(
        canvas: Canvas,
        reportTitle: String,
        subTitle: String,
        pageNumber: Int,
        totalPages: Int
    ) {
        // Decorative top banner bar
        val paintTopBarBlue = Paint().apply { color = Color.rgb(21, 101, 192) }   // Sumit Blue
        val paintTopBarGreen = Paint().apply { color = Color.rgb(46, 125, 50) }   // Attendance Green
        val paintTopBarOrange = Paint().apply { color = Color.rgb(239, 108, 0) }  // System Orange

        canvas.drawRect(MARGIN, 18f, MARGIN + 180f, 22f, paintTopBarBlue)
        canvas.drawRect(MARGIN + 180f, 18f, MARGIN + 360f, 22f, paintTopBarGreen)
        canvas.drawRect(MARGIN + 360f, 18f, PAGE_WIDTH - MARGIN, 22f, paintTopBarOrange)

        // Draw Official Ashoka Chakra Gold S Emblem on Top Left
        val logoCenterX = MARGIN + 22f
        val logoCenterY = 56f
        val logoRadius = 22f
        drawAshokaChakraGoldEmblem(canvas, logoCenterX, logoCenterY, logoRadius)

        // Branding Wordmark: Sumit (Blue) Attendance (Green) System (Orange)
        val textStartX = MARGIN + 52f
        val paintBrandBlue = Paint().apply {
            color = Color.rgb(21, 101, 192)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintBrandGreen = Paint().apply {
            color = Color.rgb(46, 125, 50)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintBrandOrange = Paint().apply {
            color = Color.rgb(239, 108, 0)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var brandX = textStartX
        canvas.drawText("Sumit ", brandX, 44f, paintBrandBlue)
        brandX += paintBrandBlue.measureText("Sumit ")
        canvas.drawText("Attendance ", brandX, 44f, paintBrandGreen)
        brandX += paintBrandGreen.measureText("Attendance ")
        canvas.drawText("System", brandX, 44f, paintBrandOrange)

        // Subhead: Sanitation Department
        val paintDept = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Sanitation Department Management System (स्वच्छता विभाग)", textStartX, 58f, paintDept)

        // Report Title Badge
        val paintTitle = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(reportTitle, textStartX, 75f, paintTitle)

        // Subtitle & Page Indicator
        val paintSub = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText(subTitle, MARGIN, 98f, paintSub)

        val pageText = "Page $pageNumber of $totalPages"
        val pageWidthMeasure = paintSub.measureText(pageText)
        canvas.drawText(pageText, PAGE_WIDTH - MARGIN - pageWidthMeasure, 98f, paintSub)

        // Divider
        val paintDivider = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }
        canvas.drawLine(MARGIN, 106f, PAGE_WIDTH - MARGIN, 106f, paintDivider)
    }

    /**
     * Draws the circular Ashoka Chakra Gold "S" logo directly onto the PDF Canvas
     */
    private fun drawAshokaChakraGoldEmblem(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        // 1. Midnight Navy Circular Disk
        val bgPaint = Paint().apply {
            color = Color.rgb(11, 25, 44) // #0B192C
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // 2. Outer Gold Rim
        val rimPaint = Paint().apply {
            color = Color.rgb(255, 213, 79) // Gold #FFD54F
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, radius - 0.6f, rimPaint)

        // 3. Ashoka Chakra 24 Spokes & Concentric Rings
        val chakraRadius = radius * 0.76f
        val hubRadius = radius * 0.38f

        val ringPaint = Paint().apply {
            color = Color.rgb(212, 175, 55) // #D4AF37
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, chakraRadius, ringPaint)
        canvas.drawCircle(cx, cy, hubRadius, ringPaint)

        val spokePaint = Paint().apply {
            color = Color.argb(160, 255, 224, 130)
            strokeWidth = 0.5f
            isAntiAlias = true
        }

        for (i in 0 until 24) {
            val angleRad = (i * 360.0 / 24.0) * (Math.PI / 180.0)
            val startX = (cx + hubRadius * Math.cos(angleRad)).toFloat()
            val startY = (cy + hubRadius * Math.sin(angleRad)).toFloat()
            val endX = (cx + chakraRadius * Math.cos(angleRad)).toFloat()
            val endY = (cy + chakraRadius * Math.sin(angleRad)).toFloat()
            canvas.drawLine(startX, startY, endX, endY, spokePaint)
        }

        // 4. Center Gold "S"
        val sPaint = Paint().apply {
            color = Color.rgb(255, 224, 130) // #FFE082
            textSize = radius * 1.05f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        // Center text vertically
        val textY = cy - (sPaint.descent() + sPaint.ascent()) / 2f
        canvas.drawText("S", cx, textY, sPaint)
    }

    private fun drawDailyTableHeader(canvas: Canvas, y: Float): Float {
        val height = 24f
        val paintBg = Paint().apply {
            color = Color.rgb(21, 101, 192) // Branded Deep Blue
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height), 4f, 4f, paintBg)

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Sr.", MARGIN + 8, y + 16, paintText)
        canvas.drawText("Worker ID", MARGIN + 35, y + 16, paintText)
        canvas.drawText("Worker Full Name", MARGIN + 105, y + 16, paintText)
        canvas.drawText("Sanitation Role", MARGIN + 260, y + 16, paintText)
        canvas.drawText("Status", PAGE_WIDTH - MARGIN - 60, y + 16, paintText)

        return y + height
    }

    private fun drawMonthlyAttendanceHeader(canvas: Canvas, y: Float): Float {
        val height = 24f
        val paintBg = Paint().apply {
            color = Color.rgb(46, 125, 50) // Branded Green
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height), 4f, 4f, paintBg)

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Sr", MARGIN + 6, y + 16, paintText)
        canvas.drawText("ID", MARGIN + 28, y + 16, paintText)
        canvas.drawText("Worker Name", MARGIN + 90, y + 16, paintText)
        canvas.drawText("Role", MARGIN + 215, y + 16, paintText)
        canvas.drawText("P (1)", MARGIN + 315, y + 16, paintText)
        canvas.drawText("A (0)", MARGIN + 360, y + 16, paintText)
        canvas.drawText("H (.5)", MARGIN + 403, y + 16, paintText)
        canvas.drawText("D (2)", MARGIN + 448, y + 16, paintText)
        canvas.drawText("Days", MARGIN + 495, y + 16, paintText)

        return y + height
    }

    private fun drawSalaryTableHeader(canvas: Canvas, y: Float, dailyWage: Double): Float {
        val height = 24f
        val paintBg = Paint().apply {
            color = Color.rgb(21, 101, 192) // Branded Deep Blue
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + height), 4f, 4f, paintBg)

        val paintText = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Sr", MARGIN + 5, y + 16, paintText)
        canvas.drawText("ID", MARGIN + 26, y + 16, paintText)
        canvas.drawText("Worker Name", MARGIN + 82, y + 16, paintText)
        canvas.drawText("Wage", MARGIN + 195, y + 16, paintText)
        canvas.drawText("P", MARGIN + 252, y + 16, paintText)
        canvas.drawText("A", MARGIN + 287, y + 16, paintText)
        canvas.drawText("H", MARGIN + 322, y + 16, paintText)
        canvas.drawText("D", MARGIN + 357, y + 16, paintText)
        canvas.drawText("Days", MARGIN + 395, y + 16, paintText)
        canvas.drawText("Net Salary", PAGE_WIDTH - MARGIN - 72, y + 16, paintText)

        return y + height
    }

    private fun drawStatusBadge(canvas: Canvas, status: String, x: Float, y: Float, w: Float, h: Float) {
        val (bgColor, textColor, text) = when (status) {
            "P" -> Triple(Color.rgb(232, 245, 233), Color.rgb(46, 125, 50), "PRESENT (P)")
            "A" -> Triple(Color.rgb(255, 235, 238), Color.rgb(198, 40, 40), "ABSENT (A)")
            "H" -> Triple(Color.rgb(255, 243, 224), Color.rgb(239, 108, 0), "HALF (H)")
            "D" -> Triple(Color.rgb(243, 229, 245), Color.rgb(123, 31, 162), "DOUBLE (D)")
            else -> Triple(Color.rgb(241, 245, 249), Color.rgb(100, 116, 139), "NOT SET")
        }

        val paintBg = Paint().apply { color = bgColor; style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 3f, 3f, paintBg)

        val paintText = Paint().apply {
            color = textColor
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textWidth = paintText.measureText(text)
        canvas.drawText(text, x + (w - textWidth) / 2, y + 11f, paintText)
    }

    private fun drawMiniBadge(canvas: Canvas, text: String, x: Float, y: Float, textColor: Int, bgColor: Int) {
        val w = 32f
        val h = 16f
        val paintBg = Paint().apply { color = bgColor; style = Paint.Style.FILL }
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 3f, 3f, paintBg)

        val paintText = Paint().apply {
            color = textColor
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textWidth = paintText.measureText(text)
        canvas.drawText(text, x + (w - textWidth) / 2, y + 12f, paintText)
    }

    private fun drawSalaryBadge(canvas: Canvas, text: String, x: Float, y: Float, w: Float, h: Float) {
        val paintBg = Paint().apply {
            color = Color.rgb(232, 245, 233)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 4f, 4f, paintBg)

        val paintText = Paint().apply {
            color = Color.rgb(46, 125, 50)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textWidth = paintText.measureText(text)
        canvas.drawText(text, x + (w - textWidth) / 2, y + 13f, paintText)
    }

    private fun drawDailySummaryBox(canvas: Canvas, y: Float, summary: DailySummary) {
        val boxHeight = 44f
        val paintBox = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight), 6f, 6f, paintBox)

        val paintBorder = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight), 6f, 6f, paintBorder)

        val paintLabel = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            isAntiAlias = true
        }
        val paintVal = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // 5 Summary items
        val itemW = (PAGE_WIDTH - 2 * MARGIN) / 5

        // 1. Total Workers
        paintVal.color = Color.rgb(15, 23, 42)
        canvas.drawText("Total Workers", MARGIN + 12, y + 16, paintLabel)
        canvas.drawText("${summary.totalWorkers}", MARGIN + 12, y + 34, paintVal)

        // 2. Present
        paintVal.color = Color.rgb(46, 125, 50)
        canvas.drawText("Present (P)", MARGIN + itemW + 12, y + 16, paintLabel)
        canvas.drawText("${summary.presentCount}", MARGIN + itemW + 12, y + 34, paintVal)

        // 3. Absent
        paintVal.color = Color.rgb(198, 40, 40)
        canvas.drawText("Absent (A)", MARGIN + itemW * 2 + 12, y + 16, paintLabel)
        canvas.drawText("${summary.absentCount}", MARGIN + itemW * 2 + 12, y + 34, paintVal)

        // 4. Half Day
        paintVal.color = Color.rgb(239, 108, 0)
        canvas.drawText("Half Day (H)", MARGIN + itemW * 3 + 12, y + 16, paintLabel)
        canvas.drawText("${summary.halfDayCount}", MARGIN + itemW * 3 + 12, y + 34, paintVal)

        // 5. Double Duty
        paintVal.color = Color.rgb(123, 31, 162)
        canvas.drawText("Double Duty (D)", MARGIN + itemW * 4 + 12, y + 16, paintLabel)
        canvas.drawText("${summary.doubleDutyCount}", MARGIN + itemW * 4 + 12, y + 34, paintVal)
    }

    private fun drawMonthlySummaryBox(canvas: Canvas, y: Float, stats: List<WorkerMonthlyStat>) {
        val totalWorkers = stats.size
        val totalP = stats.sumOf { it.presentCount }
        val totalA = stats.sumOf { it.absentCount }
        val totalH = stats.sumOf { it.halfDayCount }
        val totalD = stats.sumOf { it.doubleDutyCount }
        val totalWorkingDays = stats.sumOf { it.calculatedWorkingDays }

        val summary = DailySummary(
            date = "",
            totalWorkers = totalWorkers,
            presentCount = totalP,
            absentCount = totalA,
            halfDayCount = totalH,
            doubleDutyCount = totalD,
            totalWorkingDays = totalWorkingDays,
            estimatedSalary = 0.0
        )
        drawDailySummaryBox(canvas, y, summary)
    }

    private fun drawSalarySummaryBox(canvas: Canvas, y: Float, stats: List<WorkerMonthlyStat>) {
        val boxHeight = 46f
        val paintBox = Paint().apply {
            color = Color.rgb(232, 245, 233)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight), 6f, 6f, paintBox)

        val paintBorder = Paint().apply {
            color = Color.rgb(165, 214, 167)
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight), 6f, 6f, paintBorder)

        val totalWorkers = stats.size
        val totalP = stats.sumOf { it.presentCount }
        val grandTotalSalary = stats.sumOf { it.finalSalary }

        val paintLabel = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 8.5f
            isAntiAlias = true
        }
        val paintVal = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Total Staff", MARGIN + 14, y + 16, paintLabel)
        canvas.drawText("$totalWorkers Workers", MARGIN + 14, y + 34, paintVal)

        canvas.drawText("Total Present Days", MARGIN + 130, y + 16, paintLabel)
        canvas.drawText("$totalP Days", MARGIN + 130, y + 34, paintVal)

        // Grand Total Amount Badge
        val paintGrandLabel = Paint().apply {
            color = Color.rgb(27, 94, 32)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintGrandVal = Paint().apply {
            color = Color.rgb(46, 125, 50)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val grandSalaryFormatted = "₹" + String.format(Locale.ENGLISH, "%,d", grandTotalSalary.toInt())
        canvas.drawText("GRAND TOTAL SALARY:", MARGIN + 280, y + 18, paintGrandLabel)
        canvas.drawText(grandSalaryFormatted, MARGIN + 280, y + 37, paintGrandVal)
    }

    private fun drawSignatureSection(canvas: Canvas, y: Float) {
        val paintLine = Paint().apply {
            color = Color.rgb(148, 163, 184)
            strokeWidth = 0.8f
        }
        val paintText = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintSub = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
            isAntiAlias = true
        }

        // Supervisor Signature (Left)
        canvas.drawLine(MARGIN + 20, y + 25, MARGIN + 180, y + 25, paintLine)
        canvas.drawText("Sanitation Supervisor / Mukadam", MARGIN + 20, y + 38, paintText)
        canvas.drawText("Signature & Stamp", MARGIN + 20, y + 48, paintSub)

        // Sanitary Inspector / Officer Signature (Right)
        canvas.drawLine(PAGE_WIDTH - MARGIN - 180, y + 25, PAGE_WIDTH - MARGIN - 20, y + 25, paintLine)
        canvas.drawText("Sanitary Inspector / In-Charge Officer", PAGE_WIDTH - MARGIN - 180, y + 38, paintText)
        canvas.drawText("Verified & Approved", PAGE_WIDTH - MARGIN - 180, y + 48, paintSub)
    }

    private fun currentTimestamp(): String {
        return SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
    }

    /**
     * Checks if WhatsApp or WhatsApp Business is installed on the device.
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            try {
                pm.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    /**
     * Returns the package name for WhatsApp if installed, prioritizing standard WhatsApp over WhatsApp Business.
     */
    fun getWhatsAppPackage(context: Context): String? {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo("com.whatsapp", 0)
            "com.whatsapp"
        } catch (e: Exception) {
            try {
                pm.getPackageInfo("com.whatsapp.w4b", 0)
                "com.whatsapp.w4b"
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Share PDF directly on WhatsApp.
     * If WhatsApp is installed, opens WhatsApp directly with the PDF attached.
     * Completely bypasses Android Share sheet, Gmail, Drive, etc.
     * If WhatsApp is not installed, shows "WhatsApp is not installed on this device."
     */
    fun sharePdfToWhatsApp(context: Context, file: File, title: String) {
        val targetPackage = getWhatsAppPackage(context)
        if (targetPackage == null) {
            val lang = "en"
            Toast.makeText(context, "WhatsApp is not installed on this device.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\nGenerated from Sumit Attendance System.")
                `package` = targetPackage
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Grant read permission explicitly to WhatsApp target package as well
            try {
                context.grantUriPermission(
                    targetPackage,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // ignore
            }

            // Direct start - no chooser!
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error opening WhatsApp directly with PDF", e)
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share PDF via standard Android share sheet
     */
    fun sharePdf(context: Context, file: File, title: String, viaWhatsAppOnly: Boolean = false) {
        if (viaWhatsAppOnly) {
            sharePdfToWhatsApp(context, file, title)
            return
        }

        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\nGenerated from Sumit Attendance System.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("PdfGenerator", "Error sharing PDF", e)
            Toast.makeText(context, "Error sharing report: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * View or Download PDF in external viewer
     */
    fun viewPdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Open / Download PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "Opening ${file.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF saved to: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }
}
