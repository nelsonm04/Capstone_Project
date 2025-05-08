package com.example.capstone;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HolidayAPI {

    private static final String API_KEY = "lHx2ydz4iPwsCQQRSAQ1PM0HUSXZIPfL";

    public static Map<LocalDate, String> fetchUSHolidays(int year) {
        Map<LocalDate, String> holidayMap = new HashMap<>();
        try {
            String apiUrl = String.format(
                    "https://calendarific.com/api/v2/holidays?api_key=%s&country=US&year=%d&type=national",
                    API_KEY, year
            );


            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            JsonObject root = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            JsonArray holidays = root
                    .getAsJsonObject("response")
                    .getAsJsonArray("holidays");

            for (JsonElement el : holidays) {
                JsonObject holiday = el.getAsJsonObject();
                String name = holiday.get("name").getAsString();
                String dateStr = holiday
                        .getAsJsonObject("date")
                        .get("iso")
                        .getAsString();
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10)); // Truncate time
                holidayMap.put(date, name);
            }

        } catch (Exception e) {
            System.out.println("Failed to fetch Calendarific holidays: " + e.getMessage());
        }

        return holidayMap;
    }
}
