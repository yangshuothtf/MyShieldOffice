package com.yangshuo.myshieldoffice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.yangshuo.myshieldserver.IMyAidlInterface;

import java.io.File;

public class LocalService extends Service {
    private static final String TAG = "LocalService";
    private IMyAidlInterface iMyAidlInterface;
    private boolean mIsBound;

//    private TelephonyManager tm = null;// 电话管理器
//    private PhoneListener mListener = null; // 监听器对象
//    private LocalService.OutCallReceiver receiver = null;//去电接收者

    @Override
    public void onCreate() {
        //if (Build.VERSION.SDK_INT >= 26) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 通知渠道的id
            String CHANNEL_ID = "my_channel_01";
            // Create a notification and set the notification channel.
            Notification notification = new  NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("LocalService title") .setContentText("LocalService text.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
            startForeground(1, notification);
        }

        super.onCreate();
        Log.e(TAG, "onCreate: 创建 LocalService");

//        // 模拟 5 秒后解除绑定
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                unbindRemoteService();
//            }
//        }, 5000);

        bindRemoteService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " );
        boolean bSendOnlineNotify = false;
        Bundle bundle=intent.getExtras();
        if(bundle.isEmpty()==false)
        {
            bSendOnlineNotify = bundle.getBoolean(CommonParams.TXT_ONCREATE, false);
        }
        Intent i = new Intent(this,MyShieldService.class);
        Bundle paramBundle = new Bundle();
        paramBundle.putBoolean(CommonParams.TXT_ONCREATE,bSendOnlineNotify);
        i.putExtras(paramBundle);
        //if (Build.VERSION.SDK_INT >= 26) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //安卓8.0以上
            startForegroundService(i);
        } else {
            startService(i);
        }
        return START_STICKY;
/*
        Bundle bundle=intent.getExtras();
        boolean bSendOnlineNotify = bundle.getBoolean(CommonParams.TXT_ONCREATE, true);
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
        //注册去电广播接收者
        if(receiver==null)
        {
            receiver = new LocalService.OutCallReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
            registerReceiver(receiver, filter);
        }
        //使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
        return START_REDELIVER_INTENT;
        */
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: 绑定 LocalService");
        return stub;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: 解绑 LocalService");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
/*        //取消注册去电广播接收者
        unregisterReceiver(receiver);
        receiver = null;
        tm.listen(mListener, PhoneStateListener.LISTEN_NONE);
        mListener = null;
        tm = null;
        */
        Log.e(TAG, "onDestroy: 销毁 LocalService");
        super.onDestroy();
    }

    private IMyAidlInterface.Stub stub = new IMyAidlInterface.Stub() {
        @Override
        public void bindSuccess() throws RemoteException {
            Log.e(TAG, "bindSuccess: RemoteService 绑定 LocalService 成功");
        }

        @Override
        public void unbind() throws RemoteException {
            getApplicationContext().unbindService(connection);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: RemoteService 链接成功");
            mIsBound = true;
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                iMyAidlInterface.bindSuccess();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected: RemoteService 断开连接，重新启动");
            mIsBound = false;
            createTransferActivity();
        }
    };

    private void createTransferActivity() {
        Intent intent = new Intent(this, TransferActivity.class);
        intent.setAction(TransferActivity.ACTION_FROM_SELF);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void bindRemoteService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.yangshuo.myshieldserver", "com.yangshuo.myshieldserver.RemoteService"));
        if (!getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "bindRemoteService: 绑定 RemoteService 失败");
            stopSelf();
        }
    }

    /**
     * 解除绑定 RemoteService
     */
    private void unbindRemoteService() {
        if (mIsBound) {
            try {
                // 先让 RemoteService 解除绑定 LocalService
                iMyAidlInterface.unbind();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            getApplicationContext().unbindService(connection);  // 解除 LocalService 与 RemoteService
            stopSelf();
        }
    }

    /**
     * 去电广播接收者
     */
/*    class OutCallReceiver extends BroadcastReceiver {
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
}
