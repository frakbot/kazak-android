package io.kazak

import org.fest.assertions.api.Assertions
import org.fest.assertions.api.ObjectAssert
import org.mockito.Mockito

fun <T> verify(mock: T): T {
    return Mockito.verify(mock)
}

fun <T> assertThat(actual: T): ObjectAssert<T> {
    return Assertions.assertThat(actual)
}
