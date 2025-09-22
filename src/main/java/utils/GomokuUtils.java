package main.java.utils;

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

}
