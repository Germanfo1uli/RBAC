package org.example.util;

public class ThreadState {
    private final int seqNumber;
    private final int maxProgress;
    private volatile long threadId;
    private volatile int progress;
    private volatile long startTime;
    private volatile long endTime;
    private volatile boolean done;

    public ThreadState(int seqNumber, int maxProgress) {
        this.seqNumber = seqNumber;
        this.maxProgress = maxProgress;
    }

    public void start(long threadId) {
        this.threadId = threadId;
        this.startTime = System.currentTimeMillis();
    }

    public void incrementProgress() {
        this.progress++;
    }

    public void finish() {
        this.endTime = System.currentTimeMillis();
        this.done = true;
    }

    public boolean isDone() {
        return done;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public String formatOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Thread %02d | ID: %4d | [", seqNumber, threadId));
        for (int i = 0; i < maxProgress; i++) {
            if (i < progress) {
                sb.append("=");
            } else if (i == progress && !done && threadId != 0) {
                sb.append(">");
            } else {
                sb.append(" ");
            }
        }
        sb.append("] ");
        if (done) {
            sb.append(String.format("%d ms", (endTime - startTime)));
        }
        return sb.toString();
    }
}