package com.example.nikolay.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Call<WeatherModel> getWeather(@Query("q") String cityName, @Query("appid") String token);
}
