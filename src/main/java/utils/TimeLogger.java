package main.java.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TimeLogger {
    private static final Map<String, Long> timers = new HashMap<>();

    public static void time(String name, Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long elapsed = System.currentTimeMillis() - start;
        timers.merge(name, elapsed, Long::sum);
    }

    public static <T> T time(String name, Supplier<T> task) {
        long start = System.currentTimeMillis();
        T result = task.get();
        long elapsed = System.currentTimeMillis() - start;
        timers.merge(name, elapsed, Long::sum);
        return result;
    }

    public static void startTimer(String name) {
        timers.merge(name, - System.currentTimeMillis(), Long::sum);
    }

    public static void stopTimer(String name) {
        timers.merge(name, System.currentTimeMillis(), Long::sum);
    }

    public static void reset(String name) {
        timers.put(name, 0L);
    }

    /* Print */

    public static void printTimes() {
        timers.forEach((k, v) -> 
            System.out.println(k + " took " + v + " ms in total")
        );
    }

    public static void printTime(String name) {
        System.out.println(name + " took " + timers.get(name) + " ms in total");
    }

    public static void reset() {
        timers.clear();
    }

    public static void resetAiTimers() {
        TimeLogger.reset("getBestMove");
        TimeLogger.reset("sort");
        TimeLogger.reset("boardAction");
        TimeLogger.reset("boardScan");
        TimeLogger.reset("computeSequenceData");
        TimeLogger.reset("getScoreAt");
    }

    public static void printAiTime(){
        long totalTime = timers.get("getBestMove");
        long sortTimer = timers.get("sort");
        long boardActionTimer = timers.get("boardAction");
        long boardScanTimer = timers.get("boardScan");
        long copyLastHistoryTimer = timers.get("copyLastHistory");
        long computeSequenceDataTimer = timers.get("computeSequenceData");
        long getScoreAtTimer = timers.get("getScoreAt");
        System.out.printf("Execution time:  %d ms%n", totalTime);
        System.out.printf("  sort:          | %d ms (%.2f%%)%n", sortTimer, (sortTimer * 100.0) / totalTime);
        System.out.printf("  boardAction:   | %d ms (%.2f%%)%n", boardActionTimer, (boardActionTimer * 100.0) / totalTime);
        System.out.printf("  boardScan:     | %d ms (%.2f%%)%n", boardScanTimer, (boardScanTimer * 100.0) / totalTime);
        System.out.printf("    cpHistory:   | | %d ms (%.2f%%)%n", copyLastHistoryTimer, (copyLastHistoryTimer * 100.0) / boardScanTimer);
        System.out.printf("    compSeqData: | | %d ms (%.2f%%)%n", computeSequenceDataTimer, (computeSequenceDataTimer * 100.0) / boardScanTimer);
        System.out.printf("    getScoreAt:  | | %d ms (%.2f%%)%n", getScoreAtTimer, (getScoreAtTimer * 100.0) / boardScanTimer);
        long other = totalTime - (sortTimer + boardActionTimer + boardScanTimer);
        System.out.printf("  other:         | %d ms (%.2f%%)%n", other, (other * 100.0) / totalTime);
    }
}
