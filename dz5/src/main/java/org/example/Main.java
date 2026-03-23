package org.example;

import org.example.util.ThreadState;
import org.example.util.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int threadCount = 7;
        int calculationLength = 40;

        List<ThreadState> states = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            states.add(new ThreadState(i + 1, calculationLength));
        }

        for (int i = 0; i < threadCount; i++) {
            System.out.println();
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (ThreadState state : states) {
            executor.submit(new Worker(state));
        }
        executor.shutdown();

        boolean allDone = false;
        while (!allDone) {
            System.out.print("\033[" + threadCount + "A");
            allDone = true;

            for (ThreadState state : states) {
                System.out.print("\033[2K");
                System.out.println(state.formatOutput());
                if (!state.isDone()) {
                    allDone = false;
                }
            }
            Thread.sleep(60);
        }

        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
}