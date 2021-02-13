package org.divineuk.divineapp.network.model

import com.google.gson.annotations.SerializedName

open class Widget(val id: String, val type: String)


class Title(id: String, type: String, val props: Property? = null) : Widget(id, type) {

    data class Property(
        val title: String,
        val size: String,
        val subTitle: String,
    )
}

class Event(id: String, type: String, val props: Property? = null) : Widget(id, type) {

    data class Property(
        val title: String,
        val link: String,
        val type: String,
        val time: String
    )
}

class YoutubeLink(id: String, type: String, val props: Property? = null) : Widget(id, type) {

    data class Property(
        val title: String,
        val id: String,
    )
}

class ImageLink(id: String, type: String, val props: Property? = null) : Widget(id, type) {

    data class Property(
        val title: String,
        val link: String,
        val imageURL: String,
    )
}

class TextLink(id: String, type: String, val props: Property? = null) : Widget(id, type) {
    data class Property(
        val title: String,
        val link: String,
        val size: String,
    )
}


class Image(id: String, type: String, val props: Property) : Widget(id, type) {

    data class Property(
        @SerializedName("aspect_ratio") val aspectRatio: String,
        val src: String,
        val link: String,
        @SerializedName("alt_text") val altText: String
    )
}


class Carousel(id: String, type: String, val props: Property) : Widget(id, type) {

    data class Property(
        @SerializedName("aspect_ratio") val aspectRatio: String,
        @SerializedName("images") var images: MutableList<ImageProperty>
    )


}

data class ImageProperty(
    val src: String,
    @SerializedName("alt_text") val altText: String
)
