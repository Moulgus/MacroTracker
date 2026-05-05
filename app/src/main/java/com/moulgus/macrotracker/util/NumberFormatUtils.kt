package com.moulgus.macrotracker.util

import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

fun Double.formatSmart(maxDecimals: Int = 1): String {
    val safeMaxDecimals = maxDecimals.coerceAtLeast(0)

    val multiplier = 10.0.pow(safeMaxDecimals)
    val rounded = (this * multiplier).roundToLong() / multiplier

    if (safeMaxDecimals == 0) {
        return rounded.roundToLong().toString()
    }

    val pattern = "%.${safeMaxDecimals}f"
    val formatted = pattern.format(Locale.US, rounded)

    return formatted
        .trimEnd('0')
        .trimEnd('.')
        .replace(".", ",")
}