package uk.co.droidcon.kazak.base

import uk.co.droidcon.kazak.assertThat
import uk.co.droidcon.kazak.base.safeTrim
import org.junit.Before as before
import org.junit.Test as test

public class StringUtilsTest {

    private val ANY_TRIMMED_STRING = "any trimmed string"
    private val ANY_TRIMMABLE_STRING = "   $ANY_TRIMMED_STRING "
    private val EMPTY_STRING = ""

    test
    fun trimmableStringIsTrimmed() {

        val trimmed = ANY_TRIMMABLE_STRING.safeTrim()

        assertThat(trimmed).isEqualTo(ANY_TRIMMED_STRING)
    }

    test
    fun nullStringIsConvertedToEmptyString() {

        val trimmed = null.safeTrim()

        assertThat(trimmed).isEqualTo(EMPTY_STRING)
    }

}
