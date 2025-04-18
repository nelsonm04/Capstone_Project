package com.example.capstone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherAPI {


    private static final String API_KEY = "988c6987d3427577d66fec2686876d61";

    public static String getWeather(String city) {
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    city + "&appid=" + API_KEY + "&units=metric";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            String weather = jsonObject
                    .getAsJsonArray("weather")
                    .get(0)
                    .getAsJsonObject()
                    .get("description")
                    .getAsString();

            double temp = jsonObject
                    .getAsJsonObject("main")
                    .get("temp")
                    .getAsDouble();

            return "Weather in " + city + ": " + weather + ", " + temp + "°C";

        } catch (Exception e) {
            e.printStackTrace();
            return "Could not fetch weather data.";
        }
    }
}

