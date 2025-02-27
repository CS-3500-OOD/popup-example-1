package cs3500.lab10.controller;

import cs3500.lab10.model.BoardCell;
import cs3500.lab10.model.Coord;
import cs3500.lab10.model.Mole;
import cs3500.lab10.model.WamBoard;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Represents the controller for a game of Whack-a-Mole.
 */
public class WamControllerImpl implements WamController {
  private final WamBoard board;
  private final Mole mole;
  private int moleWhacks;


  @FXML
  private GridPane buttonGrid;
  private final StringProperty[][] buttons;
  private final Random rand = new Random();
  private static final int BUTTON_SIZE = 100;

  @FXML
  private Button popupButton;

  private Stage stage;
  private Popup popup;

  /**
   * Instantiates a game of Whack-a-Mole.
   *
   * @param board the Whack-a-Mole board
   */
  public WamControllerImpl(WamBoard board, Stage stage) {
    this.board = Objects.requireNonNull(board);
    this.mole = new Mole(this.board.getRandomCell());
    this.moleWhacks = 0;
    this.buttons = new StringProperty[this.board.getRowCount()][this.board.getColCount()];
    this.stage = stage;
  }

  /**
   * Initializes a game of Whack-a-Mole.
   *
   * @throws IllegalStateException if the game board is not defined
   */
  @FXML
  public void run() throws IllegalStateException, IOException {
    popupButton.setOnAction(e -> makePopup());
    this.initButtons();
    this.initTimeline();
    this.popup = new Popup();
    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("popup.fxml"));
    loader.setController(this);
    Scene s = loader.load();
    popup.getContent().add((Node)s.getRoot());
    Button b = new Button("Done!1");
    b.setOnAction(e -> popup.hide());
    popup.getContent().add(b);
  }

  @FXML
  private void makePopup() {
    this.popup.show(this.stage);
  }

  /**
   * Handles a user whacking a cell on the game board.
   *
   * @param coord the coordinates of the cell whacked
   */
  private void handleWhack(Coord coord) {
    if (mole.getLocation().equals(coord)) {
      this.moleWhacks++;

      // hide the mole
      this.mole.setVisible(false);
      Coord moleLocation = mole.getLocation();
      buttons[moleLocation.getRow()][moleLocation.getCol()].set("");

      System.out.println("Whacked " + this.moleWhacks + " moles!");
    }
  }

  /**
   * Updates the game's view and model to handle moving the mole.
   */
  private void nextTurn() {
    if (mole.isVisible()) {
      // hide mole from previous location
      Coord origin = mole.getLastKnownLocation();
      buttons[origin.getRow()][origin.getCol()].set("");

      // show mole at new location
      Coord dest = mole.getLocation();
      buttons[dest.getRow()][dest.getCol()].set(">MOLE<");

      // hide mole for next turn
      mole.setVisible(false);
    } else {
      // determine destination
      int destRow = rand.nextInt(this.board.getRowCount());
      int destCol = rand.nextInt(this.board.getColCount());
      BoardCell destCell = this.board.getCellAt(destRow, destCol);

      // update mole
      mole.move(destCell);
      mole.setVisible(true);
    }
  }

  /**
   * Initializes the timeline for each mole movement.
   */
  private void initTimeline() {
    KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.75), e -> this.nextTurn());
    Timeline timeline = new Timeline(keyFrame);
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.playFromStart();
  }

  /**
   * Dynamically generates grid and buttons. Adds an event handler to
   * each button for supporting whacks.
   */
  private void initButtons() {
    // add constraints to each column/row to stretch + fill available space
    for (int row = 0; row < this.board.getRowCount(); row++) {
      RowConstraints rowConstraints = new RowConstraints();
      rowConstraints.setVgrow(Priority.ALWAYS);
      buttonGrid.getRowConstraints().add(rowConstraints);

      // iterate through each column
      for (int col = 0; col < this.board.getColCount(); col++) {
        // only initialize columns for first row
        if (row == 0) {
          ColumnConstraints colConstraints = new ColumnConstraints();
          colConstraints.setHgrow(Priority.ALWAYS);
          buttonGrid.getColumnConstraints().add(colConstraints);
        }

        // create coordinate
        Coord coord = new Coord(row, col);

        // create button
        Button button = new Button();
        button.setPrefWidth(BUTTON_SIZE);
        button.setPrefHeight(BUTTON_SIZE);
        button.setOnAction(e -> handleWhack(coord));

        // bind text
        buttons[row][col] = new SimpleStringProperty("");
        button.textProperty().bind(buttons[row][col]);

        // add button to grid
        buttonGrid.add(button, col, row);
      }
    }
  }
}