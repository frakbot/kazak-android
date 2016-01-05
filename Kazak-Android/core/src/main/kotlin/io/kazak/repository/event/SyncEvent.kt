package io.kazak.repository.event

data class SyncEvent(val state: SyncState, val error: Throwable?)
