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

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_player.*
import org.divineuk.divineapp.DrawableGradient
import org.divineuk.divineapp.R
import org.divineuk.divineapp.ui.fragments.AbsPlayerFragment
import org.divineuk.divineapp.ui.fragments.AlbumCoverFragment
import org.divineuk.divineapp.ui.fragments.player.PlayerPlaybackControlsFragment

class PlayerFragment : AbsPlayerFragment(R.layout.fragment_player) {

    private var lastColor: Int = 0
        get() = lastColor

    private lateinit var controlsFragment: PlayerPlaybackControlsFragment
    private var valueAnimator: ValueAnimator? = null

    private fun colorize(i: Int) {
        if (valueAnimator != null) {
            valueAnimator?.cancel()
        }

        valueAnimator = ValueAnimator.ofObject(
            ArgbEvaluator(),
            R.color.colorAccent,
            i
        )
        valueAnimator?.addUpdateListener { animation ->
            if (isAdded) {
                val drawable = DrawableGradient(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        animation.animatedValue as Int,
                        R.color.colorAccent
                    ), 0
                )
                colorGradientBackground?.background = drawable
            }
        }
        valueAnimator?.setDuration(1000)?.start()
    }

    override fun onShow() {
        controlsFragment.show()
    }

    override fun onHide() {
        controlsFragment.hide()
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSubFragments()
       // setUpPlayerToolbar()
    }

    private fun setUpSubFragments() {
        controlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as PlayerPlaybackControlsFragment
        val playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.playerAlbumCoverFragment) as AlbumCoverFragment
        //playerAlbumCoverFragment.setCallbacks(this)
    }


    override fun onServiceConnected() {
    }

    override fun onPlayingMetaChanged() {
    }


    companion object {

        fun newInstance(): PlayerFragment {
            return PlayerFragment()
        }
    }
}
