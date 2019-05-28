package com.company;

public class ThreadStreamerPair {
    private Thread thread;
    private Streamer streamer;

    public ThreadStreamerPair(Thread thread, Streamer streamer) {
        this.thread = thread;
        this.streamer = streamer;
    }

    public Thread getThread() {
        return thread;
    }

    public Streamer getStreamer() {
        return streamer;
    }
}
