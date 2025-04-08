package com.example.capstone;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarUtils {
    public static List<LocalDate> getMonthDates(int year, int month) {
        List<LocalDate> dates = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        DayOfWeek firstDayOfWeek = firstOfMonth.getDayOfWeek();

        int startOffset = firstDayOfWeek.getValue() % 7;

        for (int i = 1; i <= startOffset; i++) {
            dates.add(null);
        }

        for(int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            dates.add(LocalDate.of(year, month, day));
        }
        return dates;
    }

}
