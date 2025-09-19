package main.java;

public class GomokuUtils {
    
    public static void debugCaller() {
        System.out.println("----- Debug Caller -----");
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (e.getClassName().startsWith("main.java")) {
                System.out.println(e);
            }
        }
    }

}
