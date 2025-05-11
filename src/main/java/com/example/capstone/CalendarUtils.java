package com.example.capstone;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for calendar-related operations.
 */
public class CalendarUtils {

    /**
     * Generates a list of dates for a given month and year, including leading null placeholders
     * to align the first day of the month with its corresponding day of the week.
     *
     * <p>This method is useful for rendering calendar grids where each week starts on Sunday
     * and the days of the month need to be properly aligned.</p>
     *
     * @param year  the year (e.g., 2025)
     * @param month the month (1 for January, 12 for December)
     * @return a list of {@link LocalDate} objects for each day in the month,
     *         prefixed with {@code null} values for alignment in a calendar grid
     */

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
