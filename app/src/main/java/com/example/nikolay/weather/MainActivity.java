package com.example.nikolay.weather;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import static com.example.nikolay.weather.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    public static String city = "Moscow";
    final String SAVED_TEXT = "saved_text";
    public String TAG = "TAG";
    public String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private String LANG = "en";
    private String UNITS = "metric";

    String token = Token.getToken();

    SharedPreferences sPref;

    Coord coords;

    Retrofit retrofit;
    WeatherApi weatherApi;

    RecyclerView recyclerView;

    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        loadCity();
        LANG = getString(R.string.lang);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor).build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherApi.class);
        getWeatherCity();
    }

    private void getWeatherCity() {
        Call<WeatherModel> messages = weatherApi.getWeatherCityForecast(city, token, UNITS, LANG);
        //Log.d(TAG, "<-- request " + messages.request().url().toString());
        updateInfo(messages);
    }

    private void getWeatherCoords(Coord coordsWeather) {
        Call<WeatherModel> messages = weatherApi.getWeatherCoordsForecast(coordsWeather.getLat(), coordsWeather.getLon(), token, UNITS, LANG);
        updateInfo(messages);
    }

    void updateInfo(Call<WeatherModel> messages) {
        messages.enqueue(new Callback<WeatherModel>() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onResponse(@NonNull Call<WeatherModel> call, @NonNull Response<WeatherModel> response) {
                //Log.d(TAG, "<-- response " + response.raw().body());
                WeatherModel weatherCity = response.body();

                if (weatherCity != null) {

                    city = weatherCity.getCity().getName();

                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                    Adapter adapter = new Adapter(weatherCity.getList(), city, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherModel> call, @NonNull Throwable t) {
                Log.d(TAG, t.getMessage());
                Toast toast = Toast.makeText(MainActivity.this, R.string.error_net, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_city:
                inputCity();
                break;
            case R.id.change_coords:
                inputCoords();
                break;
            case R.id.update:
                getWeatherCity();
                break;
        }
        return false;
    }

    private void inputCity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_city);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(city);
        builder.setView(input);
        builder
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        city = input.getText().toString();
                        getWeatherCity();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ;
                    }
                });
        builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void inputCoords() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle(R.string.change_coords);
        final View view = inflater.inflate(R.layout.input_coords, null);
        final EditText inputLat = (EditText) view.findViewById(R.id.input_lat);
        final EditText inputLon = (EditText) view.findViewById(R.id.input_lon);
        inputLat.setText(coords.getLat().toString());
        inputLon.setText(coords.getLon().toString());
        builder.setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double lat = Double.valueOf(inputLat.getText().toString());
                        double lon = Double.valueOf(inputLon.getText().toString());
                        coords.setLat(lat);
                        coords.setLon(lon);
                        getWeatherCoords(coords);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    void saveCity() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_TEXT, city);
        ed.apply();
        getWeatherCity();
    }

    void loadCity() {
        sPref = getPreferences(MODE_PRIVATE);
        city = sPref.getString(SAVED_TEXT, city);
    }

    @Override
    protected void onDestroy() {
        saveCity();
        super.onDestroy();
    }
}
