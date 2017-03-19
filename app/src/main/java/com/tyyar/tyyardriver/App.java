package com.tyyar.tyyardriver;

import android.app.Application;

import com.blankj.utilcode.utils.Utils;
import com.squareup.leakcanary.LeakCanary;

/**
 * User: YourPc
 * Date: 1/31/2017
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
        Utils.init(this);

    }
}
