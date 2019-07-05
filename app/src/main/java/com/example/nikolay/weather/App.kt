package com.example.nikolay.weather

import android.app.Application

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    private val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    private var retrofit: Retrofit? = null

    override fun onCreate() {
        super.onCreate()

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        val retrofitBuilder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
        if (BuildConfig.DEBUG)
            retrofitBuilder.client(client)

        retrofit = retrofitBuilder.build()

        api = retrofit!!.create(WeatherApi::class.java)
    }

    companion object {
        var api: WeatherApi? = null
            private set
    }
}
