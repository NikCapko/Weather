package com.example.nikolay.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherModel> getWeatherCity(@Query("q") String cityName);
    @GET("weather")
    Call<WeatherModel> getWeatherCoords(@Query("lat") Double lat, @Query("lon") Double lon);
    @GET("forecast")
    Call<WeatherModel> getWeatherCityForecast(@Query("q") String cityName);
    @GET("forecast")
    Call<WeatherModel> getWeatherCoordsForecast(@Query("lat") Double lat, @Query("lon") Double lon);
}