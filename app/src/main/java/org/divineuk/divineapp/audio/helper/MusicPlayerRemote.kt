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

package org.divineuk.divineapp.audio.helper

import android.app.Activity
import android.content.*
import android.os.IBinder
import androidx.core.content.ContextCompat
import org.divineuk.divineapp.audio.NetworkAudio
import org.divineuk.divineapp.audio.old.RadioService
import org.divineuk.divineapp.audio.service.MusicService
import java.util.*

object MusicPlayerRemote {

    val TAG: String = MusicPlayerRemote::class.java.simpleName
    private val mConnectionMap = WeakHashMap<Context, ServiceBinder>()
    var musicService: RadioService? = null


    @JvmStatic
    val isPlaying: Boolean
        get() = musicService != null && musicService!!.isPlaying

    fun isPlaying(song: NetworkAudio): Boolean {
        return if (!isPlaying) {
            false
        } else song.streamUrl == song.streamUrl
    }



    val currentSong: NetworkAudio
        get() = if (musicService != null) {
            NetworkAudio.emptyNetworkAudio //todo
        } else NetworkAudio.emptyNetworkAudio


    val songProgressMillis: Int
        get() = if (musicService != null) {
            musicService!!.songProgressMillis().toInt()
        } else -1

    val songDurationMillis: Int
        get() = if (musicService != null) {
            musicService!!.songDurationMillis().toInt()
        } else -1

     val metadata: String
        get() = if(musicService!=null){
            musicService?.metadataString!!
        }else ""

    val isPlayerStopped:Boolean
    get() = musicService?.isPlayerStopped ?: false


    fun bindToService(context: Context, callback: ServiceConnection): ServiceToken? {

        var realActivity: Activity? = (context as Activity).parent
        if (realActivity == null) {
            realActivity = context
        }

        val contextWrapper = ContextWrapper(realActivity)
        val intent = Intent(contextWrapper, RadioService::class.java)
        try {
            contextWrapper.startService(intent)
        } catch (ignored: IllegalStateException) {
            ContextCompat.startForegroundService(context, intent)
        }
        val binder = ServiceBinder(callback)

        if (contextWrapper.bindService(
                Intent().setClass(contextWrapper, RadioService::class.java),
                binder,
                Context.BIND_AUTO_CREATE
            )
        ) {
            mConnectionMap[contextWrapper] = binder
            return ServiceToken(contextWrapper)
        }
        return null
    }

    fun unbindFromService(token: ServiceToken?) {
        if (token == null) {
            return
        }
        val mContextWrapper = token.mWrappedContext
        val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
        mContextWrapper.unbindService(mBinder)
        if (mConnectionMap.isEmpty()) {
            musicService = null
        }
    }


    fun pauseSong() {
        musicService?.pause()
    }

    fun stop(){
      musicService?.stop()
    }

    fun play(networkAudio: NetworkAudio) {
        musicService?.play(networkAudio.streamUrl)
    }


    fun resumePlaying(networkAudio: NetworkAudio) {
        musicService?.playOrPause(networkAudio.streamUrl)
        println("Sabin")
    }


    fun seekTo(millis: Int): Int {
        /*return if (musicService != null) {
            musicService!!.seek(millis)
        } else -1*/
        return -1
    }

    class ServiceBinder internal constructor(private val mCallback: ServiceConnection?) :
        ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as RadioService.LocalBinder
            musicService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            musicService = null
        }
    }

    class ServiceToken internal constructor(internal var mWrappedContext: ContextWrapper)
}
