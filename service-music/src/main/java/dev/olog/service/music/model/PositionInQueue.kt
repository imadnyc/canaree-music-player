package dev.olog.service.music.model

internal enum class PositionInQueue {
    /**
     * Has only next songs
     */
    FIRST,
    /**
     * Has previous and next songs
     */
    IN_MIDDLE, // has previous and next songs
    /**
     * Has only previous songs
     */
    LAST,
    /**
     * Is the only track the list, has no previous or next songs
     */
    FIRST_AND_LAST
}
