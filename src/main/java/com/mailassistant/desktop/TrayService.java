package com.mailassistant.desktop;

/** Future adapters must marshal callbacks onto the JavaFX application thread. */
public interface TrayService extends AutoCloseable {
    boolean available();
    void install(Runnable showWindow, Runnable showProfile, Runnable showDrafts, Runnable quit);
    @Override void close();
    static TrayService disabled() {
        return new TrayService() {
            public boolean available() { return false; }
            public void install(Runnable showWindow, Runnable showProfile, Runnable showDrafts, Runnable quit) {}
            public void close() {}
        };
    }
}
