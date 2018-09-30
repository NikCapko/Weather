package com.example.nikolay.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherModel> getWeatherCity(@Query("q") String cityName, @Query("appid") String token, @Query("units") String units, @Query("lang") String lang);

    @GET("weather")
    Call<WeatherModel> getWeatherCoords(@Query("lat") Double lat, @Query("lon") Double lon, @Query("appid") String token, @Query("units") String units, @Query("lang") String lang);
}