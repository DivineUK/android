package org.divineuk.divineapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.radio_list_fragment.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.NetworkAudio
import org.divineuk.divineapp.models.ImageAndText
import org.divineuk.divineapp.ui.adapter.RadioListAdapter
import org.divineuk.divineapp.ui.fragments.base.AbsMusicServiceFragment

class RadioFragment : AbsMusicServiceFragment(R.layout.radio_list_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val imageAndTextList = mutableListOf<NetworkAudio>(
            NetworkAudio(streamUrl ="https://stream.radio.co/s49163d29a/listen","English" ),
            NetworkAudio("https://streamer.radio.co/s4a51a8c93/listen", "Malayalam"),
            NetworkAudio("https://streaming.radio.co/s1938904e8/listen", "Tamil",),
            NetworkAudio("https://streams.radio.co/s1f940d252/listen", "Konkani")
        )

        recyclerView.adapter =
            RadioListAdapter(this,imageAndTextList)
        recyclerView.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}