package org.divineuk.divineapp.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.divineuk.divineapp.R
import org.divineuk.divineapp.network.model.*
import org.divineuk.divineapp.ui.components.UiComponents

class HomeAdapter(homeContent: HomeContent) :
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
            else -> null!!
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when(this.widgets[position]){
            is Title -> UiComponents.TYPE_TITLE
            is Image -> UiComponents.TYPE_IMAGE
            is Carousel ->UiComponents.TYPE_CAROUSEL
            else -> null!!
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
        }
    }

    override fun getItemCount(): Int {
        return widgets.size
    }

}