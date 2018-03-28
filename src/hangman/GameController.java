//Peter Jones
//Arthur Wirsching
//Cheryl Huber
//Ivan Suarez

//Note: Professor, please read the readme.txt included before reading the source code. Thank you!

package hangman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;


public class GameController  {

	private final ExecutorService executorService;
	private final Game game;
	private final int NUMLTRS = 26;
	private ImageView lastImage;
	private ArrayList<Character> guessedLetters;
	
	public GameController(Game game) {
		this.game = game;
		guessedLetters = new ArrayList<Character>();
		executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				return thread;
			}
		});

	}

	@FXML
	private VBox board ;
	@FXML
	private HBox ltrBoard = new HBox(15);
	@FXML
	private HBox ltrBoard2 = new HBox(15);
	@FXML
	private Label statusLabel ;
	@FXML
	private Label enterALetterLabel ;
	@FXML
	private TextField textField ;
	@FXML
	private Label lblLtrs[] = new Label[NUMLTRS];
	private char ltrs[] = new char [] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
										'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	@FXML
	private Label lblWord = new Label();

	public void initialize() throws IOException {
		System.out.println("in initialize");
		initLtrs();
		drawHangman();
		addTextBoxListener();
		setUpStatusLabelBindings();
	}
	private void initLtrs(){ //populates the letters at the top of the frame
		int i;
		for(i=0;i<NUMLTRS;i++){
			lblLtrs[i]=new Label(Character.toString(ltrs[i]));
			lblLtrs[i].setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					if(game.getGameStatus() != Game.GameStatus.GAME_OVER && game.getGameStatus() != Game.GameStatus.WON) {
						Label l = new Label();
						l = (Label) e.getSource();
						String s = l.textFillProperty().get().toString();
						System.out.println(s);
						if (l.textFillProperty().get().toString().equals("0xff0000ff")) {
							displayDuplicateInputError();
						} else {
							l.setTextFill(Color.color(1.0, 0, 0));
							game.makeMove(l.getText());
							drawHangman();
						}
					}
				}
			});
			if(i<(NUMLTRS/2)) {
				ltrBoard.getChildren().add(lblLtrs[i]);
			}
			else{
				ltrBoard2.getChildren().add(lblLtrs[i]);
			}
		}
	}

	private void displayDuplicateInputError(){ //creates the pop up dialog for when the same letter is guessed again.
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Duplicate Letter Dialog");
		alert.setHeaderText("Pay Attention!");
		alert.setContentText("You have already tried that letter, please try again");
		alert.showAndWait();
	}


	private void addTextBoxListener() {
		textField.setEditable(false);

		//adds a listener that tests the user input to see if its valid
		textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				//if not in game over state and not won, else do nothing
				if(game.getGameStatus() != Game.GameStatus.GAME_OVER && game.getGameStatus() != Game.GameStatus.WON) {
					//if not a letter then return
					if (!event.getCode().isLetterKey())
						return;

					char c = event.getCode().toString().charAt(0);

					//make the characters upper case so its easier to work with
					if (c > 91)
						c = Character.toUpperCase(c);

					//test to see if the character has already been guessed
					for (char ch : guessedLetters) {
						if (ch == c) {
							//duplicate found, display error and return
							displayDuplicateInputError();
							return;
						}
					}

					//color on of the letters on the screen
					if (c >= 65 && c <= 90) {
						lblLtrs[c - 65].setTextFill(Color.color(1.0, 0, 0));
					}
					guessedLetters.add(c);
					game.makeMove(c + "");
					drawHangman();
				}
			}
		});
	}

	private void setUpStatusLabelBindings() {

		System.out.println("in setUpStatusLabelBindings");
		statusLabel.textProperty().bind(Bindings.format("%s", game.gameStatusProperty()));
		enterALetterLabel.textProperty().bind(Bindings.format("%s", "Enter or Click a letter"));
	}

	/**
	 * Formats the textbox to have underscores for missing letters, but displays letters that are correctly guessed
	 */
	private void updateTextBox(){
		String tmpWord = game.getTempAnswer().toString();
		String toTextBox = "";
		StringBuilder sb = new StringBuilder(tmpWord);

		//if a letter is blank in the temp answer make it an underscore. Goes for as long as the word length
		for (int i = 0; i<tmpWord.length();i++){
			if(sb.charAt(i) == ' ')
				sb.setCharAt(i, '_');
		}
		toTextBox = sb.toString();
		textField.setText(toTextBox);
		lblWord.setText(toTextBox);
	}

	/**
	 * retrieves an image of the hangman, loading in a different one based on the number of remaining tries
	 */
	private void drawHangman() {
		updateTextBox();
    	if(lastImage!=null)
			board.getChildren().remove(lastImage);
    	int tries = game.getTries();
    	System.out.println("Retrieving image from images/" + tries + ".png");
    	Image img = new Image("images/" + tries + ".png");
		ImageView imgView = new ImageView(img);
		lastImage = imgView;
		board.getChildren().add(imgView);
	}
		
	@FXML 
	private void newHangman() { //resets the game and calls the reset function in the game object as well
		System.out.println("IT RESET");
		guessedLetters = new ArrayList<Character>();
		ltrBoard.getChildren().clear();
		ltrBoard2.getChildren().clear();
		initLtrs();
		addTextBoxListener();
		setUpStatusLabelBindings();
		guessedLetters.clear();
		game.reset();
		drawHangman();
	}

	@FXML
	private void quit() {
		board.getScene().getWindow().hide();
	}

	@FXML
	private void ltrClicked(){

	}

}

