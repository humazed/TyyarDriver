package com.tyyar.tyyardriver;

import android.app.Application;

import com.blankj.utilcode.utils.Utils;

/**
 * User: YourPc
 * Date: 1/31/2017
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);

    }
}