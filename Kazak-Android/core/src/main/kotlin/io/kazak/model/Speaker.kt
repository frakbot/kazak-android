package io.kazak.model

import java.net.URI

public data class Speaker(
        val id: String,
        val name: String,
        val company: String?,
        val bio: String?,
        val pic: URI?,
        val social: List<String>?
)
