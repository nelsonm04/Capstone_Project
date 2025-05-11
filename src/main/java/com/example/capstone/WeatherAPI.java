package com.example.capstone;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Provides functionality for fetching weather information from the OpenWeatherMap API.
 * This class includes a method to retrieve the current weather description and temperature
 * for a given city.
 */
public class WeatherAPI {

    // API key for accessing OpenWeatherMap API
    private static final String API_KEY = "988c6987d3427577d66fec2686876d61";

    /**
     * Fetches the current weather information for a given city.
     * Makes a request to the OpenWeatherMap API to get the weather description and temperature
     * for the specified city.
     *
     * @param city The name of the city for which weather data is to be fetched.
     * @return A string containing the weather description and temperature of the city.
     *         If an error occurs while fetching the data, returns a message indicating the failure.
     */
    public static String getWeather(String city) {
        try {
            // Constructing the API URL with the city and API key
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                    city + "&appid=" + API_KEY + "&units=metric";

            // Creating a URL object from the string and opening a connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Reading the response from the API
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Extracting weather description and temperature from the response
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

            // Returning the formatted weather information
            return "Weather in " + city + ": " + weather + ", " + temp + "°C";

        } catch (Exception e) {
            // Handling errors and printing stack trace
            e.printStackTrace();
            return "Could not fetch weather data.";
        }
    }
}
