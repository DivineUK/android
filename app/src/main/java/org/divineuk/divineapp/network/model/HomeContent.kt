package org.divineuk.divineapp.network.model

class HomeContent {
    var content: List<Widget>? = null
}

enum class WidgetType(val itemName: String) {
    Title("title"),
    Image("Image"),
    Carousel("Carousel")
}

