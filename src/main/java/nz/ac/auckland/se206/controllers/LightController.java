package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class LightController {
  // Intialising all the variables for the scene

  @FXML private Circle behindLight;
  @FXML private Label timer;
  @FXML private Canvas gameMaster;
  @FXML private Rectangle quizMaster;
  @FXML private ImageView fixOne;
  @FXML private ImageView fixTwoOne;
  @FXML private ImageView fixTwoTwo;
  @FXML private Rectangle fixTwoThree;
  @FXML private ImageView fixThreeOne;
  @FXML private ImageView fixThreeTwo;
  @FXML private ImageView fixFour;
  @FXML private Label lightSuggest;
  private Image[] alienImages;
  private int currentImageIndex = 0;
  @FXML private TextArea objText;
  @FXML private TextArea hintsText;
  @FXML private ImageView tape;
  @FXML private ImageView sdCard;
  @FXML private ImageView globe;
  @FXML private ImageView globe1;
  @FXML private Button rgbClue1;

  public void initialize() {
    // Intialisng the items collected by the user/ will collect from the scene
    if (GameState.isRgbClueFound) {
      rgbClue1.setVisible(true);
      rgbClue1.setText(GameState.password);
    } else {
      rgbClue1.setVisible(false);
    }
    // Intialisng the objectives and hints section of the scene
    objText.setText(GameState.getObjective());
    hintsText.setText(GameState.getHint());
    tape.setVisible(GameState.isElectricalTapeFound);
    sdCard.setVisible(GameState.isSdCardFound);
    // Intialisng the items collected by the user/ will collect from the scene
    if (!GameState.isGlobeFound && GameState.isLightPuzzleSolved) {
      globe.setVisible(GameState.isRiddleResolved);
    } else {
      globe.setVisible(false);
    }
    globe1.setVisible(GameState.isGlobeFound);
    if (GameState.isLightPuzzleSolved) {
      objText.setText(GameState.getObjective());
    } else {
      objText.setText("Fix the wires to turn on the light.");
    }
    // Checking if the light wires are fixed
    if (GameState.isWireOneFixed) {
      fixOne.setOpacity(1);
      fixOne.setOnMouseClicked(null);
    }
    // Checking if the light wires are fixed
    if (GameState.isWireTwoFixed) {
      fixTwoOne.setOpacity(1);
      fixTwoTwo.setOpacity(1);
      fixTwoThree.setOpacity(1);
      fixTwoOne.setOnMouseClicked(null);
      fixTwoTwo.setOnMouseClicked(null);
      fixTwoThree.setOnMouseClicked(null);
    }
    // Checking if the light wires are fixed
    if (GameState.isWireThreeFixed) {
      fixThreeOne.setOpacity(1);
      fixThreeTwo.setOpacity(1);
      fixThreeOne.setOnMouseClicked(null);
      fixThreeTwo.setOnMouseClicked(null);
    }
    // Checking if the light wires are fixed
    if (GameState.isWireFourFixed) {
      fixFour.setOpacity(1);
      fixFour.setOnMouseClicked(null);
    }
    // If all of them are fixed display the mesage
    if (GameState.wireFixes == 4) {
      lightSuggest.setText("All the wires have been fixed.");
    }
    lightSuggest.setWrapText(true);
    timer.setText(GameState.getTimeLeft());
    // timer thread
    Thread timeThread =
        new Thread(
            () -> {
              startTimer();
            });
    timeThread.start();
    alienImages =
        new Image[] {
          new Image("images/move1.png"),
          new Image("images/move2.png"),
          new Image("images/move3.png"),
          new Image("images/move4.png")
        };

    // Start the animation
    startAnimation();

    TranslateTransition translateTransition =
        new TranslateTransition(Duration.seconds(2), gameMaster);

    // set the Y-axis translation value
    translateTransition.setByY(-10);

    // set the number of cycles for the animation
    translateTransition.setCycleCount(TranslateTransition.INDEFINITE);

    // Set auto-reverse to true to make the label return to its original position
    translateTransition.setAutoReverse(true);

    // Start the animation
    translateTransition.play();
  }

  // Starting animation for the gamemaster
  private void startAnimation() {
    GraphicsContext gc = gameMaster.getGraphicsContext2D();
    AnimationTimer timer =
        new AnimationTimer() {
          private long lastTime = 0;
          private final long frameDurationMillis = 100; // 1000 milliseconds = 1 second

          @Override
          public void handle(long currentTime) {
            if (currentTime - lastTime >= frameDurationMillis * 1_000_000) {
              if (currentImageIndex < alienImages.length) {
                gc.clearRect(0, 0, gameMaster.getWidth(), gameMaster.getHeight());
                gc.drawImage(alienImages[currentImageIndex], 0, 0);
                currentImageIndex++;
                // Check if we have displayed all images; if so, reset the index to 0
                if (currentImageIndex >= alienImages.length) {
                  currentImageIndex = 0;
                }
                lastTime = currentTime;
              }
            }
          }
        };
    timer.start();
  }

  // pressing on the quiz master to open the chat box
  @FXML
  private void clickQuizMaster(MouseEvent event) {
    App.setUi("chat");
  }

  // Starting timer for the timer in the scene
  public void startTimer() {
    Timeline timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                new EventHandler<ActionEvent>() {
                  @Override
                  public void handle(ActionEvent event) {
                    // Counts down the timer.
                    Platform.runLater(
                        new Runnable() {
                          @Override
                          public void run() {
                            timer.setText(GameState.getTimeLeft());
                          }
                        });
                  }
                }));

    timeline.setCycleCount((GameState.minutes * 60) + GameState.seconds - 1);
    timeline.play();
  }

  // Go back method for th ego back button once the user leaves the room
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    if (GameState.isLightPuzzleSolved) {
      GameState.currentObj = "Picture Puz";
    }
    GameState.isLightPuzzleStarted = true;
    GameState.currentRoom = "bathroom";
    App.setUi("bathroom");
  }

  // method for fixing the break
  @FXML
  private void clickBreakOne() {
    // Checking if the electrical tape is found
    if (GameState.isElectricalTapeFound) {
      // Patching the wire
      objText.setText("Patch the wires with the electrical tape.");
      fixOne.setOpacity(1);
      GameState.isWireOneFixed = true;
      fixOne.setOnMouseClicked(null);
      GameState.wireFixes++;
      if (GameState.wireFixes == 4) {
        lightSuggest.setText("Good Job! Collect your next clue.");
        objText.setText(
            "Good Job! You have solved the light puzzle. Collect the Picture of the Globe and"
                + " travel to your next puzzle.");
        // Sending prompt to gpt to update game flow and progress
        Thread thread =
            new Thread(
                () -> {
                  GameState.sendPrompt(
                      "The player has fixed the broken light. The player has received a picture of"
                          + " a globe that can be found in the room with the locked door. The"
                          + " player must now go and click on the globe to access the next part of"
                          + " the puzzle.");
                });
        thread.start();
        GameState.isLightPuzzleSolved = true;
        globe.setVisible(true);
      }
    } else {
      objText.setText(
          "You need to find the Electrical Tape needed to patch the wires. Check every room"
              + " carefully!");
    }
  }

  @FXML
  private void clickBreakTwo() {
    // Checking if the electrical tape is found
    if (GameState.isElectricalTapeFound) {
      // Patching the wire
      objText.setText("Patch the wires with the electrical tape.");
      fixTwoOne.setOpacity(1);
      fixTwoTwo.setOpacity(1);
      fixTwoThree.setOpacity(1);
      GameState.isWireTwoFixed = true;
      fixTwoOne.setOnMouseClicked(null);
      fixTwoTwo.setOnMouseClicked(null);
      fixTwoThree.setOnMouseClicked(null);
      GameState.wireFixes++;
      if (GameState.wireFixes == 4) {
        lightSuggest.setText("Good Job! Collect your next clue.");
        objText.setText(
            "Good Job! You have solved the light puzzle. Collect the Picture of the Globe and"
                + " travel to your next puzzle.");
        // Sending prompt to gpt to update game flow and progress
        Thread thread =
            new Thread(
                () -> {
                  GameState.sendPrompt(
                      "The player has fixed the broken light. The player has received a picture of"
                          + " a globe that can be found in the room with the locked door. The"
                          + " player must now go and click on the globe to access the next part of"
                          + " the puzzle.");
                });
        thread.start();
        GameState.isLightPuzzleSolved = true;
        globe.setVisible(true);
      }
    } else {
      objText.setText(
          "You need to find the Electrical Tape needed to patch the wires. Check every room"
              + " carefully!");
    }
  }

  @FXML
  private void clickBreakThree() {
    // Checking if the electrical tape is found
    if (GameState.isElectricalTapeFound) {
      // Patching the wire
      objText.setText("Patch the wires with the electrical tape.");
      fixThreeOne.setOpacity(1);
      fixThreeTwo.setOpacity(1);
      GameState.isWireThreeFixed = true;
      fixThreeOne.setOnMouseClicked(null);
      fixThreeTwo.setOnMouseClicked(null);
      GameState.wireFixes++;
      if (GameState.wireFixes == 4) {
        lightSuggest.setText("Good Job! Collect your next clue.");
        objText.setText(
            "Good Job! You have solved the light puzzle. Collect the Picture of the Globe and"
                + " travel to your next puzzle.");
        // Sending prompt to gpt to update game flow and progress
        Thread thread =
            new Thread(
                () -> {
                  GameState.sendPrompt(
                      "The player has fixed the broken light. The player has received a picture of"
                          + " a globe that can be found in the room with the locked door. The"
                          + " player must now go and click on the globe to access the next part of"
                          + " the puzzle.");
                });
        thread.start();
        GameState.isLightPuzzleSolved = true;
        globe.setVisible(true);
      }
    } else {
      objText.setText(
          "You need to find the Electrical Tape needed to patch the wires. Check every room"
              + " carefully!");
    }
  }

  @FXML
  private void clickBreakFour() {
    // Checking if the electrical tape is found
    if (GameState.isElectricalTapeFound) {
      // Patching the wire
      objText.setText("Patch the wires with the electrical tape.");
      fixFour.setOpacity(1);
      GameState.isWireFourFixed = true;
      fixFour.setOnMouseClicked(null);
      GameState.wireFixes++;
      if (GameState.wireFixes == 4) {
        lightSuggest.setText("Good Job! Collect your next clue.");
        objText.setText(
            "Good Job! You have solved the light puzzle. Collect the Picture of the Globe and"
                + " travel to your next puzzle.");
        // Sending prompt to gpt to update game flow and progress
        Thread thread =
            new Thread(
                () -> {
                  GameState.sendPrompt(
                      "The player has fixed the broken light. The player has received a picture of"
                          + " a globe that can be found in the room with the locked door. The"
                          + " player must now go and click on the globe to access the next part of"
                          + " the puzzle.");
                });
        thread.start();
        GameState.isLightPuzzleSolved = true;
        globe.setVisible(true);
      }
    } else {
      objText.setText(
          "You need to find the Electrical Tape needed to patch the wires. Check every room"
              + " carefully!");
    }
  }

  // Increasing size of the globe
  @FXML
  private void increaseGlobeSize() {
    globe.setScaleX(1.2);
    globe.setScaleY(1.2);
  }

  // Decreasing size of the globe
  @FXML
  private void decreaseGlobeSize() {
    globe.setScaleX(1);
    globe.setScaleY(1);
  }

  // Method for clicking the globe
  @FXML
  private void clickGlobe() {
    GameState.isGlobeFound = true;
    GameState.currentObj = "Picture Puz";
    lightSuggest.setText("");
    globe.setVisible(false);
    globe1.setVisible(true);
  }
}
