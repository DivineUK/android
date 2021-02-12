package org.divineuk.divineapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.radio_list_row.view.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.NetworkAudio
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote


class RadioListAdapter(private val fragmentContext: Fragment?, private val rowList: List<NetworkAudio>) :
    RecyclerView.Adapter<RadioListAdapter.CardViewHolder>() {


    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView.title!!
        val imageView = itemView.image!!
        val container = itemView.container!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.radio_list_row, parent, false)
        return CardViewHolder(
            itemView
        )
    }

    override fun getItemCount() = rowList.size


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val streamAudio = rowList[position]
        holder.imageView.setImageResource(R.drawable.ic_baseline_radio_24)
        holder.textView.text = streamAudio.title
        holder.container.setOnClickListener {
            MusicPlayerRemote.play(
                streamAudio
            )
        }
    }
}