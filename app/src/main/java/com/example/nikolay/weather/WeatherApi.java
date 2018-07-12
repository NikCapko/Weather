package com.example.nikolay.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather?units=metric&lang=ru")
    Call<WeatherModel> getWeatherCity(@Query("q") String cityName, @Query("appid") String token);
    @GET("weather?units=metric&lang=ru")
    Call<WeatherModel> getWeatherCoords(@Query("lat") Double lat, @Query("lon") Double lon, @Query("appid") String token);
}