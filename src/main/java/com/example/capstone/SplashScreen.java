package com.example.capstone;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * A JavaFX application that displays a splash screen with a background image,
 * a logo, brand text, and animated transitions before launching the main application.
 * <p>
 * The splash screen includes fade-in, zoom-in, pause, and fade-out animations
 * to create a smooth introduction to the application.
 * </p>
 */
public class SplashScreen extends Application {

    /**
     * The main entry point for JavaFX applications.
     * Initializes and displays the splash screen with animated effects and transitions.
     *
     * @param splashStage the primary stage used for displaying the splash screen
     */
    @Override
    public void start(Stage splashStage) {
        // Load background image
        Image backgroundImage = new Image(getClass().getResource("/Images/background.jpg").toExternalForm());
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(500);
        backgroundView.setFitHeight(400);
        backgroundView.setPreserveRatio(false);

        // Load logo
        Image logoImage = new Image(getClass().getResource("/Images/Logo.png").toExternalForm());
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(300);
        logoView.setPreserveRatio(true);

        // Glow effect for logo
        Glow logoGlow = new Glow(0.8);
        logoView.setEffect(logoGlow);

        // Create splash layout
        StackPane splashLayout = new StackPane();
        splashLayout.getChildren().addAll(backgroundView, logoView);

        // Add brand text below the logo
        Label brandText = new Label("Your Brand Name");
        brandText.setFont(new Font("Arial", 24));
        brandText.setTextFill(Color.WHITE);

        // Glow effect for text
        Glow textGlow = new Glow(0.7);
        brandText.setEffect(textGlow);

        splashLayout.getChildren().add(brandText);
        StackPane.setAlignment(brandText, javafx.geometry.Pos.BOTTOM_CENTER);
        brandText.setTranslateY(30);

        // Create and show splash scene
        Scene splashScene = new Scene(splashLayout, 500, 400);
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        // Fade In animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), logoView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Zoom In animation
        ScaleTransition zoomIn = new ScaleTransition(Duration.seconds(1.5), logoView);
        zoomIn.setFromX(0.5);
        zoomIn.setFromY(0.5);
        zoomIn.setToX(1);
        zoomIn.setToY(1);

        // Pause
        PauseTransition stayOnScreen = new PauseTransition(Duration.seconds(1.0));

        // Fade Out animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), logoView);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // On finish, launch main app
        fadeOut.setOnFinished(event -> {
            splashStage.close();
            openMainApp();
        });

        // Animation sequence
        fadeIn.setOnFinished(e -> zoomIn.play());
        zoomIn.setOnFinished(e -> stayOnScreen.play());
        stayOnScreen.setOnFinished(e -> fadeOut.play());

        fadeIn.play(); // Start the animation sequence
    }

    /**
     * Launches the main application after the splash screen finishes.
     * This method creates a new instance of the main application class and starts it.
     */
    private void openMainApp() {
        CapstoneApplication mainApp = new CapstoneApplication();
        Stage mainStage = new Stage();
        try {
            mainApp.start(mainStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch();
    }
}
