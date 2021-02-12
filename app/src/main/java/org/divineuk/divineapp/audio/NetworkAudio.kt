package org.divineuk.divineapp.audio

data class NetworkAudio(
    val streamUrl: String,
    val title: String,
    val imageUrl: String="",
    val description: String=""
) {
    companion object {

        @JvmStatic
        val emptyNetworkAudio = NetworkAudio(
            streamUrl = "", title = "", imageUrl = "",
            description = ""
        )
    }
}