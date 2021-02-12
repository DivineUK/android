package org.divineuk.divineapp.audio.old

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.divineuk.divineapp.audio.old.RadioService.LocalBinder

class RadioManager private constructor(private val context: Context) {
    private var serviceBound = false

    fun playOrPause(streamUrl: String) {
        service!!.playOrPause(streamUrl)
    }

    fun getService(): RadioService?{
        return service
    }

    val isPlaying: Boolean
        get() = service!!.isPlaying

    fun bind() {
        val intent = Intent(context, RadioService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        //if(service != null)
        //  EventBus.getDefault().post(service.getStatus()); //TODO
    }

    fun unbind() {
        context.unbindService(serviceConnection)
    }

     val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, binder: IBinder) {
            service = (binder as LocalBinder).service
            serviceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceBound = false
        }
    }

    companion object {
        private var instance: RadioManager? = null
        var service: RadioService? = null
            private set

        fun with(context: Context): RadioManager {
            if (instance == null) instance = RadioManager(context)
            return instance as RadioManager
        }


    }


}