package com.yangshuo.myshieldoffice;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * 监听android开机广播（只要手机一开机就开始监听）
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        Bundle paramBundle = new Bundle();
        paramBundle.putBoolean(CommonParams.TXT_ONCREATE, true);
        //2019.2.11: keepalive for android 8.0
//        Intent i = new Intent(context, LocalService.class);
        Intent i = new Intent(context,MyShieldService.class);
        i.putExtras(paramBundle);
        //if (Build.VERSION.SDK_INT >= 26) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(i);
        } else {
            context.startService(i);
        }
    }
}
