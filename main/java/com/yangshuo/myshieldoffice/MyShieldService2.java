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
//	private OutCallReceiver receiver = null;//去电接收者
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle=intent.getExtras();
//		String strMachineType =bundle.getString(CommonParams.PARAM_TYPE);
//    boolean bUseSysRecorder = bundle.getBoolean(CommonParams.PARAM_SYSRECORDER, true);
		boolean bSendOnlineNotify = false;
		if(bundle.isEmpty()==false)
		{
			bSendOnlineNotify = bundle.getBoolean(CommonParams.TXT_ONCREATE, false);
		}
		//启动alarm，定时启动GPS
		CfgParamMgr.getInstance().initContext(getApplicationContext());
//		CfgParamMgr.getInstance().readCfgFile();//读进状态
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
		/*2019.2.13: 8.0中，permission denial，后台服务权限不够，拿不到这个消息
        //注册去电广播接收者
		if(receiver==null)
		{
			receiver = new OutCallReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
			registerReceiver(receiver, filter);
		}*/
		//       return super.onStartCommand(intent, flags, startId);
		//使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
		return START_REDELIVER_INTENT;
	}
    /* 2019.2.13: 8.0中，permission denial，后台服务权限不够，拿不到这个消息
   // 去电广播接收者
	class OutCallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
				mListener.strCallNumber += "." + intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER) +".out";
				// 这就是我们拿到的播出去的电话号码
				//这个消息晚于PhoneStateListener，迟到了
				//mlistener.strCallNumber = getResultData();
				//实际上,下面的代码从未被执行
				if(mListener.strLastCallName.isEmpty()==false)
				{
					if(mListener.callStatus==TelephonyManager.CALL_STATE_IDLE)
					{
						//改变文件名
						File file = new File(CommonParams.path, mListener.strLastCallName);
						if(file.exists())
						{
							String strLastCallNewName = mListener.strLastCallTime + mListener.strCallNumber + ".aac";
							file.renameTo(new File(CommonParams.path, strLastCallNewName));
							mListener.strLastCallName = "";
							mListener.strLastCallTime = "";
							mListener.strCallNumber = "";
						}
					}
				}
			}
		}
	}
	*/
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
		/* 2019.2.13: 8.0中，permission denial，后台服务权限不够，拿不到这个消息
		//取消注册去电广播接收者
		unregisterReceiver(receiver);
		receiver = null;
        */
		tm.listen(mListener, PhoneStateListener.LISTEN_NONE);
		mListener = null;
		tm = null;
		super.onDestroy();
		// 取消电话的监听,采取线程守护的方法，当一个服务关闭后，开启另外一个服务，除非你很快把两个服务同时关闭才能完成
		Intent i = new Intent(this,MyShieldService.class);
		Bundle paramBundle = new Bundle();
//		paramBundle.putString(CommonParams.PARAM_TYPE, CfgParamMgr.getInstance().getMachineType());//360,X900
//		paramBundle.putBoolean(CommonParams.PARAM_SYSRECORDER,CfgParamMgr.getInstance().getSysRecorderFlag());
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
