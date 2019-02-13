package com.yangshuo.myshieldoffice;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyShieldService2 extends Service {
	// 电话管理器
	private TelephonyManager tm = null;
	// 监听器对象
	private PhoneListener mListener = null;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle=intent.getExtras();
		boolean bSendOnlineNotify = false;
		if(bundle.isEmpty()==false)
		{
			bSendOnlineNotify = bundle.getBoolean(CommonParams.TXT_ONCREATE, false);
		}
		//启动alarm，定时启动GPS
		CfgParamMgr.getInstance().initContext(getApplicationContext());
		CfgParamMgr.getInstance().checkAlarmGPS(true);

		// 后台监听电话的呼叫状态。
		if(mListener==null)
		{
			mListener = new PhoneListener(getApplicationContext());
		}
		// 得到电话管理器
		if(tm==null)
		{
			tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			tm.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		if(bSendOnlineNotify)
		{
			mListener.sendOnlineNotify();
		}
		//       return super.onStartCommand(intent, flags, startId);
		//使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
		return START_REDELIVER_INTENT;
	}
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 服务创建的时候调用的方法
	 */
	@Override
	public void onCreate() {
		// https://www.jianshu.com/p/71e16b95988a
		// https://blog.csdn.net/u010784887/article/details/79675147
		//if (Build.VERSION.SDK_INT >= 26) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// 通知渠道的id
			String CHANNEL_ID = "my_channel_01";
			// Create a notification and set the notification channel.
			Notification notification = new  NotificationCompat.Builder(this,CHANNEL_ID )
					.setContentTitle("MyShieldService2 title") .setContentText("MyShieldService2 text.")
					.setSmallIcon(R.drawable.ic_launcher_foreground)
					.build();
			startForeground(1,notification);
		}
		super.onCreate();
	}

	/**
	 * 服务销毁的时候调用的方法
	 */
	@Override
	public void onDestroy() {
		tm.listen(mListener, PhoneStateListener.LISTEN_NONE);
		mListener = null;
		tm = null;
		super.onDestroy();
		// 取消电话的监听,采取线程守护的方法，当一个服务关闭后，开启另外一个服务，除非你很快把两个服务同时关闭才能完成
		Intent i = new Intent(this,MyShieldService.class);
		Bundle paramBundle = new Bundle();
        paramBundle.putBoolean(CommonParams.TXT_ONCREATE,false);
		i.putExtras(paramBundle);
		// https://www.jianshu.com/p/71e16b95988a
		// https://blog.csdn.net/u010784887/article/details/79675147
		stopForeground(true);
		//if (Build.VERSION.SDK_INT >= 26) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			//安卓8.0以上
			startForegroundService(i);
		} else {
			startService(i);
		}
	}
}
