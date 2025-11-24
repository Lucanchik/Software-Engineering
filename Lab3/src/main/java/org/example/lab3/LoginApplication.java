package org.example.lab3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

import java.io.IOException;

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        List<String> params = getParameters().getRaw();
        int maxAttempts = Integer.parseInt(params.get(0));   // n
        int blockTimeSec = Integer.parseInt(params.get(1));  // t

        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 280);

        LoginController controller = fxmlLoader.getController();
        controller.setLimits(maxAttempts, blockTimeSec);

        stage.setTitle("Login");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> System.exit(0));

        stage.show();
    }

}