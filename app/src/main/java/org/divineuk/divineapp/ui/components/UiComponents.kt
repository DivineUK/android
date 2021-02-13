package org.divineuk.divineapp.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.CarouselView
import com.synnapps.carouselview.ImageListener
import kotlinx.android.synthetic.main.item_title.view.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.network.model.*


object UiComponents {
    const val UNKNOWN = -1
    const val TYPE_TITLE = 1
    const val TYPE_IMAGE = 2
    const val TYPE_CAROUSEL = 3
    const val TYPE_EVENT = 4
    const val TYPE_YOUTUBE_LINK = 5
    const val TYPE_IMAGE_LINK = 6
    const val TYPE_TEXT_LINK = 7


    //TODO extract to dims xml
    private fun textSize(textSize: String): Float {
        return when (textSize) {
            "h1" -> 20.0F
            "h2" -> 16.0F
            "h3" -> 12.0F
            "h4" -> 10.0F
            else -> 12.0F
        }
    }

    private fun setGravity(align: String): Int {
        return when (align) {
            "left" -> Gravity.START
            "center" -> Gravity.CENTER
            "right" -> Gravity.END
            else -> Gravity.CENTER
        }
    }

    private fun parseColor(color: String): Int {
        return when (color) {
            "primary" -> R.color.colorHomePageTitle //todo
            "secondary" -> R.color.colorHomePageTitle
            "tertiary" -> R.color.colorHomePageTitle
            else -> R.color.colorPrimary
        }
    }

    class UnknownViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.title_view.visibility = View.INVISIBLE
            itemView.sub_title_view.visibility = View.INVISIBLE
        }
    }


    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView as ImageView
        fun setData(image: Image) {
            Picasso.get().load(image.props.src).into(this.imageView)
        }

    }

    class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(title: Title) {
            val titleTextView = itemView.title_view
            val subTitleTextView = itemView.sub_title_view
            titleTextView.text = title.props?.title
            subTitleTextView.text = title.props?.subTitle
            // titleTextView.textSize = textSize(title.props?.size!!)
            //titleTextView.gravity = setGravity(title.props.align)

        }
    }

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(carousel: Carousel) {
            val carouselView = itemView as CarouselView
            val images: List<ImageProperty> = carousel.props.images
            carouselView.pageCount = images.size

            val imageListener =
                ImageListener { position, imageView ->
                    Picasso.get().load(images[position].src).fit().into(
                        imageView
                    )
                }

            carouselView.setImageListener(imageListener)
        }
    }

    class EventViewHolder(private val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView as ImageView
        fun setData(event: Event) {
            if (event.props?.type == "youtube") {
                val imageUrl = "https://img.youtube.com/vi/${event.props.link}/mqdefault.jpg"
                Picasso.get().load(imageUrl).into(this.imageView)
                imageView.setOnClickListener {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=${event.props.link}")
                        )
                    )
                }
            }

        }
    }

    class YoutubeViewHolder(private val context: Context, itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView as ImageView
        fun setData(youtubeLink: YoutubeLink) {

            val imageUrl = "https://img.youtube.com/vi/${youtubeLink.props?.id}/mqdefault.jpg"
            Picasso.get().load(imageUrl).into(this.imageView)
            imageView.setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=${youtubeLink.props?.id}")
                    )
                )
            }

        }
    }

    class ImageLinkViewHolder(private val context: Context,itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView as ImageView
        fun setData(imageLink: ImageLink) {

            Picasso.get().load(imageLink.props?.imageURL).into(this.imageView)
            imageView.setOnClickListener {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(imageLink.props?.link)
                    )
                )
            }

        }
    }

    class TextLinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}