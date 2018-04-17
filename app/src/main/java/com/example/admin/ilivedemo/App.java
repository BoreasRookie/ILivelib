package com.example.admin.ilivedemo;

import android.app.Application;
import android.content.Context;

import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveManager;

/**
 * Created by admin on 2017/11/30.
 */

public class App extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        ILiveSDK.getInstance().initSdk(this.getApplicationContext(),Constants.SDK_APPID,Constants.ACCOUNT_TYPE);
        //初始化直播场景
        ILVLiveConfig liveConfig = new ILVLiveConfig();
        ILVLiveManager.getInstance().init(liveConfig);
    }
    public static Context getAppContext(){
        return context;
    }
}
