package com.example.microdustwidget.data.models.airpollution

import androidx.annotation.ColorRes
import com.example.microdustwidget.R
import com.google.gson.annotations.SerializedName

enum class Grade(val label: String, val emoji: String, @ColorRes val colorResId: Int) {
    @SerializedName("1")
    GOOD("좋음", "\uD83D\uDE04", R.color.good),
    @SerializedName("2")
    NORMAL("보통", "\uD83D\uDE10", R.color.normal),
    @SerializedName("3")
    BAD("나쁨", "\uD83D\uDE20", R.color.bad),
    @SerializedName("4")
    TERRIBLE("매우 나쁨", "\uD83D\uDC7F", R.color.terrible),
    UNKNOWN("미측정", "\uD83D\uDCA4", R.color.unknown);


    override fun toString(): String {
        return "$label $emoji"
    }
}