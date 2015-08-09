package uk.co.droidcon.kazak.repository

data class SyncEvent(val state : SyncState, val error : Throwable?)
