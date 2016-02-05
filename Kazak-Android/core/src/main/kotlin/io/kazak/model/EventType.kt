package io.kazak.model

enum class EventType {
    TALK,
    CEREMONY,
    COFFEE_BREAK,
    PLACEHOLDER;

    fun canBeFavorite(): Boolean {
        return when (this) {
            (TALK) -> true
            (CEREMONY) -> true
            else -> false
        }
    }
}
