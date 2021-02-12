/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package org.divineuk.divineapp.ui.fragments.player

import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar

import kotlinx.android.synthetic.main.fragment_adaptive_player_playback_controls.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.NetworkAudio
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote
import org.divineuk.divineapp.audio.helper.MusicProgressViewUpdateHelper
import org.divineuk.divineapp.audio.helper.SimpleOnSeekbarChangeListener
import org.divineuk.divineapp.ui.fragments.AbsPlayerControlsFragment
import org.divineuk.divineapp.util.MusicUtil

class AdaptivePlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_adaptive_player_playback_controls) {

    private var lastPlaybackControlsColor: Int = 0
    private var lastDisabledPlaybackControlsColor: Int = 0
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_adaptive_player_playback_controls,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMusicControllers()

        playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying(NetworkAudio("https://streamer.radio.co/s4a51a8c93/listen","","",""))
            }
            showBonceAnimation(playPauseButton)
        }
    }

    private fun updateSong() {
        /*if (PreferenceUtil.isSongInfo) {
            songInfo?.text = getSongInfo(MusicPlayerRemote.currentSong)
            songInfo.show()
        } else {
            songInfo?.hide()
        }*/
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper!!.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper!!.stop()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }



    private fun updatePlayPauseColor() {
        // playPauseButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private fun setUpPlayPauseFab() {
       // playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpShuffleButton()
        setUpProgressSlider()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
        nextButton.setOnClickListener {  }
        previousButton.setOnClickListener {  }
    }

    private fun updatePrevNextColor() {
        nextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
        previousButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpShuffleButton() {
        //shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }

    override fun show() {
    }

    override fun hide() {
    }


    override fun setUpProgressSlider() {
        if(MusicPlayerRemote.songDurationMillis==1){
            progressSlider.isEnabled = false
        }
        progressSlider.setOnSeekBarChangeListener(object : SimpleOnSeekbarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress)
                    onUpdateProgressViews(
                        MusicPlayerRemote.songProgressMillis,
                        MusicPlayerRemote.songDurationMillis
                    )
                }
            }
        })
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {

        progressSlider.max = total


        val animator = ObjectAnimator.ofInt(progressSlider, "progress", progress)
        animator.duration = SLIDER_ANIMATION_TIME
        animator.interpolator = LinearInterpolator()
        animator.start()

        songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }
}
