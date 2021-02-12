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

package org.divineuk.divineapp.audio.service;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.divineuk.divineapp.R;
import org.divineuk.divineapp.audio.NetworkAudio;
import org.divineuk.divineapp.audio.helper.MusicPlayerRemote;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author Karim Abou Zeid (kabouzeid), Andrew Neal
 */
public class MusicService extends Service {

    public static final String TAG = MusicService.class.getSimpleName();

    public static final String RETRO_MUSIC_PACKAGE_NAME = "org.divineuk.divineapp";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String ACTION_TOGGLE_PAUSE = RETRO_MUSIC_PACKAGE_NAME + ".togglepause";
    public static final String ACTION_PLAY = RETRO_MUSIC_PACKAGE_NAME + ".play";
    public static final String ACTION_PLAY_PLAYLIST = RETRO_MUSIC_PACKAGE_NAME + ".play.playlist";
    public static final String ACTION_PAUSE = RETRO_MUSIC_PACKAGE_NAME + ".pause";
    public static final String ACTION_STOP = RETRO_MUSIC_PACKAGE_NAME + ".stop";
    public static final String ACTION_SKIP = RETRO_MUSIC_PACKAGE_NAME + ".skip";
    public static final String ACTION_REWIND = RETRO_MUSIC_PACKAGE_NAME + ".rewind";
    public static final String ACTION_QUIT = RETRO_MUSIC_PACKAGE_NAME + ".quitservice";
    public static final String ACTION_PENDING_QUIT = RETRO_MUSIC_PACKAGE_NAME + ".pendingquitservice";
    
    // Do not change these three strings as it will break support with other apps (e.g. last.fm
    // scrobbling)
    public static final String META_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".metachanged";
    public static final String QUEUE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".queuechanged";
    public static final String PLAY_STATE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".playstatechanged";
    public static final String FAVORITE_STATE_CHANGED =
            RETRO_MUSIC_PACKAGE_NAME + "favoritestatechanged";
    public static final String REPEAT_MODE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".repeatmodechanged";
    public static final String SHUFFLE_MODE_CHANGED =
            RETRO_MUSIC_PACKAGE_NAME + ".shufflemodechanged";
    public static final String MEDIA_STORE_CHANGED = RETRO_MUSIC_PACKAGE_NAME + ".mediastorechanged";
    public static final String CYCLE_REPEAT = RETRO_MUSIC_PACKAGE_NAME + ".cyclerepeat";
    public static final String TOGGLE_SHUFFLE = RETRO_MUSIC_PACKAGE_NAME + ".toggleshuffle";
    public static final String TOGGLE_FAVORITE = RETRO_MUSIC_PACKAGE_NAME + ".togglefavorite";
    public static final String SAVED_POSITION = "POSITION";
    public static final String SAVED_POSITION_IN_TRACK = "POSITION_IN_TRACK";
    public static final String SAVED_SHUFFLE_MODE = "SHUFFLE_MODE";
    public static final String SAVED_REPEAT_MODE = "REPEAT_MODE";
    public static final int RELEASE_WAKELOCK = 0;
    public static final int TRACK_ENDED = 1;
    public static final int TRACK_WENT_TO_NEXT = 2;
    public static final int PLAY_SONG = 3;
    public static final int PREPARE_NEXT = 4;
    public static final int SET_POSITION = 5;
    public static final int FOCUS_CHANGE = 6;
    public static final int DUCK = 7;
    public static final int UNDUCK = 8;
    public static final int RESTORE_QUEUES = 9;
    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;
    public static final int SAVE_QUEUES = 0;
    private static final long MEDIA_SESSION_ACTIONS =
            PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_SEEK_TO;
    private final IBinder musicBind = new MusicBinder();
    public int nextPosition = -1;

    public boolean pendingQuit = false;

    @Nullable
    public Playback playback;

    public int position = -1;

    private AudioManager audioManager;
    private IntentFilter becomingNoisyReceiverIntentFilter =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private boolean becomingNoisyReceiverRegistered;
    private IntentFilter bluetoothConnectedIntentFilter =
            new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
    private boolean bluetoothConnectedRegistered = false;
    private IntentFilter headsetReceiverIntentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    private boolean headsetReceiverRegistered = false;
    private MediaSessionCompat mediaSession;
    private ContentObserver mediaStoreObserver;
    private HandlerThread musicPlayerHandlerThread;
    private boolean notHandledMetaChangedForCurrentTrack;
    public NetworkAudio networkAudio;
    private boolean pausedByTransientLossOfFocus;

    private final BroadcastReceiver becomingNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, @NonNull Intent intent) {
                    if (intent.getAction() != null
                            && intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                        pause();
                    }
                }
            };

    private PlaybackHandler playerHandler;

    private final AudioManager.OnAudioFocusChangeListener audioFocusListener =
            new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(final int focusChange) {
                    playerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
                }
            };

    //private PlayingNotification playingNotification; Todo
    private final BroadcastReceiver updateFavoriteReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    updateNotification();
                }
            };

    private HandlerThread queueSaveHandlerThread;
    private boolean queuesRestored;
    private int repeatMode;
    private int shuffleMode;
    /*private final BroadcastReceiver bluetoothReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    String action = intent.getAction();
                    if (action != null) {
                        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)
                                && PreferenceUtil.INSTANCE.isBluetoothSpeaker()) {
                            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                                if (getAudioManager().getDevices(AudioManager.GET_DEVICES_OUTPUTS).length > 0) {
                                    play();
                                }
                            } else {
                                if (getAudioManager().isBluetoothA2dpOn()) {
                                    play();
                                }
                            }
                        }
                    }
                }
            };*/
    private PhoneStateListener phoneStateListener =
            new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            // Not in call: Play music
                            play();
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            // A call is dialing, active or on hold
                            pause();
                            break;
                        default:
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
    private BroadcastReceiver headsetReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null) {
                        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                            int state = intent.getIntExtra("state", -1);
                            switch (state) {
                                case 0:
                                    pause();
                                    break;
                                case 1:
                                    play();
                                    break;
                            }
                        }
                    }
                }
            };
    private Handler uiThreadHandler;
    private PowerManager.WakeLock wakeLock;

    private static Bitmap copy(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.RGB_565;
        }
        try {
            return bitmap.copy(config, false);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setNetworkAudio(NetworkAudio networkAudio){
        this.networkAudio = networkAudio;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        final TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        }
        wakeLock.setReferenceCounted(false);

        musicPlayerHandlerThread = new HandlerThread("PlaybackHandler");
        musicPlayerHandlerThread.start();
        playerHandler = new PlaybackHandler(this, musicPlayerHandlerThread.getLooper());

        playback = new AudioPlayer(this);

        setupMediaSession();

        // queue saving needs to run on a separate thread so that it doesn't block the playback handler
        // events
        queueSaveHandlerThread =
                new HandlerThread("QueueSaveHandler", Process.THREAD_PRIORITY_BACKGROUND);
        queueSaveHandlerThread.start();

        uiThreadHandler = new Handler();

        initNotification();


        restoreState();

        sendBroadcast(new Intent("code.name.monkey.retromusic.RETRO_MUSIC_SERVICE_CREATED"));

        registerHeadsetEvents();
        registerBluetoothConnected();
    }

    @Override
    public void onDestroy() {
        if (becomingNoisyReceiverRegistered) {
            unregisterReceiver(becomingNoisyReceiver);
            becomingNoisyReceiverRegistered = false;
        }
        if (headsetReceiverRegistered) {
            unregisterReceiver(headsetReceiver);
            headsetReceiverRegistered = false;
        }
        if (bluetoothConnectedRegistered) {
            //unregisterReceiver(bluetoothReceiver);
            bluetoothConnectedRegistered = false;
        }
        mediaSession.setActive(false);
        quit();
        releaseResources();
        getContentResolver().unregisterContentObserver(mediaStoreObserver);
        //PreferenceUtil.INSTANCE.unregisterOnSharedPreferenceChangedListener(this);
        wakeLock.release();

        sendBroadcast(new Intent("org.divineuk.divineapp.RETRO_MUSIC_SERVICE_DESTROYED"));
    }

    public void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }

    @NonNull
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(SET_POSITION);
        playerHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget();
    }

    public int getSongDurationMillis() {
        if (playback != null) {
            return playback.duration();
        }
        return -1;
    }

    public int getSongProgressMillis() {
        if (playback != null) {
            return playback.position();
        }
        return -1;
    }


    public void initNotification() {
        /*if (VERSION.SDK_INT >= VERSION_CODES.N
                && !PreferenceUtil.INSTANCE.isClassicNotification()) {
            playingNotification = new PlayingNotificationImpl();
        } else {
            playingNotification = new PlayingNotificationOreo();
        }
        playingNotification.init(this);*/
    }

    public boolean isPausedByTransientLossOfFocus() {
        return pausedByTransientLossOfFocus;
    }

    public void setPausedByTransientLossOfFocus(boolean pausedByTransientLossOfFocus) {
        this.pausedByTransientLossOfFocus = pausedByTransientLossOfFocus;
    }

    public boolean isPlaying() {
        return playback != null && playback.isPlaying();
    }


    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }



    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_TOGGLE_PAUSE:
                    if (isPlaying()) {
                        pause();
                    } else {
                        play();
                    }
                    break;
                case ACTION_PAUSE:
                    pause();
                    break;
                case ACTION_PLAY:
                    play();
                    break;
                case ACTION_STOP:
                case ACTION_QUIT:
                    pendingQuit = false;
                    quit();
                    break;
                case ACTION_PENDING_QUIT:
                    pendingQuit = true;
                    break;

            }
        }

        return START_NOT_STICKY;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        if (!isPlaying()) {
            stopSelf();
        }
        return true;
    }



    public void pause() {
        pausedByTransientLossOfFocus = false;
        if (playback != null && playback.isPlaying()) {
            playback.pause();
        }
    }

    public void play() {
        openCurrent();
        synchronized (this) {
            if (requestFocus()) {
                if (playback != null && !playback.isPlaying()) {
                    if (!playback.isInitialized()) {
                        playSongAt(getPosition());
                    } else {
                        playback.start();
                        if (!becomingNoisyReceiverRegistered) {
                            registerReceiver(becomingNoisyReceiver, becomingNoisyReceiverIntentFilter);
                            becomingNoisyReceiverRegistered = true;
                        }
                        if (notHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(META_CHANGED);
                            notHandledMetaChangedForCurrentTrack = false;
                        }
                        // fixes a bug where the volume would stay ducked because the
                        // AudioManager.AUDIOFOCUS_GAIN event is not sent
                        playerHandler.removeMessages(DUCK);
                        playerHandler.sendEmptyMessage(UNDUCK);
                    }
                }
            } else {
                Toast.makeText(
                        this, getResources().getString(R.string.audio_focus_denied), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    public void playSongAt(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(PLAY_SONG);
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget();
    }

    public void playSongAtImpl(int position) {
            play();
    }


    public void quit() {
        pause();
        //playingNotification.stop();

        closeAudioEffectSession();
        getAudioManager().abandonAudioFocus(audioFocusListener);
        stopSelf();
    }

    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }


    
    public void runOnUiThread(Runnable runnable) {
        uiThreadHandler.post(runnable);
    }


  
    public int seek(int millis) {
        synchronized (this) {
            try {
                int newPosition = 0;
                if (playback != null) {
                    newPosition = playback.seek(millis);
                }
                return newPosition;
            } catch (Exception e) {
                return -1;
            }
        }
    }


    public void updateMediaSessionPlaybackState() {
        PlaybackStateCompat.Builder stateBuilder =
                new PlaybackStateCompat.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(
                                isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                                getSongProgressMillis(),
                                1);

        mediaSession.setPlaybackState(stateBuilder.build());
    }

    public void updateNotification() {
        /*if (playingNotification != null && getCurrentSong().getId() != -1) {
            playingNotification.update();
        }*/
    }

    public void updateMediaSessionMetaData() {
        Log.i(TAG, "onResourceReady: ");
        final NetworkAudio song = getCurrentSong();


//        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtistName())
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.getArtistName())
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbumName())
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
//                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
//                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getPosition() + 1)
//                .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.getYear())
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);
//
//
//
//            mediaSession.setMetadata(metaData.build());
    }

    private void closeAudioEffectSession() {
        final Intent audioEffectsIntent =
                new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        if (playback != null) {
            audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playback.getAudioSessionId());
        }
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
    }

    private AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private void handleChangeInternal(@NonNull final String what) {
        switch (what) {
            case PLAY_STATE_CHANGED:
                updateNotification();
                updateMediaSessionPlaybackState();
                final boolean isPlaying = isPlaying();
                break;
            case FAVORITE_STATE_CHANGED:
            case META_CHANGED:
                updateNotification();
                updateMediaSessionMetaData();
                savePosition();
                break;

        }
    }



    private boolean openCurrent() {
        synchronized (this) {
            try {
                if (playback != null) {
                    return playback.setDataSource(getCurrentSong().getStreamUrl());
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    private NetworkAudio getCurrentSong() {
       return networkAudio;
    }


    private void registerBluetoothConnected() {
        Log.i(TAG, "registerBluetoothConnected: ");
        if (!bluetoothConnectedRegistered) {
            //registerReceiver(bluetoothReceiver, bluetoothConnectedIntentFilter);
            bluetoothConnectedRegistered = true;
        }
    }

    private void registerHeadsetEvents() {
        if (!headsetReceiverRegistered
               // &&  PreferenceUtil.INSTANCE.isHeadsetPlugged()
        ) {
            registerReceiver(headsetReceiver, headsetReceiverIntentFilter);
            headsetReceiverRegistered = true;
        }
    }

    private void releaseResources() {
        playerHandler.removeCallbacksAndMessages(null);
        musicPlayerHandlerThread.quitSafely();
        if (playback != null) {
            playback.release();
        }
        playback = null;
        mediaSession.release();
    }

    private boolean requestFocus() {
        return (getAudioManager()
                .requestAudioFocus(
                        audioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private void restoreState() {
        shuffleMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_SHUFFLE_MODE, 0);
        repeatMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SAVED_REPEAT_MODE, 0);
        playerHandler.removeMessages(RESTORE_QUEUES);
        playerHandler.sendEmptyMessage(RESTORE_QUEUES);
    }

    private void savePosition() {
        /*PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(SAVED_POSITION, getPosition())
                .apply();*/
    }

    private void setupMediaSession() {
        ComponentName mediaButtonReceiverComponentName =
                new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);

        PendingIntent mediaButtonReceiverPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

        mediaSession =
                new MediaSessionCompat(
                        this,
                        "RetroMusicPlayer",
                        mediaButtonReceiverComponentName,
                        mediaButtonReceiverPendingIntent);
        MediaSessionCallback mediasessionCallback =
                new MediaSessionCallback(getApplicationContext(), this);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediasessionCallback);
        mediaSession.setActive(true);
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
    }


    public class MusicBinder extends Binder {

        @NonNull
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
