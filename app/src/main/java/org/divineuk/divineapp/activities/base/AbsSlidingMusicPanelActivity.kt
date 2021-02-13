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
package org.divineuk.divineapp.activities.base

import PlayerFragment
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.sliding_music_panel_layout.*
import org.divineuk.divineapp.*
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote
import org.divineuk.divineapp.ui.fragments.AbsPlayerFragment
import org.divineuk.divineapp.ui.fragments.player.AdaptiveFragment
import org.divineuk.divineapp.ui.fragments.MiniPlayerFragment

abstract class AbsSlidingMusicPanelActivity : AbsMusicServiceActivity() {
    companion object {
        val TAG: String = AbsSlidingMusicPanelActivity::class.java.simpleName
    }

    private lateinit var bottomSheetBehavior: RetroBottomSheetBehavior<FrameLayout>
    private var playerFragment: AbsPlayerFragment? = null
    private var miniPlayerFragment: MiniPlayerFragment? = null
    protected abstract fun createContentView(): View
    private val panelState: Int
        get() = bottomSheetBehavior.state

    private val bottomSheetCallbackList = object : BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            setMiniPlayerAlphaProgress(slideOffset)
            dimBackground.show()
            dimBackground.alpha = slideOffset
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                STATE_EXPANDED -> {
                   // onPanelExpanded()
                    println("Sabin Expamded${bottomSheetBehavior.peekHeight }")
                }
                STATE_COLLAPSED -> {
                   // onPanelCollapsed()
                    println("Sabin Collapsed${bottomSheetBehavior.peekHeight }")
                    dimBackground.hide()
                }
                else -> {
                    println("Do something")
                }
            }
        }
    }

    fun getBottomSheetBehavior() = bottomSheetBehavior

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLightActionBar()
        setupLightStatusBar()
        setContentView(createContentView())
        chooseFragmentForTheme()
        setupSlidingUpPanel()
        setupBottomSheet()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = from(slidingPanel) as RetroBottomSheetBehavior
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallbackList)
    }

    override fun onResume() {
        super.onResume()

        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            setMiniPlayerAlphaProgress(1f)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallbackList)
    }

    @SuppressLint("InflateParams")
    protected fun wrapSlidingMusicPanel(): View {
        val slidingMusicPanelLayout =
            layoutInflater.inflate(R.layout.sliding_music_panel_layout, null)
        val contentContainer: ViewGroup =
            slidingMusicPanelLayout.findViewById(R.id.mainContentFrame)
        layoutInflater.inflate(R.layout.activity_main, contentContainer)
        return slidingMusicPanelLayout
    }

    fun collapsePanel() {
        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    fun expandPanel() {
        bottomSheetBehavior.state = STATE_EXPANDED
        setMiniPlayerAlphaProgress(1f)
    }

    private fun setMiniPlayerAlphaProgress(progress: Float) {
        val alpha = 1 - progress
        miniPlayerFragment?.view?.alpha = alpha
        miniPlayerFragment?.view?.visibility = if (alpha == 0f) View.GONE else View.VISIBLE
        bottomNavigationView.translationY = progress * 500
        bottomNavigationView.alpha = alpha
    }



    private fun setupSlidingUpPanel() {
        slidingPanel.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                slidingPanel.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val params = slidingPanel.layoutParams as ViewGroup.LayoutParams
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    slidingPanel.layoutParams = params
                }

        })
    }

    fun getBottomNavigationView(): BottomNavigationView {
        return bottomNavigationView
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (true) { //todo change and test
            slidingPanel.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    slidingPanel.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    hideBottomBar(false)
                }
            })
        } // don't call hideBottomBar(true) here as it causes a bug with the SlidingUpPanelLayout
    }


    override fun onBackPressed() {
        if (!handleBackPress()) super.onBackPressed()
    }

    private fun handleBackPress(): Boolean {

        if (bottomSheetBehavior.peekHeight != 0 && playerFragment!!.onBackPressed()) return true
        if (panelState == STATE_EXPANDED) {
            collapsePanel()
            return true
        }

        return false
    }

    override fun onPlayerStopped() {
        super.onPlayerStopped()
        println("Sabin Onplayer stopped")
        hideBottomBar(true)
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        hideBottomBar(false)
        println("Playerstatechanged")
    }

    /*

    fun updateTabs() {
        bottomNavigationView.menu.clear()
        val currentTabs: List<CategoryInfo> = PreferenceUtil.libraryCategory
        for (tab in currentTabs) {
            if (tab.visible) {
                val menu = tab.category
                bottomNavigationView.menu.add(0, menu.id, 0, menu.stringRes).setIcon(menu.icon)
            }
        }
        if (bottomNavigationView.menu.size() == 1) {
            bottomNavigationView.hide()
        }
    }

     */


    fun setBottomBarVisibility(visible: Int) {
        bottomNavigationView.visibility = visible
        hideBottomBar(false)
    }

    protected fun hideBottomBar(hide: Boolean) {
        val heightOfBar = dip(R.dimen.mini_player_height)
        val heightOfBarWithTabs = dip(R.dimen.mini_player_height_expanded)
        val isVisible = bottomNavigationView.isVisible
        if (hide) {
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.peekHeight = 0
            ViewCompat.setElevation(slidingPanel, 0f)
            ViewCompat.setElevation(bottomNavigationView, 10f)
            collapsePanel()
        } else {
            if (!MusicPlayerRemote.isPlayerStopped) {
                bottomSheetBehavior.isHideable = false
                ViewCompat.setElevation(slidingPanel, 10f)
                ViewCompat.setElevation(bottomNavigationView, 10f)
                if (isVisible) {
                    bottomSheetBehavior.peekHeight = heightOfBarWithTabs
                    bottomNavigationView.translateYAnimate(0f)
                } else {
                    bottomNavigationView.translateYAnimate(150f)
                    bottomSheetBehavior.peekHeight = heightOfBar
                }
            }
        }
    }

    private fun chooseFragmentForTheme() {

        val fragment: Fragment = PlayerFragment()//AdaptiveFragment()

        supportFragmentManager.commit {
            replace(R.id.playerFragmentContainer, fragment)
        }
        supportFragmentManager.executePendingTransactions()
        playerFragment = whichFragment<AbsPlayerFragment>(R.id.playerFragmentContainer)
        miniPlayerFragment = whichFragment<MiniPlayerFragment>(R.id.miniPlayerFragment)
        miniPlayerFragment?.view?.setOnClickListener { expandPanel() }
    }

    private fun setupLightActionBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            window.navigationBarColor = resources.getColor(R.color.colorPrimary)

        }
    }


    private fun setupLightStatusBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val flags = window.decorView.systemUiVisibility

            window.decorView.setSystemUiVisibility(flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
