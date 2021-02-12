package org.divineuk.divineapp.util

import java.util.*

object MusicUtil  {

    fun getReadableDurationString(songDurationMillis: Long): String? {
        var minutes = songDurationMillis / 1000 / 60
        val seconds = songDurationMillis / 1000 % 60
        return if (minutes < 60) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
            )
        } else {
            val hours = minutes / 60
            minutes = minutes % 60
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        }
    }

}