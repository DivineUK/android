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

import android.os.Bundle
import android.view.View
import org.divineuk.divineapp.R
import org.divineuk.divineapp.ui.fragments.AbsPlayerFragment
import org.divineuk.divineapp.ui.fragments.AlbumCoverFragment

class AdaptiveFragment : AbsPlayerFragment(R.layout.fragment_adaptive_player) {



    private var lastColor: Int = 0
    private lateinit var playbackControlsFragment: AdaptivePlaybackControlsFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSubFragments()
        //setUpPlayerToolbar()
    }



    private fun setUpSubFragments() {
        playbackControlsFragment =
            childFragmentManager.findFragmentById(R.id.playbackControlsFragment) as AdaptivePlaybackControlsFragment
        val playerAlbumCoverFragment =
            childFragmentManager.findFragmentById(R.id.albumCoverFragment) as AlbumCoverFragment
        playerAlbumCoverFragment.apply {
           // removeSlideEffect()
           // setCallbacks(this@AdaptiveFragment)
        }
    }



    override fun onServiceConnected() {
        super.onServiceConnected()
        //updateIsFavorite()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        //updateIsFavorite()
        updateSong()
    }

    private fun updateSong() {

    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onShow() {
    }

    override fun onHide() {
        onBackPressed()
    }

}
