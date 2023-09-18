package nz.ac.auckland.se206.controllers;

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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;

public class BathroomController {

  @FXML private Rectangle toLockedRoom;
  @FXML private Rectangle quizMaster;
  @FXML private Canvas gameMaster;
  @FXML private Label Timer;
  private Image[] alienImages;
  private int currentImageIndex = 0;
  @FXML private ImageView lightOne;
  @FXML private ImageView lightTwo;
  @FXML private ImageView lightThree;
  @FXML private Ellipse ellipseOne;
  @FXML private Ellipse ellipseTwo;
  @FXML private Ellipse ellipseThree;
  @FXML private Circle puzzle;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    if (!GameState.isLightPuzzleSolved) {
      ellipseOne.setOpacity(0.45);
      ellipseTwo.setOpacity(0.45);
      ellipseThree.setOpacity(0.45);
    }
    if (!GameState.isDecryptCompleted) {
      ellipseOne.setOnMouseClicked(null);
      ellipseOne.setOnMouseEntered(null);
      ellipseTwo.setOnMouseClicked(null);
      ellipseTwo.setOnMouseEntered(null);
      ellipseThree.setOnMouseClicked(null);
      ellipseThree.setOnMouseEntered(null);
    } else if (GameState.randomLight.equals("first")) {
      ellipseTwo.setOnMouseClicked(null);
      ellipseTwo.setOnMouseEntered(null);
      ellipseThree.setOnMouseClicked(null);
      ellipseThree.setOnMouseEntered(null);
    } else if (GameState.randomLight.equals("second")) {
      ellipseOne.setOnMouseClicked(null);
      ellipseOne.setOnMouseEntered(null);
      ellipseThree.setOnMouseClicked(null);
      ellipseThree.setOnMouseEntered(null);
    } else {
      ellipseTwo.setOnMouseClicked(null);
      ellipseTwo.setOnMouseEntered(null);
      ellipseOne.setOnMouseClicked(null);
      ellipseOne.setOnMouseEntered(null);
    }
    Timer.setText(GameState.getTimeLeft());
    Thread timeThread =
        new Thread(
            () -> {
              startTimer();
            });
    timeThread.start();
    // game master animation
    // Initialize alienImages with your image paths
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
  public void clickQuizMaster(MouseEvent event) {
    if (!GameState.isRiddleResolved) {
      App.setUi("chat");
    }
  }

  @FXML
  public void enterLockedRoom(MouseEvent event) {
    GameState.currentRoom = "lockedroom";
    App.setUi("lockedroom");
  }

  @FXML
  public void highlight() {
    toLockedRoom.setOpacity(0.7);
  }

  @FXML
  public void removeHighlight() {
    toLockedRoom.setOpacity(0.4);
  }

  @FXML
  public void clickLightOne() {
    GameState.isLightPuzzleStarted = true;
    GameState.currentRoom = "light";
    App.setUi("light");
  }

  @FXML
  public void clickLightTwo() {
    GameState.isLightPuzzleStarted = true;
    GameState.currentRoom = "light";
    App.setUi("light");
  }

  @FXML
  public void clickLightThree() {
    GameState.isLightPuzzleStarted = true;
    GameState.currentRoom = "light";
    App.setUi("light");
  }

  @FXML
  public void openPuzzle(MouseEvent event) {
    GameState.currentRoom = "puz";
    App.setUi("puz");
  }

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
                            Timer.setText(GameState.getTimeLeft());
                          }
                        });
                  }
                }));

    timeline.setCycleCount((GameState.minutes * 60) + GameState.seconds - 1);
    timeline.play();
  }

  @FXML
  public void increaseSizeOne(MouseEvent event) {
    ellipseOne.setScaleX(1.2);
    ellipseOne.setScaleY(1.2);
    lightOne.setScaleX(1.2);
    lightOne.setScaleY(1.2);
  }

  @FXML
  public void increaseSizeTwo(MouseEvent event) {
    ellipseTwo.setScaleX(1.2);
    ellipseTwo.setScaleY(1.2);
    lightTwo.setScaleX(1.2);
    lightTwo.setScaleY(1.2);
  }

  @FXML
  public void increaseSizeThree(MouseEvent event) {
    ellipseThree.setScaleX(1.2);
    ellipseThree.setScaleY(1.2);
    lightThree.setScaleX(1.2);
    lightThree.setScaleY(1.2);
  }

  @FXML
  public void decreaseSizeOne(MouseEvent event) {
    ellipseOne.setScaleX(1);
    ellipseOne.setScaleY(1);
    lightOne.setScaleX(1);
    lightOne.setScaleY(1);
  }

  @FXML
  public void decreaseSizeTwo(MouseEvent event) {
    ellipseTwo.setScaleX(1);
    ellipseTwo.setScaleY(1);
    lightTwo.setScaleX(1);
    lightTwo.setScaleY(1);
  }

  @FXML
  public void decreaseSizeThree(MouseEvent event) {
    ellipseThree.setScaleX(1);
    ellipseThree.setScaleY(1);
    lightThree.setScaleX(1);
    lightThree.setScaleY(1);
  }
}
