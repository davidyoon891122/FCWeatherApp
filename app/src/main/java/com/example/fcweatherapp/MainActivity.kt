package com.example.fcweatherapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fcweatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        service.getVillageForecase(
            serviceKey = getString(R.string.weather_api_key),
            baseDate = "20241009",
            baseTime = "2000",
            nx = 55,
            ny = 127,
        ).enqueue(object : Callback<WeatherEntity> {
            override fun onResponse(p0: Call<WeatherEntity>, p1: Response<WeatherEntity>) {
                val forecastDateTimeMap = mutableMapOf<String, Forecast>()
                val forecastList = p1.body()?.response?.body?.items?.forecastEntities.orEmpty()

                for (forecast in forecastList) {

                    if(forecastDateTimeMap["${forecast.forecastDate} / ${forecast.forecastTime}"] == null) {
                        forecastDateTimeMap["${forecast.forecastDate} / ${forecast.forecastTime}"] = Forecast(
                            forecastDate = forecast.forecastDate,
                            forecastTime = forecast.forecastTime,
                        )
                    }
                    forecastDateTimeMap["${forecast.forecastDate} / ${forecast.forecastTime}"]?.apply {
                        when(forecast.category) {
                            Category.POP -> precipitation = forecast.forecastValue.toInt()
                            Category.PTY -> precipitationType = transformRainType(forecast)
                            Category.SKY -> sky = transformSkyType(forecast)
                            Category.TMP -> temperature = forecast.forecastValue.toDouble()
                            else -> {}
                        }
                    }
                }

                Log.e("Forecast", forecastDateTimeMap.toString())

            }

            override fun onFailure(p0: Call<WeatherEntity>, p1: Throwable) {
                p1.printStackTrace()
            }
        })
    }

    private fun transformRainType(forecast: ForecastEntity): String{
        return when(forecast.forecastValue.toInt()) {
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }
    }

    private fun transformSkyType(forecast: ForecastEntity): String {
        return when(forecast.forecastValue.toInt()) {
            1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> ""
        }
    }
}