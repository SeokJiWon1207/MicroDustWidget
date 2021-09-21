package com.example.microdustwidget.data.models.coordinates


import com.google.gson.annotations.SerializedName

data class TmCoordinateResponse(
    @SerializedName("documents")
    val documents: List<Document>?,
    @SerializedName("meta")
    val meta: Meta?
)