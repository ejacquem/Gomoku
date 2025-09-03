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
        new Pattern(new int[]{1,1,1,1,0}, 1000),
        new Pattern(new int[]{1,1,1,0}, 100),
        new Pattern(new int[]{1,1,0}, 10),
        new Pattern(new int[]{1,0}, 2),
        new Pattern(new int[]{1}, 1),
    };

    GomokuAI(BoardGame game){
        this.game = game;

        game.currentPlayerProperty().addListener((obs, oldVal, newVal) -> {
            evaluate();
        });
    }

    public class Pattern {
        int [] patern;
        long score;

        Pattern(int [] patern, int score){
            this.patern = patern;
            this.score = score;
        }
    }

    public void evaluate(){
        System.out.println("Evaluating");
        player1Score.set(findAndSumMatch(patterns, 1, 2));
        player2Score.set(findAndSumMatch(patterns, 2, 1));

        System.out.println("Player1 score: " + player1Score.get());
        System.out.println("Player2 score: " + player2Score.get());

        double score1 = (double)player1Score.get();
        double score2 = (double)player2Score.get();
        percentage.set(score2 / (score1 + score2));
    }

    private long findAndSumMatch(Pattern[] patterns, int player, int opponent){
        long sum = 100; //default score
        for (int r = 0; r < game.BOARD_SIZE; r++) {
            for (int c = 0; c < game.BOARD_SIZE; c++) {
                for (Pattern pat : patterns){
                    int[] pattern = new int[pat.patern.length];
                    for (int i = 0; i < pat.patern.length; i++) {
                        pattern[i] = pat.patern[i] * player;
                    }
                    for (int[] dir: game.DIRECTION){
                        if (game.checkSequenceMatch(r, c, pattern.length, pattern, dir, opponent))
                            sum += pat.score;
                    }
                }
            }
        }
        return sum;
    }
}
