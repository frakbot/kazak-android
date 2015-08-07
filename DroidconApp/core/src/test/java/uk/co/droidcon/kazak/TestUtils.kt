package uk.co.droidcon.kazak

import org.fest.assertions.api.Assertions
import org.fest.assertions.api.ObjectAssert
import org.mockito.Mockito

fun verify<T>(mock: T): T {
    return Mockito.verify(mock)
}

fun assertThat<T>(actual: T): ObjectAssert<T> {
    return Assertions.assertThat(actual)
}
