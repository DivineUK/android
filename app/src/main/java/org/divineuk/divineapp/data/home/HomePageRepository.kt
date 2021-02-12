package org.divineuk.divineapp.data.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.divineuk.divineapp.data.DivineDataSource
import org.divineuk.divineapp.data.Resource
import org.divineuk.divineapp.network.model.DivineApi
import org.divineuk.divineapp.network.model.HomeContent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class HomePageRepository @Inject constructor(
    private val remoteDivineDataSource:DivineDataSource
){

    fun getHomePageContent() =
        liveData<Resource<HomeContent>>(Dispatchers.IO) {
            emit(Resource.loading())
            if(remoteDivineDataSource.getHomeContents().status==Resource.Status.SUCCESS){
                emitSource(MutableLiveData(remoteDivineDataSource.getHomeContents()))
            }else if(remoteDivineDataSource.getHomeContents().status==Resource.Status.ERROR){
                emit(Resource.error(remoteDivineDataSource.getHomeContents().message!!))
            }
    }


}