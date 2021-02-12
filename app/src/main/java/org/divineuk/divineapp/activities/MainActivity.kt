package org.divineuk.divineapp.activities

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sliding_music_panel_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.divineuk.divineapp.R
import org.divineuk.divineapp.activities.base.AbsSlidingMusicPanelActivity
import org.divineuk.divineapp.audio.old.RadioManager

@AndroidEntryPoint
class MainActivity : AbsSlidingMusicPanelActivity() {
    lateinit var radioManager: RadioManager

    override fun createContentView(): View {
        return wrapSlidingMusicPanel()
    }



    private fun setupNavigationController() {
        /* val navController = findNavController(R.id.nav_host_fragment_main)
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph)

        navController.graph = navGraph


         */
       NavigationUI.setupWithNavController(
            bottomNavigationView,
            Navigation.findNavController(this,R.id.nav_host_fragment_main)
        )

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNavigationController()
        radioManager = RadioManager.with(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    override fun onResume() {

        super.onResume()
        radioManager.bind()

        GlobalScope.launch(Dispatchers.Main) {
            delay(2000)
            //radioManager?.playOrPause("https://streamer.radio.co/s4a51a8c93/listen")
        }


    }









}