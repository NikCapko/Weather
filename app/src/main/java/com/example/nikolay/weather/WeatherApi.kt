package com.example.nikolay.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    fun getWeatherCity(@Query("q") cityName: String, @Query("appid") token: String, @Query("units") units: String, @Query("lang") lang: String): Call<WeatherModel>

    @GET("weather")
    fun getWeatherCoords(@Query("lat") lat: Double?, @Query("lon") lon: Double?, @Query("appid") token: String, @Query("units") units: String, @Query("lang") lang: String): Call<WeatherModel>
}