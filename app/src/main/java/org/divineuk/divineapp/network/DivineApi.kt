package org.divineuk.divineapp.network.model

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface DivineApi{

    companion object{
        const val BASE_URL = "https://divineuk.github.io/api/test/" //TODO
    }

    @GET("home_new.json") //TODO
    suspend fun getHomePageContents() : Response<HomeContent>
}