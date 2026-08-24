package com.example.data

import com.example.data.model.WorkerEntity

enum class RateMode {
    MULTIPLIER,
    FIXED
}

data class SalaryWageConfig(
    val baseDailyWage: Double = 300.0,
    val halfDayMode: RateMode = RateMode.MULTIPLIER,
    val halfDayMultiplier: Double = 0.5,
    val halfDayFixedRate: Double = 150.0,
    val doubleDutyMode: RateMode = RateMode.MULTIPLIER,
    val doubleDutyMultiplier: Double = 2.0,
    val doubleDutyFixedRate: Double = 600.0,
    val useRoleBasedRates: Boolean = false,
    val roleRates: Map<String, Double> = defaultRoleRates()
) {
    fun calculateDayRate(roleCategory: String): Double {
        return if (useRoleBasedRates) {
            roleRates[roleCategory] ?: baseDailyWage
        } else {
            baseDailyWage
        }
    }

    fun calculateHalfDayRate(roleCategory: String): Double {
        val dayRate = calculateDayRate(roleCategory)
        return when (halfDayMode) {
            RateMode.MULTIPLIER -> dayRate * halfDayMultiplier
            RateMode.FIXED -> halfDayFixedRate
        }
    }

    fun calculateDoubleDutyRate(roleCategory: String): Double {
        val dayRate = calculateDayRate(roleCategory)
        return when (doubleDutyMode) {
            RateMode.MULTIPLIER -> dayRate * doubleDutyMultiplier
            RateMode.FIXED -> doubleDutyFixedRate
        }
    }

    companion object {
        fun defaultRoleRates(): Map<String, Double> = mapOf(
            "Broom Worker" to 300.0,
            "Drain Cleaning Worker" to 350.0,
            "Garbage Vehicle Driver" to 450.0,
            "Garbage Vehicle Helper" to 320.0,
            "Road Sweeper" to 300.0,
            "Public Toilet Cleaner" to 350.0,
            "Supervisor / Mukadam" to 550.0,
            "Other Sanitation Staff" to 300.0
        )
    }
}

data class WorkerPayrollAdjustment(
    val workerId: Int,
    val month: String,
    val customDailyWageOverride: Double? = null,
    val advanceDeduction: Double = 0.0,
    val bonusAllowance: Double = 0.0,
    val paymentStatus: String = "PENDING", // PENDING, PAID_CASH, PAID_BANK, PAID_UPI
    val paymentNotes: String = ""
)

data class WorkerSalaryComputation(
    val worker: WorkerEntity,
    val presentCount: Int,
    val absentCount: Int,
    val halfDayCount: Int,
    val doubleDutyCount: Int,
    val calculatedManDays: Double,
    val appliedDailyWage: Double,
    val appliedHalfDayWage: Double,
    val appliedDoubleDutyWage: Double,
    val presentEarnings: Double,
    val halfDayEarnings: Double,
    val doubleDutyEarnings: Double,
    val grossSalary: Double,
    val advanceDeduction: Double,
    val bonusAllowance: Double,
    val netPayableSalary: Double,
    val paymentStatus: String,
    val paymentNotes: String
)

data class PayrollMonthSummary(
    val month: String,
    val totalWorkers: Int,
    val totalManDays: Double,
    val totalGrossSalary: Double,
    val totalAdvances: Double,
    val totalBonuses: Double,
    val totalNetPayable: Double,
    val paidCount: Int,
    val pendingCount: Int
)
