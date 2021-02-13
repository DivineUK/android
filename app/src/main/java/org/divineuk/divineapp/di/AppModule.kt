package org.divineuk.divineapp.di


import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.gsonfire.GsonFireBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.divineuk.divineapp.data.DivineDataSource
import org.divineuk.divineapp.network.model.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC)
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val builder: GsonFireBuilder = GsonFireBuilder()
            .registerTypeSelector(Widget::class.java
            ) { readElement ->
                val kind = readElement.asJsonObject["type"].asString
                if (kind == "title") {
                    Title::class.java
                } else if (kind == "image") {
                    Image::class.java
                } else if (kind == "carousel") {
                    Carousel::class.java
                }else if (kind == "event") {
                    Event::class.java
                }else if (kind == "youtubeLink") {
                    YoutubeLink::class.java
                }else if (kind == "imageLink") {
                    ImageLink::class.java
                }else if (kind == "textLink") {
                    TextLink::class.java
                }else{
                    null
                }
            }
        val gson: Gson = builder.createGson()

        return Retrofit.Builder()
            .baseUrl(DivineApi.BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    gson
                )
            )
            .build()

    }


    @Provides
    @Singleton
    fun provideDivineApi(retrofit: Retrofit): DivineApi =
        retrofit.create(DivineApi::class.java)

    fun provideRemoteDataSource(divineApi: DivineApi) = DivineDataSource(divineApi)

}