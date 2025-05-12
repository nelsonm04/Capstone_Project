package com.example.capstone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Provides functionality for fetching weather information from the OpenWeatherMap API.
 * This class includes a method to retrieve the current weather description and temperature
 * for a given city.
 */


/**
 * Provides functionality for fetching weather information from the OpenWeatherMap API.
 * This class includes a method to retrieve the current weather description and temperature
 * for a given city.
 */
public class WeatherAPI {
    private static final String API_KEY = "988c6987d3427577d66fec2686876d61";

    public static String getWeatherFahrenheit(String city) {
        try {
            // URL-encode your city name
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            // imperial = Fahrenheit
            String urlString = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=imperial",
                    encodedCity, API_KEY
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            InputStreamReader reader;
            if (code == 200) {
                reader = new InputStreamReader(conn.getInputStream());
            } else {
                reader = new InputStreamReader(conn.getErrorStream());
                JsonObject err = JsonParser.parseReader(reader).getAsJsonObject();
                return "API error: " + err.get("message").getAsString();
            }

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            String weatherDesc = json
                    .getAsJsonArray("weather")
                    .get(0).getAsJsonObject()
                    .get("description").getAsString();
            double tempF = json
                    .getAsJsonObject("main")
                    .get("temp").getAsDouble();

            return String.format("Weather in %s: %s, %.1f°F", city, weatherDesc, tempF);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }
}
