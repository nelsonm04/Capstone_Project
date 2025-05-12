package com.example.capstone;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

class MainScreenTest {

    private MainScreen mainScreen;

    @BeforeAll
    static void initJavaFx() {
        // Boot JavaFX toolkit so controls can be created in tests
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ex) {
            // already initialized
        }
    }

    // Reflection helper
    private void inject(String fieldName, Object value) throws Exception {
        Field f = MainScreen.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(mainScreen, value);
    }

    @Test
    void setUsername() {
        mainScreen.setUsername("Bob");
        Label lbl = (Label) getField("usernameDisplay");
        assertEquals("Bob", lbl.getText(), "setUsername should update the label text");
    }



    @Test
    void getCurrentUser() {
        // simply verify it matches the stub we set in setUp
        User current = MainScreen.getCurrentUser();
        assertNotNull(current, "CurrentUser should never be null after setUp");
        assertEquals("fake-uid", current.getUid());
    }

    @Test
    void initialize() {
        // initialize should set the monthYear label to today’s month and year
        mainScreen.initialize(null, null);
        Label lbl = (Label) getField("monthYear");
        LocalDate now = LocalDate.now();
        String expected = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + now.getYear();
        assertEquals(expected, lbl.getText());
    }

    @Test
    void handleSignOut() {
        // We just verify it doesn't throw NPE (full UI navigation is out of scope)
        assertDoesNotThrow(() -> mainScreen.handleSignOut(null));
    }

    @Test
    void loadCalendar() throws Exception {
        // loadCalendar must at least render the 7 day‐name headers
        mainScreen.loadCalendar(2025, 5);
        GridPane grid = (GridPane) getField("calendarGrid");
        String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        for (String day : days) {
            boolean found = grid.getChildren().stream()
                    .filter(n -> n instanceof Label)
                    .map(n -> ((Label)n).getText())
                    .anyMatch(text -> text.equals(day));
            assertTrue(found, "Expected header: " + day);
        }
    }

    // Helper to pull any private field by name
    private Object getField(String name) {
        try {
            Field f = MainScreen.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(mainScreen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
