package org.divineuk.divineapp.audio.old

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.divineuk.divineapp.R
import org.divineuk.divineapp.audio.service.MusicService

class RadioService : Service(), Player.EventListener,
    OnAudioFocusChangeListener {

    companion object {
        val RETRO_MUSIC_PACKAGE_NAME = "org.divineuk.divineapp"
        val META_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".metachanged"
        val AUDIO_STOPPED = RETRO_MUSIC_PACKAGE_NAME + ".stopped"

        const val ACTION_PLAY = "org.divineuk.divineapp.ACTION_PLAY"
        const val ACTION_PAUSE = "org.divineuk.divineapp.ACTION_PAUSE"
        const val ACTION_STOP = "org.divineuk.divineapp.ACTION_STOP"
    }







    private val iBinder: IBinder = LocalBinder()
    var exoPlayer: SimpleExoPlayer? = null
        private set
    var mediaSession: MediaSessionCompat? = null
        private set
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private var onGoingCall = false
    private var telephonyManager: TelephonyManager? = null
    private var audioManager: AudioManager? = null
    private var notificationManager: MediaNotificationManager? = null
    var status: String? = null
        private set
    private var strAppName: String? = null
    private var strLiveBroadcast: String? = null
    private var streamUrl: String? = null

    var metadataString:String = ""

    var isPlayerStopped = true;

    inner class LocalBinder : Binder() {
        val service: RadioService
            get() = this@RadioService
    }

    private val phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(
            state: Int,
            incomingNumber: String
        ) {
            if (state == TelephonyManager.CALL_STATE_OFFHOOK
                || state == TelephonyManager.CALL_STATE_RINGING
            ) {
                if (!isPlaying) return
                onGoingCall = true
                stop()
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (!onGoingCall) return
                onGoingCall = false
                resume()
            }
        }
    }
    private val mediasSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            override fun onPause() {
                super.onPause()
                pause()
            }

            override fun onStop() {
                super.onStop()
                stop()
                notificationManager!!.cancelNotify()
            }

            override fun onPlay() {
                super.onPlay()
                resume()
            }
        }

    override fun onBind(intent: Intent): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        strAppName = resources.getString(R.string.app_name)
        strLiveBroadcast = resources.getString(R.string.app_name) //TODO
        onGoingCall = false
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager = MediaNotificationManager(this)
        mediaSession = MediaSessionCompat(this, javaClass.simpleName)
        transportControls = mediaSession!!.controller.transportControls
        mediaSession!!.isActive = true
        mediaSession!!.setCallback(mediasSessionCallback)
        telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager!!.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(this)
        val trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, trackSelectionFactory)


        exoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        exoPlayer!!.addListener(this)
        exoPlayer!!.experimentalSetOffloadSchedulingEnabled(true)
        addMetadataListener()
        exoPlayer!!.setWakeMode(C.WAKE_MODE_NETWORK)
        exoPlayer!!.setHandleAudioBecomingNoisy(true)
        status = PlaybackStatus.IDLE

        updateNotificatioMetadata()

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (TextUtils.isEmpty(action)) return START_NOT_STICKY
        val result = audioManager!!.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            stop()
            return START_NOT_STICKY
        }
        if (action.equals(ACTION_PLAY, ignoreCase = true)) {
            transportControls!!.play()
        } else if (action.equals(ACTION_PAUSE, ignoreCase = true)) {
            transportControls!!.pause()
        } else if (action.equals(ACTION_STOP, ignoreCase = true)) {
            transportControls!!.stop()
        }
        return START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (status == PlaybackStatus.IDLE) stopSelf()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {}
    override fun onDestroy() {
        stop()
        exoPlayer!!.release()
        exoPlayer!!.removeListener(this)
        if (telephonyManager != null) telephonyManager!!.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_NONE
        )
        notificationManager!!.cancelNotify()
        mediaSession!!.release()
        super.onDestroy()
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                exoPlayer!!.volume = 0.8f
                resume()
            }
            AudioManager.AUDIOFOCUS_LOSS -> stop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> if (isPlaying) pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (isPlaying) exoPlayer!!.volume =
                0.1f
        }
    }

    fun songDurationMillis(): Long {
        return exoPlayer!!.duration
    }

    fun songProgressMillis():Long{
        return exoPlayer!!.contentPosition
    }

    override fun onPlayerStateChanged(
        playWhenReady: Boolean,
        playbackState: Int
    ) {
        status = when (playbackState) {
            Player.STATE_BUFFERING -> PlaybackStatus.LOADING
            Player.STATE_ENDED -> PlaybackStatus.STOPPED
            Player.STATE_IDLE -> PlaybackStatus.IDLE
            Player.STATE_READY -> if (playWhenReady) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
            else -> PlaybackStatus.IDLE
        }
        if (status != PlaybackStatus.IDLE) notificationManager!!.startNotify(status!!)

        //EventBus.getDefault().post(status); todo
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
    }

    override fun onLoadingChanged(isLoading: Boolean) {}
    override fun onPlayerError(error: ExoPlaybackException) {

        //EventBus.getDefault().post(PlaybackStatus.ERROR); todo
    }

    override fun onRepeatModeChanged(repeatMode: Int) {}
    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
    override fun onPositionDiscontinuity(reason: Int) {}
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
    override fun onSeekProcessed() {}

    private fun addMetadataListener(){
        exoPlayer!!.addMetadataOutput { metadata: Metadata ->
            for (i in 0 until metadata.length()) {
                if (metadata[i] is IcyInfo) {
                    metadataString = (metadata[i] as IcyInfo).title.toString()
                    notificationManager!!.setStrLiveBroadcast(metadataString!!)
                    notificationManager!!.startNotify(ACTION_PLAY)
                    updateNotificatioMetadata()
                }
            }
              sendBroadcast(Intent(META_CHANGED))
        }
    }

    fun updateNotificatioMetadata(){
        val cover = BitmapFactory.decodeResource(resources, R.drawable.divine_logo)
        mediaSession!!.setMetadata(
            MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, cover)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, cover)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, strAppName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataString)
                .build()
        )
    }

    fun play(streamUrl: String?) {
        synchronized(this){
            isPlayerStopped = false
            this.streamUrl = streamUrl
            val dataSourceFactory =
                DefaultDataSourceFactory(this, userAgent)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(streamUrl))
            exoPlayer!!.prepare(mediaSource)
            exoPlayer!!.playWhenReady = true

            sendBroadcast(Intent(MusicService.PLAY_STATE_CHANGED))
        }

    }

    fun resume() {
        if (streamUrl != null) play(streamUrl)
    }

    fun pause() {
        synchronized(this){
            isPlayerStopped = false
            exoPlayer!!.playWhenReady = false
            audioManager!!.abandonAudioFocus(this)
            sendBroadcast(Intent(MusicService.PLAY_STATE_CHANGED))
        }
    }

    fun stop() {
        synchronized(this){
            exoPlayer!!.stop()
            audioManager!!.abandonAudioFocus(this)
            isPlayerStopped = true
            sendBroadcast(Intent(AUDIO_STOPPED))
            println("Sabin service stopped")
        }

    }

    fun playOrPause(url: String) {
        if (streamUrl != null && streamUrl == url) {
            if (!isPlaying) {
                play(streamUrl)
            } else {
                pause()
            }
        } else {
            if (isPlaying) {
                pause()
            }
            play(url)
        }
        sendBroadcast(Intent(MusicService.PLAY_STATE_CHANGED))
    }

    val isPlaying: Boolean
        get() = status == PlaybackStatus.PLAYING || status == PlaybackStatus.LOADING

    private val userAgent: String
        private get() = Util.getUserAgent(this, javaClass.simpleName)


}