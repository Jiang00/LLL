package com.lockscreen.api.liberal.yibuas;

import com.lockscreen.api.liberal.BaseApplication;

import java.lang.ref.WeakReference;


public abstract class LoadingAsync implements Runnable {
    boolean dead = false;
    boolean cancel = false;
    boolean running = false;
    boolean restart = false;
    boolean started = false;
    private final Object lock = new Object();

    public final boolean isFinished() {
        return dead;
    }
    public final boolean isCanceled() {
        return cancel | restart;
    }

    @Override
    public void run() {
        do {
            starting();
            try {
                doInBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
            synchronized (lock) {
                running = false;
            }
            if (!isCanceled() && waiting != null) {
                BaseApplication.runOnUiThread(waiting.get());
                waiting.clear();
                waiting = null;
            }
        } while (restart);

        synchronized (lock) {
            dead = true;
        }
    }

    private void starting() {
        restart = false;
        running = true;
        dead = false;
        cancel = false;
    }

    public void restart(Runnable runnable) {
        synchronized (lock) {
            waiting = new WeakReference<>(runnable);
            if (dead) {
                dead = false;
                start();
            } else if (running) {
                restart = true;
            } else {
                start();
            }
        }
    }

    public synchronized void start() {
        started = true;
        running = true;
        AsyncTaskExecutor.execute(this);
    }

    public synchronized void cancel() {
        AsyncTaskExecutor.remove(this);
        onCancel();
    }

    WeakReference<Runnable> waiting;

    public void waiting(Runnable runnable) {
        synchronized (lock) {
            if (running || restart) {
                waiting = new WeakReference<>(runnable);
            } else if (started) {
                BaseApplication.runOnUiThread(runnable);
            } else {
                waiting = new WeakReference<>(runnable);
                start();
            }
        }
    }

    protected void onCancel() {
        cancel = true;
    }

    protected abstract void doInBackground();
}
