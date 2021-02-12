package org.divineuk.divineapp.network.model

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface DivineApi{

    companion object{
        const val BASE_URL = "http://www.json-generator.com/api/json/get/" //TODO
    }

    @GET("cfTUqQtzYi?indent=2") //TODO
    suspend fun getHomePageContents() : Response<HomeContent>
}