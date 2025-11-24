package org.example.lab3;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;


import java.io.IOException;

public class LoginController {
    @FXML
    private Label errorText;
    @FXML
    private TextField username;
    @FXML
    private TextField password;

    private int maxAttempts;     // n – max attempts to login
    private int blockTimeSec;    // t – block time in sec

    private final Map<String, Integer> attemptsPerUser = new HashMap<>();    // Stores the number of failed login attempts per valid user (identified by email).
    private final Map<String, Long> blockStartPerUser = new HashMap<>();    // Stores the timestamp (in milliseconds) when a specific user was blocked
    private final Map<String, Object> userLocks = new HashMap<>();          // A per-user lock map used to provide fine-grained synchronization.

    public void setLimits(int maxAttempts, int blockTimeSec) {
        this.maxAttempts = maxAttempts;
        this.blockTimeSec = blockTimeSec;
    }
// Return lock object for associated with the given email.
    private Object getUserLock(String email) {
        synchronized (userLocks) {
            return userLocks.computeIfAbsent(email, k -> new Object());     // If no lock exists yet, a new one is created.
        }
    }
// Increments the failed login attempt count for a specific user.
    private int incrementAttempts(String email) {
        Object lockForUser = getUserLock(email);
        synchronized (lockForUser) {
            int newCount = attemptsPerUser.getOrDefault(email, 0) + 1;
            attemptsPerUser.put(email, newCount);
            return newCount;
        }
    }

    // Marks the current time as the beginning of the user's block period.
    private void markBlockedNow(String email) {
        Object lockForUser = getUserLock(email);
        synchronized (lockForUser) {
            blockStartPerUser.put(email, System.currentTimeMillis());
        }
    }

    // Checks whether the user is still blocked based on the stored block timestamp.
    private boolean isStillBlocked(String email) {
        Object lockForUser = getUserLock(email);
        synchronized (lockForUser) {
            Long start = blockStartPerUser.get(email);
            if (start == null) {
                return false;
            }
            long now = System.currentTimeMillis();
            long blockEnd = start + blockTimeSec * 1000L;
            if (now < blockEnd) {
                return true; // Still blocked
            } else {        // If the block period has already expired, the user's state is reset.
                attemptsPerUser.put(email, 0);
                blockStartPerUser.remove(email);
                return false;
            }
        }
    }

    // Resets the user's failed attempt counter and removes any block timestamp.
    // Used after a successful login or after the block period ends.
    private void resetUser(String email) {
        Object lockForUser = getUserLock(email);
        synchronized (lockForUser) {
            attemptsPerUser.put(email, 0);
            blockStartPerUser.remove(email);
        }
    }


    @FXML
    protected void onLoginButtonClick() {
        String email = username.getText();
        String psw = password.getText();
            if (!Launcher.isKnownUser(email)) {
                try {
                    new User(email, psw);        // Creates a "temporary" user to check that the email and password are valid.
                } catch (IllegalArgumentException e) {
                    errorText.setText(e.getMessage());       // Getting the error message from the User class
                    return;
                }
            errorText.setText("The user does not exist in the system.");
            return;
        }

        // Step 1: Check if the user is currently blocked (before even checking the password)
        if (isStillBlocked(email)) {
            errorText.setText("User is blocked. Please wait for the blocking to end.");
            return;
        }
        // Step 2: Check if username + password are correct
        boolean ok = Launcher.checkUser(email, psw);
        if (!ok) {
            // *****Thread 1: Update the number of failed attempts + start blocking if necessary *****
            Thread updateAttemptsThread = new Thread(() -> {
                int newCount = incrementAttempts(email);
                if (newCount < maxAttempts) {   // If we haven't reached maxAttempts yet, displays an error and number of attempts
                    Platform.runLater(() -> errorText.setText(
                            "Incorrect password. Attempt " + newCount + " of " + maxAttempts + "."
                    ));
                } else {            // We have reached the maximum number -> Block
                    markBlockedNow(email); // Find and save blocking time
                    Platform.runLater(() -> errorText.setText(
                            "User blocked for " + blockTimeSec + " seconds"
                    ));
                    // Blocking thread - waits t seconds and then allows new attempts
                    Thread blockThread = new Thread(() -> {
                        try {
                            Thread.sleep(blockTimeSec * 1000L);
                        } catch (InterruptedException ignored) {}

                        resetUser(email);   // After block time ends – reset the user
                        Platform.runLater(() -> errorText.setText(
                                "The block is over, you can try again."
                        ));
                    });
                    blockThread.setDaemon(true);
                    blockThread.start();
                }
            });
            updateAttemptsThread.setDaemon(true);
            updateAttemptsThread.start();

        } else {
            //  Thread 2: Username + password are correct -> check if not blocked
            Thread successThread = new Thread(() -> {
                boolean blocked = isStillBlocked(email);
                if (blocked) {
                    Platform.runLater(() -> errorText.setText(
                            "The user is still blocked, unable to log in."
                    ));
                } else {
                    // The user is not blocked -> reset to be safe and open Welcome
                    resetUser(email);
                    Platform.runLater(this::openWelcomeWindow);
                }
            });
            successThread.setDaemon(true);
            successThread.start();
        }
    }

    // Separate method for opening the Welcome window
    private void openWelcomeWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("welcome.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 220, 150);
            Stage newStage = new Stage();
            newStage.setTitle("Welcome");
            newStage.setScene(scene);
            newStage.show();

            newStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

        } catch (IOException e) {
            e.printStackTrace();
            errorText.setText("Error opening Welcome window");
        }
    }
}
