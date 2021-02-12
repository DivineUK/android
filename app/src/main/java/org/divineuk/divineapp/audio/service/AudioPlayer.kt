/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.divineuk.divineapp.audio.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.PowerManager
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.old.PlaybackStatus
import org.divineuk.divineapp.audio.old.PlaybackStatus.IDLE
import org.divineuk.divineapp.audio.old.RadioService

/** @author Andrew Neal, Karim Abou Zeid (kabouzeid)
 */
class AudioPlayer internal constructor(private val context: Context?) : Playback,
    MediaPlayer.OnErrorListener, OnCompletionListener {
    private var mCurrentMediaPlayer:SimpleExoPlayer
    private var streamUrl:String=""


    init {
        val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(context!!)
        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(context, trackSelectionFactory)

        mCurrentMediaPlayer = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        mCurrentMediaPlayer.setWakeMode(C.WAKE_MODE_NETWORK)
        mCurrentMediaPlayer!!.setHandleAudioBecomingNoisy(true)
    }


    /** @return True if the player is ready to go, false otherwise
     */
    override var isInitialized = false
        private set


    private val userAgent: String
        private get() = Util.getUserAgent(context!!, javaClass.simpleName)

    /** Starts or resumes playback.  */
    override fun start(): Boolean {
        return try {
            val dataSourceFactory =
                DefaultDataSourceFactory(context!!, userAgent)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(streamUrl))
            mCurrentMediaPlayer!!.prepare(mediaSource)
            mCurrentMediaPlayer!!.playWhenReady = true
            mCurrentMediaPlayer!!.addMetadataOutput { metadata: Metadata ->
                for (i in 0 until metadata.length()) {
                    if (metadata[i] is IcyInfo) {
                        val title = (metadata[i] as IcyInfo).title
                        //notificationManager!!.setStrLiveBroadcast(title!!)
                        //notificationManager!!.startNotify(RadioService.ACTION_PLAY)
                    }

                }

            }
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /** Resets the MediaPlayer to its uninitialized state.  */
    override fun stop() {
        mCurrentMediaPlayer.release()
        this.isInitialized = false
    }

    /** Releases resources associated with this MediaPlayer object.  */
    override fun release() {
        stop()
        mCurrentMediaPlayer.release()
    }

    /** Pauses playback. Call start() to resume.  */
    override fun pause(): Boolean {
        return try {
            mCurrentMediaPlayer.pause()
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /** Checks whether the MultiPlayer is playing.  */
    override val isPlaying: Boolean
        get() = this.isInitialized && mCurrentMediaPlayer.isPlaying

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    override fun duration(): Int {
        return if (!this.isInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.duration.toInt()
        } catch (e: IllegalStateException) {
            -1
        }
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    override fun position(): Int {
        return if (!this.isInitialized) {
            -1
        } else try {
            mCurrentMediaPlayer.currentPosition.toInt()
        } catch (e: IllegalStateException) {
            -1
        }
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     * @return The offset in milliseconds from the start to seek to
     */
    override fun seek(whereto: Int): Int {
        return try {
            mCurrentMediaPlayer.seekTo(whereto.toLong())
            whereto
        } catch (e: IllegalStateException) {
            -1
        }
    }

    override fun setVolume(vol: Float): Boolean {
        return try {
            mCurrentMediaPlayer.volume = vol
            true
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    override fun setAudioSessionId(sessionId: Int): Boolean {
        return try {
            mCurrentMediaPlayer.audioSessionId = sessionId
            true
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: IllegalStateException) {
            false
        }
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    override val audioSessionId: Int
        get() = mCurrentMediaPlayer.audioSessionId

    override fun setDataSource(path: String): Boolean {
        streamUrl = path
        return true
    }

    /** {@inheritDoc}  */
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        this.isInitialized = false
        mCurrentMediaPlayer.release()
        return false
    }

    /** {@inheritDoc}  */
    override fun onCompletion(mp: MediaPlayer) {
        mCurrentMediaPlayer.release()
    }

    companion object {
        val TAG = AudioPlayer::class.java.simpleName
    }

}