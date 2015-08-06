package uk.co.droidcon.kazak.base

fun String?.safeTrim() : String {
    return this?.trim() ?: ""
}
