package io.kazak.base

import io.kazak.assertThat
import org.junit.Test

public class StringUtilsTest {

    private val ANY_TRIMMED_STRING = "any trimmed string"
    private val ANY_TRIMMABLE_STRING = "   $ANY_TRIMMED_STRING "
    private val EMPTY_STRING = ""

    @Test
    fun trimmableStringIsTrimmed() {

        val trimmed = ANY_TRIMMABLE_STRING.safeTrim()

        assertThat(trimmed).isEqualTo(ANY_TRIMMED_STRING)
    }

    @Test
    fun nullStringIsConvertedToEmptyString() {

        val trimmed = null.safeTrim()

        assertThat(trimmed).isEqualTo(EMPTY_STRING)
    }

}
