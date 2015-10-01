package io.kazak.base

public class DeveloperError : RuntimeException {

    public constructor(cause: Throwable, message: String, vararg messageArgs: Any) : super(format(message, *messageArgs), cause)

    public constructor(message: String, vararg messageArgs: Any) : super(format(message, *messageArgs))

    public constructor(cause: Throwable) : super(cause)

    public constructor() : super()

}

private fun format(format: String, vararg args: Any): String = java.lang.String.format(format, args)
