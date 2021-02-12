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

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.LayoutRes
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.helper.MusicProgressViewUpdateHelper
import org.divineuk.divineapp.ui.fragments.base.AbsMusicServiceFragment


abstract class AbsPlayerControlsFragment(@LayoutRes layout: Int) : AbsMusicServiceFragment(layout),
    MusicProgressViewUpdateHelper.Callback {

    protected abstract fun show()

    protected abstract fun hide()

    protected abstract fun setUpProgressSlider()

    fun showBonceAnimation(view: View) {
        view.apply {
            clearAnimation()
            scaleX = 0.9f
            scaleY = 0.9f
            visibility = View.VISIBLE
            pivotX = (view.width / 2).toFloat()
            pivotY = (view.height / 2).toFloat()

            animate().setDuration(200)
                .setInterpolator(DecelerateInterpolator())
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction {
                    animate().setDuration(200)
                        .setInterpolator(AccelerateInterpolator())
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .start()
                }
                .start()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideVolumeIfAvailable()
    }

    protected var volumeFragment: VolumeFragment? = null

    private fun hideVolumeIfAvailable() {
        //if (PreferenceUtil.isVolumeVisibilityMode) {
            childFragmentManager.beginTransaction()
                .replace(R.id.volumeFragmentContainer, VolumeFragment()).commit()
            childFragmentManager.executePendingTransactions()
            volumeFragment =
                childFragmentManager.findFragmentById(R.id.volumeFragmentContainer) as VolumeFragment?
       // }
    }

    companion object {
        const val SLIDER_ANIMATION_TIME: Long = 400
    }
}