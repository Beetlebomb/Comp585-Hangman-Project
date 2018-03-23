package hangman;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class GameController {

	private final ExecutorService executorService;
	private final Game game;
	private final int NUMLTRS = 26;
	private ImageView lastImage;
	
	public GameController(Game game) {
		this.game = game;
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

	public void resetHangman() {
		System.out.println("Reset Hangman Visuals");
	}

	public void initialize() throws IOException {
		System.out.println("in initialize");
		initLtrs();
		drawHangman();
		addTextBoxListener();
		setUpStatusLabelBindings();
	}
	private void initLtrs(){
		for(int i=0;i<NUMLTRS;i++){
			lblLtrs[i]=new Label(Character.toString(ltrs[i]));
			if(i<(NUMLTRS/2)) {
				ltrBoard.getChildren().add(lblLtrs[i]);
			}
			else{
				ltrBoard2.getChildren().add(lblLtrs[i]);
			}
			//lblLtrs[i].setText(Character.toString(ltrs[i]));
		}
	}
	private void addTextBoxListener() {
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
				if(newValue.length() > 0) {
					System.out.print(newValue);
					game.makeMove(newValue);
					drawHangman();
					textField.clear();
				}
			}
		});
	}

	private void setUpStatusLabelBindings() {

		System.out.println("in setUpStatusLabelBindings");
		statusLabel.textProperty().bind(Bindings.format("%s", game.gameStatusProperty()));
		enterALetterLabel.textProperty().bind(Bindings.format("%s", "Enter a letter:"));
		/*	Bindings.when(
					game.currentPlayerProperty().isNotNull()
			).then(
				Bindings.format("To play: %s", game.currentPlayerProperty())
			).otherwise(
				""
			)
		);
		*/
	}

	private void drawHangman() {
		ltrBoard.getChildren().addAll();
		//board.getChildren().add(ltrBoard);
		//board.getChildren().add(ltrBoard2);
    	if(lastImage!=null)
			board.getChildren().remove(lastImage);
    	int tries = Game.getInstance().getTries();
    	System.out.println("Retrieving image from images/" + tries + ".png");
    	Image img = new Image("images/" + tries + ".png");
		ImageView imgView = new ImageView(img);
		lastImage = imgView;
		board.getChildren().add(imgView);
		/*
		Line line = new Line();
		line.setStartX(25.0f);
		line.setStartY(0.0f);
		line.setEndX(25.0f);
		line.setEndY(25.0f);

		Circle c = new Circle();
		c.setRadius(10);

		board.getChildren().add(line);
		board.getChildren().add(c);*/
	}
		
	@FXML 
	private void newHangman() {
		ltrBoard.getChildren().clear();
		ltrBoard2.getChildren().clear();
		board.getChildren().clear();
		game.reset();
	}

	@FXML
	private void quit() {
		board.getScene().getWindow().hide();
	}

}