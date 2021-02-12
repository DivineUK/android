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
package org.divineuk.divineapp.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlinx.android.synthetic.main.fragment_mini_player.*
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.NetworkAudio
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote
import org.divineuk.divineapp.audio.helper.MusicProgressViewUpdateHelper
import org.divineuk.divineapp.ui.fragments.base.AbsMusicServiceFragment

open class MiniPlayerFragment : AbsMusicServiceFragment(R.layout.fragment_mini_player),
    MusicProgressViewUpdateHelper.Callback, View.OnClickListener {

    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    var progressbarDummyVal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.actionNext -> {

            }
            R.id.actionPrevious -> {
            }

            R.id.actionStop -> {
                MusicPlayerRemote.stop()
                println("Sabin stop player")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnTouchListener(FlingPlayBackController(requireContext()))
        setUpMiniPlayer()


        /*  actionNext.visibility = if (PreferenceUtil.isExtraControls) View.VISIBLE else View.GONE
          actionPrevious.visibility =
              if (PreferenceUtil.isExtraControls) View.VISIBLE else View.GONE

         */

        actionNext.setOnClickListener(this)
        actionPrevious.setOnClickListener(this)
        actionNext?.setOnClickListener(this)
        actionPrevious?.setOnClickListener(this)
        actionStop.setOnClickListener(this)
    }

    private fun setUpMiniPlayer() {
        setUpPlayPauseButton()
        //progressBar.accentColor()

    }

    private fun setUpPlayPauseButton() {

        class PlayPauseButtonOnClickHandler : View.OnClickListener {
            override fun onClick(v: View) {
                if (MusicPlayerRemote.isPlaying) {
                    MusicPlayerRemote.pauseSong()
                } else {
                    MusicPlayerRemote.resumePlaying(
                        NetworkAudio(
                            "https://streamer.radio.co/s4a51a8c93/listen",
                            "",
                            "",
                            ""
                        )
                    )
                }
            }
        }

        miniPlayerPlayPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updateSongTitle() {
        /*
        val builder = SpannableStringBuilder()

        val song = MusicPlayerRemote.currentSong
        val title = SpannableString(song.title)
        title.setSpan(ForegroundColorSpan(textColorPrimary()), 0, title.length, 0)

        val text = SpannableString(song.artistName)
        text.setSpan(ForegroundColorSpan(textColorSecondary()), 0, text.length, 0)

        builder.append(title).append(" • ").append(text)
         */


        miniPlayerTitle.text = if(MusicPlayerRemote?.metadata.length<60){
            MusicPlayerRemote.metadata.padEnd(60,' ')
        }else{
            MusicPlayerRemote.metadata
        }
        println(" metadata length ${MusicPlayerRemote.metadata.length}" )
        miniPlayerTitle.isSelected = true

    }

    override fun onServiceConnected() {
        updateSongTitle()
        updatePlayPauseDrawableState()
    }

    override fun onPlayingMetaChanged() {
        println("Metadata changed")
        updateSongTitle()
    }



    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        progressBar.max = total
        val animator = ObjectAnimator.ofInt(progressBar, "progress", progressbarDummyVal)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    protected fun updatePlayPauseDrawableState() {
        println(MusicPlayerRemote.isPlaying)
        if (MusicPlayerRemote.isPlaying) {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    fun updateProgressBar(paletteColor: Int) {
        //progressBar.applyColor(paletteColor)
    }

    class FlingPlayBackController(context: Context) : View.OnTouchListener {

        private var flingPlayBackController: GestureDetector

        init {
            flingPlayBackController = GestureDetector(context,
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onFling(
                        e1: MotionEvent,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float
                    ): Boolean {
                        if (abs(velocityX) > abs(velocityY)) {
                            if (velocityX < 0) {

                                return true
                            } else if (velocityX > 0) {

                                return true
                            }
                        }
                        return false
                    }
                })
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return flingPlayBackController.onTouchEvent(event)
        }
    }
}