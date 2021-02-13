package org.divineuk.divineapp.ui.adapter

import android.content.Context
import android.telephony.UiccCardInfo
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.divineuk.divineapp.R
import org.divineuk.divineapp.network.model.*
import org.divineuk.divineapp.ui.components.UiComponents

class HomeAdapter(private val context: Context, homeContent: HomeContent) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val widgets = homeContent.content!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            UiComponents.TYPE_TITLE -> {
                val itemView =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_title, parent, false)
                UiComponents.TitleViewHolder(itemView)
            }
            UiComponents.TYPE_IMAGE -> {
                val itemView =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
                UiComponents.ImageViewHolder(itemView)
            }
            UiComponents.TYPE_CAROUSEL -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_carousel, parent, false)
                UiComponents.CarouselViewHolder(itemView)
            }


            UiComponents.TYPE_EVENT -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
                UiComponents.EventViewHolder(context, itemView)
            }

            UiComponents.TYPE_YOUTUBE_LINK -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
                UiComponents.YoutubeViewHolder(context,itemView)
            }

            UiComponents.TYPE_IMAGE_LINK -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
                UiComponents.ImageLinkViewHolder(context,itemView)
            }

            UiComponents.TYPE_TEXT_LINK -> {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
                UiComponents.TextLinkViewHolder(itemView)
            }


            else ->  {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_title, parent, false)
                UiComponents.UnknownViewHolder(itemView)
            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        return when(this.widgets[position]){
            is Title -> UiComponents.TYPE_TITLE
            is Image -> UiComponents.TYPE_IMAGE
            is Carousel ->UiComponents.TYPE_CAROUSEL
            is YoutubeLink -> UiComponents.TYPE_YOUTUBE_LINK
            is Event -> UiComponents.TYPE_EVENT
            is ImageLink -> UiComponents.TYPE_IMAGE_LINK
            is TextLink -> UiComponents.TYPE_TEXT_LINK
            else -> UiComponents.UNKNOWN
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val widget = widgets[position]
        when(getItemViewType(position)){
            UiComponents.TYPE_TITLE -> {
                val mHolder = holder as UiComponents.TitleViewHolder
                mHolder.setData(widget as Title)
            }
            UiComponents.TYPE_IMAGE -> {
                val mHolder = holder as UiComponents.ImageViewHolder
                mHolder.setData(widget as Image)
            }
            UiComponents.TYPE_CAROUSEL -> {

                val mHolder = holder as UiComponents.CarouselViewHolder
                mHolder.setData(widget as Carousel)

            }

            UiComponents.TYPE_EVENT -> {

                val mHolder = holder as UiComponents.EventViewHolder
                mHolder.setData(widget as Event)

            }
            UiComponents.TYPE_YOUTUBE_LINK -> {

                val mHolder = holder as UiComponents.YoutubeViewHolder
                mHolder.setData(widget as YoutubeLink)

            }
            UiComponents.TYPE_IMAGE_LINK -> {

                val mHolder = holder as UiComponents.ImageLinkViewHolder
                mHolder.setData(widget as ImageLink)

            }
            UiComponents.TYPE_TEXT_LINK -> {

                //val mHolder = holder as UiComponents.CarouselViewHolder
                //mHolder.setData(widget as Carousel)

            }
        }
    }

    override fun getItemCount(): Int {
        return widgets.size
    }

}