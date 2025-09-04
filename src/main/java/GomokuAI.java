package main.java;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

public class GomokuAI {
    private BoardGame game;

    private final LongProperty player1Score = new SimpleLongProperty(1);
    private final LongProperty player2Score = new SimpleLongProperty(1);
    private final DoubleProperty percentage = new SimpleDoubleProperty(0f);

    public LongProperty player1ScoreProperty() { return player1Score; }
    public LongProperty player2ScoreProperty() { return player2Score; }
    public DoubleProperty percentageProperty() { return percentage; }

    Pattern[] patterns = {
        new Pattern(new int[]{1,1,1,1,1}, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,1,1,0}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,1,0,1}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,0,1,1}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,0,1,1,1}, 10000, Integer.MAX_VALUE),
        new Pattern(new int[]{2,1,1,1,1,0}, 150, Integer.MAX_VALUE),
        new Pattern(new int[]{0,1,1,1,0,0}, 150, 10000),
        new Pattern(new int[]{0,1,1,1,0}, 100, 150),
        new Pattern(new int[]{2,1,1,1,0,0}, 50, 150),
        new Pattern(new int[]{2,1,0,1,0,1,0}, 50, 100),
        new Pattern(new int[]{0,1,1,0}, 20, 30),
        new Pattern(new int[]{2,1,1,0}, 10, 15),
    };
    Pattern[] patterns2;

    GomokuAI(BoardGame game){
        this.game = game;

        createPattern2();
        game.currentPlayerProperty().addListener((obs, oldVal, newVal) -> {
            evaluate();
        });
    }

    public class Pattern {
        int [] pattern;
        long score[] = new long[2]; // store the basic score, and the score if its the player turn

        Pattern(int [] pattern, long score){
            this.pattern = pattern;
            this.score[0] = score;
            this.score[1] = score;
        }

        Pattern(int [] pattern, long score, long scorePlayerTurn){
            this.pattern = pattern;
            this.score[0] = score;
            this.score[1] = scorePlayerTurn;
        }
    }

    //copy patterns but reverse player 1 and player 2
    private void createPattern2(){
        patterns2 = new Pattern[patterns.length];
        for (int i = 0; i < patterns.length; i++){
            patterns2[i] = new Pattern(new int[patterns[i].pattern.length], patterns[i].score[0], patterns[i].score[1]);
            for (int j = 0; j < patterns[i].pattern.length; j++) {
                if (patterns[i].pattern[j] == 0) patterns2[i].pattern[j] = 0;
                else if (patterns[i].pattern[j] == 1) patterns2[i].pattern[j] = 2;
                else if (patterns[i].pattern[j] == 2) patterns2[i].pattern[j] = 1;
            }
        }
    }

    public void evaluate(){
        System.out.println("Evaluating");
        player1Score.set(findAndSumMatch(patterns, 1, 2, game.getCurrentPlayer() == 1 ? 1 : 0));
        player2Score.set(findAndSumMatch(patterns2, 2, 1, game.getCurrentPlayer() == 2 ? 1 : 0));

        System.out.println("Player1 score: " + player1Score.get());
        System.out.println("Player2 score: " + player2Score.get());

        double score1 = (double)player1Score.get();
        double score2 = (double)player2Score.get();
        percentage.set(score2 / (score1 + score2));
    }

    private long findAndSumMatch(Pattern[] patterns, int player, int opponent, int playerTurn){
        long sum = 100; //default score
        for (int r = 0; r < game.BOARD_SIZE; r++) {
            for (int c = 0; c < game.BOARD_SIZE; c++) {
                for (int[] dir: game.DIRECTION){
                    for (Pattern pat : patterns){
                        if (game.checkSequenceMatch(r, c, pat.pattern.length, 0, pat.pattern, dir, (p, cell) -> p == cell.player && !cell.can_be_captured, opponent)){
                            sum += pat.score[playerTurn];
                            break;
                        }
                    }
                }
                // gain one point per piece
                if (game.getTileState(r, c) == player){
                    sum += 1;
                }
                Cell cell = game.getCell(r, c);
                if (playerTurn == 1){
                    sum += cell.can_be_captured ? 10 : 0; // 10 point per capturable piece
                    sum += cell.isFreeThree() ? 5 : 0; // 5 point per isFreeThree
                }
            }
        }
        return sum;
    }
}
