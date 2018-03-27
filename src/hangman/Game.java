//Peter Jones
//Arthur Wirsching
//Cheryl Huber
//Ivan Suarez
package hangman;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Game {

	private static Game instance;
	private String answer;
	private String tmpAnswer;
	private String[] letterAndPosArray;
	private ArrayList<String> words;
	private int moves;
	private int index;
	private final String fileName = "src/resources/dictionary.txt";
	private final ReadOnlyObjectWrapper<GameStatus> gameStatus;
	private ObjectProperty<Boolean> gameState = new ReadOnlyObjectWrapper<Boolean>();
	private boolean reset=true;

	/**
	 * Returns string based on selected enum
	 */
	public enum GameStatus {
		GAME_OVER {
			@Override
			public String toString() {
				return "Game over! The word was "+Game.instance.getAnswer().toLowerCase() + ".";
			}
		},
		BAD_GUESS {
			@Override
			public String toString() { return "Bad guess..."; }
		},
		GOOD_GUESS {
			@Override
			public String toString() {
				return "Good guess!";
			}
		},
		WON {
			@Override
			public String toString() {
				return "You won!";
			}
		},
		OPEN {
			@Override
			public String toString() {
				return "Game on, let's go!";
			}
		}
	}

	/**
	 * Constructor. Sets game status, adds keylistener, sets all initial game values
	 */
	public Game() {
		gameStatus = new ReadOnlyObjectWrapper<GameStatus>(this, "gameStatus", GameStatus.OPEN);
		gameStatus.addListener(new ChangeListener<GameStatus>() {
			@Override
			public void changed(ObservableValue<? extends GameStatus> observable,
								GameStatus oldValue, GameStatus newValue) {
				if (gameStatus.get() != GameStatus.OPEN) {
					log("in Game: in changed");
					//currentPlayer.set(null);
				}
			}

		});
		setRandomWord();
		prepTmpAnswer();
		prepLetterAndPosArray();
		moves = 0;
		instance=this;


		gameState.setValue(false); // initial state
		createGameStatusBinding();
	}

	/**
	 * create binding that returns game state
	 */
	private void createGameStatusBinding() {
		List<Observable> allObservableThings = new ArrayList<>();
		ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
			{
				super.bind(gameState);
			}
			@Override
			public GameStatus computeValue() {
				log("in computeValue");
				GameStatus check = checkForWinner(index);
				if(check != null ) {
					return check;
				}

				if(tmpAnswer.trim().length() == 0 && index != -1 || reset==true){
					reset=false;
					log("new game");
					return GameStatus.OPEN;
				}
				else if (index != -1){
					log("good guess");
					return GameStatus.GOOD_GUESS;
				}
				else {
					moves++;
					log("bad guess");
					return GameStatus.BAD_GUESS;
					//printHangman();
				}
			}
		};
		gameStatus.bind(gameStatusBinding);
	}

	public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
		return gameStatus.getReadOnlyProperty();
	}
	public GameStatus getGameStatus() {
		return gameStatus.get();
	}

	/**
	 * selects a word from an external file. If something goes wrong the word "apple" is defaulted to
	 */
	private void setRandomWord() {
		words = new ArrayList<String>();
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));

			//add a string to the list
			while ((line = br.readLine()) != null) {
				line.trim(); // remove new line character
				words.add(line);
			}
			int idx = (int) (Math.random() * words.size());
			answer = words.get(idx);
			System.out.println("The word is: " + answer);
		}catch (FileNotFoundException e) {
			answer = "apple"; //defaults to apple if file not found
			System.out.println("Error. File not found. " + e.getMessage());
		}catch (IOException e){
			answer = "apple"; //defaults to apple if there's an exception
			System.out.println("IO Exception. " + e.getMessage());
		}
	}

	private void prepTmpAnswer() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < answer.length(); i++) {
			sb.append(" ");
		}
		tmpAnswer = sb.toString();
	}

	private void prepLetterAndPosArray() {
		letterAndPosArray = new String[answer.length()];
		for(int i = 0; i < answer.length(); i++) {
			letterAndPosArray[i] = answer.substring(i,i+1);
		}
	}

	private Deque<Integer> getValidIndexes(String input) {
		Deque<Integer> indexes = new ArrayDeque<Integer>();

		for(int i = 0; i < letterAndPosArray.length; i++) {
			if(letterAndPosArray[i].equalsIgnoreCase(input)) {
				indexes.push(i);
				letterAndPosArray[i] = "";
			}
		}
		return indexes;
	}

	private int update(String input) {
		int index = -1;
		Deque<Integer> indexes = getValidIndexes(input);
		StringBuilder sb = new StringBuilder(tmpAnswer);

		//if the stack is empty then so letter was found, return -1. Else return 1 and build the string
		if(!indexes.isEmpty()) {
			index = 1;
			while (!indexes.isEmpty())
				sb.setCharAt(indexes.pop(), input.charAt(0));
		}
		tmpAnswer = sb.toString();

		return index;
	}

	public void makeMove(String letter) {
		log("\nin makeMove: " + letter);
		index = update(letter);
		// this will toggle the state of the game
		gameState.setValue(!gameState.getValue());
	}

	public void reset() {
		moves = 0;
		setRandomWord();
		prepTmpAnswer();
		prepLetterAndPosArray();
		reset=true;
		gameState.setValue(!gameState.getValue());

	}

	private int numOfTries() {
		return 5; // TODO, fix me
	}

	public static void log(String s) {
		System.out.println(s);
	}

	private GameStatus checkForWinner(int status) {
		log("in checkForWinner");
		if(tmpAnswer.equalsIgnoreCase(answer)) {
			log("won");
			return GameStatus.WON;
		}
		else if(moves >= numOfTries()) {
			log("game over");
			moves=6;
			return GameStatus.GAME_OVER;
		}
		else {
			return null;
		}
	}

	/**
	 * Gets number of tries attempted
	 * @return int
	 */
	public int getTries(){
		return moves;
	}

	/**
	 * Gets the answer that the user is working on
	 * @return String
	 */
	public String getTempAnswer(){
		return tmpAnswer;
	}

	/**
	 * Gets the answer of the game
	 * @return String
	 */
	public String getAnswer(){
		return answer;
	}
	public GameStatus getStatus(){
		return gameStatus.get();
	}
}
