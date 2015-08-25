package io.kazak.base

import java.lang

public class DeveloperError : RuntimeException {

    public constructor(cause: Throwable, message: String, vararg messageArgs: Any) : super(String.format(message, *messageArgs), cause)

    public constructor(message: String, vararg messageArgs: Any) : super(String.format(message, *messageArgs))

    public constructor(cause: Throwable) : super(cause)

    public constructor() : super()

}

@suppress("NOTHING_TO_INLINE")
private inline fun String.Companion.format(format: String, vararg args: Any) = lang.String.format(format, args)
