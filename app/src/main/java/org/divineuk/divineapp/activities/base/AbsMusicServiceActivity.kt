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

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference
import java.util.*
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote
import org.divineuk.divineapp.audio.old.RadioService
import org.divineuk.divineapp.audio.old.RadioService.Companion.AUDIO_STOPPED
import org.divineuk.divineapp.audio.service.MusicService.*
import org.divineuk.divineapp.interfaces.IMusicServiceEventListener

abstract class AbsMusicServiceActivity : AppCompatActivity(), IMusicServiceEventListener {

    private val mMusicServiceEventListeners = ArrayList<IMusicServiceEventListener>()
    private var serviceToken: MusicPlayerRemote.ServiceToken? = null
    private var musicStateReceiver: MusicStateReceiver? = null
    private var receiverRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceToken = MusicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                this@AbsMusicServiceActivity.onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@AbsMusicServiceActivity.onServiceDisconnected()
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerRemote.unbindFromService(serviceToken)
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
    }

    fun addMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.add(listenerI)
        }
    }

    fun removeMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.remove(listenerI)
        }
    }

    override fun onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = MusicStateReceiver(this)

            val filter = IntentFilter() //TOdo to be added
            filter.addAction(PLAY_STATE_CHANGED)
            filter.addAction(AUDIO_STOPPED)
            filter.addAction(SHUFFLE_MODE_CHANGED)
            filter.addAction(REPEAT_MODE_CHANGED)
            filter.addAction(META_CHANGED)
            filter.addAction(QUEUE_CHANGED)
            filter.addAction(MEDIA_STORE_CHANGED)
            filter.addAction(FAVORITE_STATE_CHANGED)

            registerReceiver(musicStateReceiver, filter)

            receiverRegistered = true
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
        //add song to hsitory
    }

    override fun onPlayerStopped() {
        println("Sabin Stopped")
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayerStopped()
        }
    }



    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }
    }


    private class MusicStateReceiver(activity: AbsMusicServiceActivity) : BroadcastReceiver() {

        private val reference: WeakReference<AbsMusicServiceActivity> = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val activity = reference.get()
            if (activity != null && action != null) {
                println("Sabin stop action change:$action")
                when (action) {
                    FAVORITE_STATE_CHANGED, META_CHANGED -> activity.onPlayingMetaChanged()
                    PLAY_STATE_CHANGED -> activity.onPlayStateChanged()
                    AUDIO_STOPPED -> activity.onPlayerStopped()
                }
            }
        }
    }

    companion object {
        val TAG: String = AbsMusicServiceActivity::class.java.simpleName
    }
}
