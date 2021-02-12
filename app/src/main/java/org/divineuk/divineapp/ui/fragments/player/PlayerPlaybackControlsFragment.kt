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
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.fragment_player_playback_controls.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.NetworkAudio
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote
import org.divineuk.divineapp.audio.helper.MusicProgressViewUpdateHelper
import org.divineuk.divineapp.audio.helper.SimpleOnSeekbarChangeListener
import org.divineuk.divineapp.ui.fragments.AbsPlayerControlsFragment
import org.divineuk.divineapp.util.MusicUtil

class PlayerPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.fragment_player_playback_controls) {


    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
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
        stopButton.setOnClickListener{
            MusicPlayerRemote.stop()
        }
        title.isSelected = true
        text.isSelected = true
    }



    private fun updateSong() {
        title.text = MusicPlayerRemote.metadata
        text.text = resources.getText(R.string.app_name)
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }


    private fun setUpPlayPauseFab() {
       // playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            playPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
        DrawableCompat.setTint(playPauseButton.drawable, Color.WHITE)
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
    }

    private fun setUpPrevNext() {
        updatePrevNextColor()
       // nextButton.setOnClickListener { MusicPlayerRemote.playNextSong() }
        //previousButton.setOnClickListener { MusicPlayerRemote.back() }
    }

    private fun updatePrevNextColor() {
        //nextButton.setColorFilter(R.color.md_white_1000, PorterDuff.Mode.SRC_IN)
        //previousButton.setColorFilter(R.color.md_white_1000, PorterDuff.Mode.SRC_IN)
        //stopButton.setColorFilter(R.color.md_white_1000, PorterDuff.Mode.SRC_IN)

        previousButton.visibility =View.INVISIBLE
        nextButton.visibility =View.INVISIBLE

        DrawableCompat.setTint(playPauseButton.drawable, Color.WHITE)
        DrawableCompat.setTint(stopButton.drawable, Color.WHITE)


    }

    private fun setUpShuffleButton() {
       // shuffleButton.setOnClickListener { MusicPlayerRemote.toggleShuffleMode() }
    }



    private fun setUpRepeatButton() {
       // repeatButton.setOnClickListener { MusicPlayerRemote.cycleRepeatMode() }
    }



    public override fun show() {
        playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun setUpProgressSlider() {
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
