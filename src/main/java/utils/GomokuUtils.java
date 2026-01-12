package main.java.utils;

import main.java.app.GameSettings;

public class GomokuUtils {
    
    public static void debugCaller() {
        System.out.println("----- Debug Caller -----");
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().startsWith("main.java")) {
                System.out.println(e);
            }
        }
    }

    public static void printLoadingBar(double percent) {
        int barWidth = 50; // number of characters in the bar
        int filled = (int) (percent * barWidth);
    
        StringBuilder bar = new StringBuilder();
        bar.append('[');
        for (int i = 0; i < barWidth; i++) {
            if (i < filled) {
                bar.append('=');
            } else if (i == filled) {
                bar.append('>');
            } else {
                bar.append(' ');
            }
        }
        bar.append(']');
        bar.append(String.format(" %6.2f%%", percent * 100));
    
        System.out.print("\r" + bar.toString()); // overwrite the line
        if (percent >= 1.0) {
            System.out.println(); // move to next line when done
        }
    }

    public static String indexToString(int index){
        int x = (index % GameSettings.BOARD_SIZE) - 1;
        int y = (index / GameSettings.BOARD_SIZE) - 1;
        String col = Integer.toString(x + 10, 36).toUpperCase();
        String row = Integer.toString(GameSettings.GAME_SIZE - y);
        return col + row;
    }

    public static int stringToIndex(String str){
        int i = 0;
        while (i < str.length() && Character.isLetter(str.charAt(i))) i++;
    
        String colStr = str.substring(0, i).toUpperCase();
        String rowStr = str.substring(i);
    
        int col = 0;
        for (char c : colStr.toCharArray()) {
            col = col * 26 + (c - 'A');
        }
        int row = GameSettings.GAME_SIZE - (Integer.parseInt(rowStr));

        return (col + 1) + (row + 1) * GameSettings.BOARD_SIZE;
    }

    public static double analysisScoreToReadableScore(int score) {
        if (score < 10 && score > -10) {
            return (float)score / 10f;
        }
        return Math.log10(Math.abs(score)) * Integer.signum(score);
    }
}
