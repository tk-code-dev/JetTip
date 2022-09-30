package com.example.jettip.util


fun calculateTotalTip(totalBill: Double, tipPercentage: Int): Double {
    return if (totalBill > 0 && totalBill.toString().isNotEmpty())
        totalBill * tipPercentage / 100 else 0.0
}