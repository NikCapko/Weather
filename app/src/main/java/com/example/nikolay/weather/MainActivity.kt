package com.example.nikolay.weather

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*

import butterknife.BindView
import butterknife.ButterKnife

import com.squareup.picasso.Picasso

import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.io.IOException
import java.text.DateFormat
import java.util.Date
import com.example.nikolay.weather.R.layout.activity_main as activity_main1

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "MainActivity.TAG"
    private val SAVE_CITY = "MainActivity.SAVE_CITY"

    private var city: String = "Simferopol"
    private var LANG = "en"
    private val UNITS = "metric"

    private var coords: Coord? = null
    private var messages: Call<WeatherModel>? = null

    private var sPref: SharedPreferences? = null

    private var token: String = Token.TOKEN

    @BindView(R.id.city_field)
    lateinit var cityField: TextView
    @BindView(R.id.updated_field)
    lateinit var updatedField: TextView
    @BindView(R.id.progress_bar)
    lateinit var progressBar: ProgressBar
    @BindView(R.id.iv_icon)
    lateinit var ivIcon: ImageView
    @BindView(R.id.current_temperature_field)
    lateinit var currentTemperatureField: TextView
    @BindView(R.id.details_field)
    lateinit var detailsField: TextView
    @BindView(R.id.swipe_container)
    lateinit var swipeContainer: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main1)
        ButterKnife.bind(this)
        LANG = getString(R.string.lang)
        sPref = getPreferences(Context.MODE_PRIVATE)
        swipeContainer.setOnRefreshListener(this)
        loadCity()
        getWeatherCity()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_city -> inputCity()
            R.id.change_coords -> inputCoords()
            R.id.update -> getWeatherCity()
        }
        return false
    }

    internal fun getWeatherCity() {
        progressBar.visibility = ProgressBar.VISIBLE
        messages = App.api?.getWeatherCity(city, token, UNITS, LANG)
        refresh()
        progressBar.visibility = ProgressBar.GONE
    }

    internal fun getWeatherCoords(coordsWeather: Coord?) {
        progressBar.visibility = ProgressBar.VISIBLE
        messages = App.api?.getWeatherCoords(coordsWeather!!.lat, coordsWeather.lon, token, UNITS, LANG)
        refresh()
        progressBar.visibility = ProgressBar.GONE
    }

    override fun onRefresh() {
        swipeContainer.isRefreshing = true
        Handler().postDelayed({ refresh() }, 3000)
        swipeContainer.isRefreshing = false
    }

    private fun refresh() {
        if (!messages!!.isExecuted) {
            messages!!.enqueue(object : Callback<WeatherModel> {
                @SuppressLint("SetTextI18n", "DefaultLocale")
                override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {
                    val weatherCity = response.body()
                    initView(weatherCity)
                }

                override fun onFailure(call: Call<WeatherModel>, t: Throwable) {
                    Log.d(TAG, t.message)
                    val toast = Toast.makeText(applicationContext, R.string.error_net, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
            })
        }
    }

    private fun initView(weatherCity: WeatherModel?) {
        if (weatherCity != null) {
            city = weatherCity.name.toString()
            coords = weatherCity.coord
            cityField.text = weatherCity.name + ", " + (weatherCity.sys?.country ?: "")
            detailsField.setText((weatherCity.weather?.get(0)?.description?.toUpperCase() ?: "") +
                    "\n" + getString(R.string.humidity) + " " + (weatherCity.main?.humidity
                    ?: "") + "%" +
                    "\n" + getString(R.string.pressure) + " " + String.format("%.2f", weatherCity.main?.pressure!! * 0.75) + " " + applicationContext.getString(R.string.mmHg) +
                    "\n" + getString(R.string.cloud) + " " + (weatherCity.clouds?.all
                    ?: "") + " %" +
                    "\n" + getString(R.string.wind) + " " + convertRumbToDirection(getWindDeg(weatherCity.wind?.deg!!)) + " " + weatherCity.wind?.speed + " " + applicationContext.getString(R.string.meter_per_second))
            currentTemperatureField.setText(String.format("%.2f", weatherCity.main?.temp) + " ℃")
            val df = DateFormat.getDateTimeInstance()
            val updatedOn = df.format(Date(weatherCity.dt * 1000))
            updatedField.text = getString(R.string.last_update) + " " + updatedOn
            Picasso.get().load("http://openweathermap.org/img/w/" + (weatherCity.weather?.get(0)?.icon
                    ?: "") + ".png").into(ivIcon)
        } else {
            cityField.text = ""
            detailsField.text = ""
            currentTemperatureField.text = ""
            updatedField.text = ""
            val builder = AlertDialog.Builder(applicationContext)
            builder.setTitle(R.string.error)
            builder.setMessage(R.string.error_city)
            builder.setPositiveButton(R.string.ok, null)
            builder.show()
        }
    }

    internal fun getWindDeg(deg: Double): Int {
        return if ((deg * 0.088889).toInt() % 2 == 0) (deg * 0.088889).toInt() else (deg * 0.088889).toInt() + 1
    }

    private fun convertRumbToDirection(rumb: Int): String {
        when (rumb) {
            0 -> return getString(R.string.wind_direction_nord)
            2 -> return getString(R.string.wind_direction_nord_nord_east)
            4 -> return getString(R.string.wind_direction_nord_east)
            6 -> return getString(R.string.wind_direction_east_nord_east)
            8 -> return getString(R.string.wind_direction_east)
            10 -> return getString(R.string.wind_direction_east_south_east)
            12 -> return getString(R.string.wind_direction_south_east)
            14 -> return getString(R.string.wind_direction_south_south_east)
            16 -> return getString(R.string.wind_direction_south)
            18 -> return getString(R.string.wind_direction_south_south_west)
            20 -> return getString(R.string.wind_direction_south_west)
            22 -> return getString(R.string.wind_direction_west_south_west)
            24 -> return getString(R.string.wind_direction_west)
            26 -> return getString(R.string.wind_direction_west_nord_west)
            28 -> return getString(R.string.wind_direction_nord_west)
            30 -> return getString(R.string.wind_direction_nord_nord_west)
            32 -> return getString(R.string.wind_direction_nord)
            else -> return rumb.toString()
        }
    }

    private fun inputCity() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.change_city)
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(city)
        builder.setView(input)
        builder
                .setPositiveButton(R.string.ok) { _, _ ->
                    city = if (input.text.toString().isNotEmpty()) input.text.toString() else city
                    saveCity()
                    getWeatherCity()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
        builder.show()
    }

    private fun inputCoords() {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        builder.setTitle(R.string.change_coords)
        val view = inflater.inflate(R.layout.input_coords, null)
        val inputLat = view.findViewById<View>(R.id.input_lat) as EditText
        val inputLon = view.findViewById<View>(R.id.input_lon) as EditText
        inputLat.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputLon.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputLat.setText(coords!!.lat!!.toString())
        inputLon.setText(coords!!.lon!!.toString())
        builder.setView(view)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (inputLat.text.toString().isNotEmpty() && inputLon.text.toString().isNotEmpty()) {
                        val lat = java.lang.Double.valueOf(inputLat.text.toString())
                        val lon = java.lang.Double.valueOf(inputLon.text.toString())
                        coords!!.lat = lat
                        coords!!.lon = lon
                        getWeatherCoords(coords)
                    }
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
        builder.show()
    }

    internal fun saveCity() {
        val ed = sPref!!.edit()
        ed.putString(SAVE_CITY, city)
        ed.apply()
    }

    internal fun loadCity() {
        city = sPref?.getString(SAVE_CITY, "Simferopol") ?: "Simferopol"
    }
}
