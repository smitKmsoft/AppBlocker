package com.km.mylibrary;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.km.mylibrary.detectors.Detector;
import com.km.mylibrary.detectors.LollipopDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class AppChecker {
    static final int DEFAULT_TIMEOUT = 1000;

    int timeout = DEFAULT_TIMEOUT;
    ScheduledExecutorService service;
    Runnable runnable;
    Listener unregisteredPackageListener;
    Listener anyPackageListener;
    Map<String, Listener> listeners;
    Detector detector;
    Handler handler;

    public interface Listener {
        void onForeground(String process);
    }


    public AppChecker() {
        listeners = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
        detector = new LollipopDetector();


    }

    public AppChecker timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public AppChecker when(String packageName, Listener listener) {
        listeners.put(packageName, listener);
        return this;
    }

    @Deprecated
    public AppChecker other(Listener listener) {
        return whenOther(listener);
    }

    public AppChecker whenOther(Listener listener) {
        unregisteredPackageListener = listener;
        return this;
    }

    public AppChecker whenAny(Listener listener) {
        anyPackageListener = listener;
        return this;
    }

    public void start(Context context) {
        runnable = createRunnable(context.getApplicationContext());
        service = new ScheduledThreadPoolExecutor(1);
        service.schedule(runnable, timeout, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if(service != null) {
            service.shutdownNow();
            service = null;
        }
        runnable = null;
    }

    private Runnable createRunnable(final Context context) {
        return new Runnable() {
            @Override
            public void run() {
                getForegroundAppAndNotify(context);
                service.schedule(createRunnable(context), timeout, TimeUnit.MILLISECONDS);
            }
        };
    }

    private void getForegroundAppAndNotify(Context context) {
        final String foregroundApp = getForegroundApp(context);
        boolean foundRegisteredPackageListener = false;
        if(foregroundApp != null) {
            for (String packageName : listeners.keySet()) {
                if (packageName.equalsIgnoreCase(foregroundApp)) {
                    foundRegisteredPackageListener = true;
                    callListener(listeners.get(foregroundApp), foregroundApp);
                }
            }

            if(!foundRegisteredPackageListener && unregisteredPackageListener != null) {
                callListener(unregisteredPackageListener, foregroundApp);
            }
        }
        if(anyPackageListener != null) {
            callListener(anyPackageListener, foregroundApp);
        }
    }

    void callListener(final Listener listener, final String packageName) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onForeground(packageName);
            }
        });
    }

    public String getForegroundApp(Context context) {
        return detector.getForegroundApp(context);
    }
}