package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

// import javafx.scene.control.Alert;

/** Controller class for the chat view. */
public class ChatController {
  @FXML private TextArea chatTextArea;
  @FXML private TextField inputText;
  @FXML private Button sendButton;
  @FXML private Canvas quizMaster;
  @FXML private Label Timer;
  @FXML private Label labelTranslate;
  private Image[] alienImages;
  private int currentImageIndex = 0;
  @FXML private ImageView sdCard;
  @FXML private Label sdCollect;
  @FXML private ImageView sdCard1;
  @FXML private ImageView tape;
  @FXML private TextArea objText;
  @FXML private TextArea hintsText;
  @FXML private ImageView globe;
  @FXML private Button rgbClue1;

  /**
   * Initializes the chat view, loading the riddle.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  public void initialize() throws ApiProxyException {
    if (GameState.isMediumPicked) {
      hintsText.setText("Hints remaining: " + (5 - GameState.hintCounter));
    }
    objText.setText(GameState.getObjective());
    hintsText.setText(GameState.getHint());
    chatTextArea.setText(GameState.chatContents);
    if (GameState.isRgbClueFound) {
      rgbClue1.setVisible(true);
      rgbClue1.setText(GameState.password);
    } else {
      rgbClue1.setVisible(false);
    }

    if (!GameState.isGameMasterLoaded) {
      runGpt(new ChatMessage("user", GptPromptEngineering.getRiddleWithGivenWord("spaceship")));
      GameState.isGameMasterLoaded = true;
    }
    // when the enter key is pressed
    inputText.setOnAction(
        e -> {
          try {
            onSendMessage(e);
          } catch (ApiProxyException | IOException ex) {
            ex.printStackTrace();
            // Handle other exceptions appropriately.
          }
        });
    if (!GameState.isSdCardFound) {
      sdCard.setVisible(GameState.isRiddleResolved);
    } else {
      sdCard.setVisible(false);
    }
    sdCard1.setVisible(GameState.isSdCardFound);
    tape.setVisible(GameState.isElectricalTapeFound);
    globe.setVisible(GameState.isGlobeFound);
    Timer.setText(GameState.getTimeLeft());
    Thread timeThread =
        new Thread(
            () -> {
              startTimer();
            });
    timeThread.start();

    // game master animation
    // Initialize alienImages with your image paths
    alienImages = new Image[] {new Image("images/blink1.png"), new Image("images/blink2.png")};

    TranslateTransition translateTransition =
        new TranslateTransition(Duration.seconds(2), quizMaster);

    // set the Y-axis translation value
    translateTransition.setByY(-10);

    // set the number of cycles for the animation
    translateTransition.setCycleCount(TranslateTransition.INDEFINITE);

    // Set auto-reverse to true to make the label return to its original position
    translateTransition.setAutoReverse(true);

    // Start the animation
    translateTransition.play();

    // Start the animation
    startAnimation();
  }

  private void startAnimation() {
    GraphicsContext gc = quizMaster.getGraphicsContext2D();
    AnimationTimer timer =
        new AnimationTimer() {
          private long lastTime = 0;
          private final long frameDurationMillis = 1000; // 1000 milliseconds = 1 second

          @Override
          public void handle(long currentTime) {
            if (currentTime - lastTime >= frameDurationMillis * 1_000_000) {
              if (currentImageIndex < alienImages.length) {
                gc.clearRect(0, 0, quizMaster.getWidth(), quizMaster.getHeight());
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

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    chatTextArea.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private CompletableFuture<ChatMessage> runGpt(ChatMessage msg) throws ApiProxyException {
    labelTranslate.setOpacity(0.55);
    GameState.chatCompletionRequest.addMessage(msg);
    CompletableFuture<ChatMessage> completableFuture = new CompletableFuture<>();

    Task<Void> task =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              ChatCompletionResult chatCompletionResult = GameState.chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              GameState.chatCompletionRequest.addMessage(result.getChatMessage());

              Platform.runLater(
                  () -> {
                    appendChatMessage(result.getChatMessage());
                    labelTranslate.setOpacity(0);
                    completableFuture.complete(result.getChatMessage()); // Complete the future
                  });
            } catch (ApiProxyException e) {
              // TODO handle exception appropriately
              e.printStackTrace();
              completableFuture.completeExceptionally(e); // Complete the future exceptionally
            }
            return null;
          }
        };

    Thread thread = new Thread(task);
    thread.start();

    return completableFuture;
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage(ActionEvent event) throws ApiProxyException, IOException {

    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      return;
    }

    if (GameState.hintCounter == 5) {
                Thread thread =
                    new Thread(
                        () -> {
                          GameState.sendPrompt("The player has reached their hint limit. Do not provide any help to the player. Do not give the player any hints. Do not tell the player the next step.");
                        });
                thread.start();
    }

    if(GameState.isDifficultPicked){
      Thread thread =
                    new Thread(
                        () -> {
                          GameState.sendPrompt("The player is in difficult mode. Do not provide any help to the player. Do not give the player any hints. Do not tell the player the next step.");
                        });
                thread.start();
    }

    inputText.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);

    /*
    if (GameState.isMediumPicked && GameState.hintCounter >= 5) {
      Pattern helpPattern = Pattern.compile("(help|hint)", Pattern.CASE_INSENSITIVE);
      Matcher helpMatcher = helpPattern.matcher(message);

      if (helpMatcher.find() && GameState.hintCounter >= 5) {
        appendChatMessage(new ChatMessage("assistant", "All hints have been used up."));
        return;
      }
    }

    if (!GameState.isDifficultPicked) {
      Pattern pattern =
          Pattern.compile(
              "(what'?s? next|how do I continue|what should I do now |next |do |then)",
              Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(message);
      if (matcher.find()) {
        if (GameState.isMediumPicked && GameState.hintCounter >= 5) {
          appendChatMessage(
              new ChatMessage("assistant", "You've reached the maximum number of hints allowed."));
          return;
        }

        String nextStepMessage = GptPromptEngineering.getNextGameFlowStep();
        appendChatMessage(new ChatMessage("assistant", nextStepMessage));

        if (GameState.isMediumPicked) {
          GameState.hintCounter++;
          hintsText.setText("Hints Remaining: " + (5 - GameState.hintCounter));
        }
        return;
      }
    }
    */

    CompletableFuture<ChatMessage> future = runGpt(msg);
    future.thenAccept(
        lastMsg -> {
          if (lastMsg.getRole().equals("assistant")) {
            if (lastMsg.getContent().startsWith("Correct")) {
              GameState.isRiddleResolved = true;
              sdCard.setVisible(true);
              sdCollect.setText("Collect the SD card!");
              objText.setText(GameState.getObjective());
              GameState.currentObj = "Decrypt";
              Thread thread =
                    new Thread(
                        () -> {
                          GameState.sendPrompt("The player has solved the riddle and received an SD card. The player must"
                          + " now find and access the computer in the computer room by clicking on the computer.");
                        });
                thread.start();
            } else if (lastMsg.getContent().contains("hint: ")
                || lastMsg.getContent().contains("Hint: ")|| lastMsg.getContent().contains("Clue: ")|| lastMsg.getContent().contains("clue: ")) {
                  if(GameState.hintCounter<5){
              GameState.hintCounter++;
                  }
                  GameState.latestHint = lastMsg.getContent();
              // Display the hint count if its medium level
              if (GameState.isMediumPicked && GameState.hintCounter <= 5) {
                hintsText.setText("Hints Remaining: " + (5 - GameState.hintCounter) +"\n"+GameState.latestHint);
              }
            }
          }
        });
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    GameState.chatContents = chatTextArea.getText();
    App.setUi(GameState.currentRoom);
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
  public void increaseSize() {
    sdCard.setScaleX(1.2);
    sdCard.setScaleY(1.2);
  }

  @FXML
  public void decreaseSize() {
    sdCard.setScaleX(1);
    sdCard.setScaleY(1);
  }

  @FXML
  public void clickSdCard() {
    GameState.isSdCardFound = true;
    sdCard.setVisible(false);
    sdCollect.setText("");
    sdCard1.setVisible(true);
  }
}
