package com.example.buddybloom.data.model
enum class TimeInterval(val milliseconds: Long) {
    MINUTE(1000 * 60),      // 1 minute
    HOUR(1000 * 60 * 60),   // 1 hour
    DAY(1000 * 60 * 60 * 24) // 1 day
}