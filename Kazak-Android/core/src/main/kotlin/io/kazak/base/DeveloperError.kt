package io.kazak.base

class DeveloperError : RuntimeException {

    constructor(cause: Throwable, message: String, vararg messageArgs: Any) : super(format(message, *messageArgs), cause)

    constructor(message: String, vararg messageArgs: Any) : super(format(message, *messageArgs))

    constructor(cause: Throwable) : super(cause)

    constructor() : super()

}

private fun format(format: String, vararg args: Any): String = java.lang.String.format(format, args)
