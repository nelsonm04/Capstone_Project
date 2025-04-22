package com.example.capstone;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen extends Application {

    @Override
    public void start(Stage splashStage) {
        // Load your logo
        Image logoImage = new Image(getClass().getResource("/Images/Logo.png").toExternalForm());
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(300);
        logoView.setPreserveRatio(true);

        StackPane splashLayout = new StackPane(logoView);
        Scene splashScene = new Scene(splashLayout, 500, 400);

        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setScene(splashScene);
        splashStage.show();

        // 1. Fade In
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), logoView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // 2. Pause (keep fully visible for a moment)
        PauseTransition stayOnScreen = new PauseTransition(Duration.seconds(1.0));

        // 3. Fade Out
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1.5), logoView);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // 4. After fade out, open main app
        fadeOut.setOnFinished(event -> {
            splashStage.close();
            openMainApp();
        });

        // Sequence: Fade In -> Pause -> Fade Out
        fadeIn.setOnFinished(e -> stayOnScreen.play());
        stayOnScreen.setOnFinished(e -> fadeOut.play());

        fadeIn.play();  // start the sequence
    }

    private void openMainApp() {
        CapstoneApplication mainApp = new CapstoneApplication();
        Stage mainStage = new Stage();
        try {
            mainApp.start(mainStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}