package io.kazak.base

fun String?.safeTrim(): String {
    return this?.trim() ?: ""
}
