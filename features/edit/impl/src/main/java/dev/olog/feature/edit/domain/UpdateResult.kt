package dev.olog.feature.edit.domain

enum class UpdateResult{
    OK,
    EMPTY_TITLE,
    ILLEGAL_YEAR,
    ILLEGAL_DISC_NUMBER,
    ILLEGAL_TRACK_NUMBER
}