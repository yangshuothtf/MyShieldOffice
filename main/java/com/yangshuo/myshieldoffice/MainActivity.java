package com.yangshuo.myshieldoffice;

import android.Manifest;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView txtHello;
    private Button btnUnlock;
    private boolean bMailTaskWorking = false;
    private boolean bServiceStarted = false;
    private String strHello = "";
    private boolean bNeedRequestPermission = false;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //对于安卓6.0以上，如果没有权限，则申请权限。如果有了，就不再申请
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            //2019.2.12 临时注销， 8.0需要PROCESS_OUTGOING_CALLS
            if(PackageManager.PERMISSION_GRANTED!=checkSelfPermission(Manifest.permission.RECORD_AUDIO))
            {
                bNeedRequestPermission = true;
                String [] permissions = {Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_SETTINGS, //android 6.0
                        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG,       //获得去电号码
                        Manifest.permission.WAKE_LOCK, //线程唤醒之后保持清醒的锁
                        Manifest.permission.ACCESS_COARSE_LOCATION, //大体位置
                        Manifest.permission.ACCESS_FINE_LOCATION,   //精确位置
                        Manifest.permission.RECEIVE_BOOT_COMPLETED, //启动完成
                        Manifest.permission.PROCESS_OUTGOING_CALLS, //对外呼叫
                        Manifest.permission.ACCESS_NETWORK_STATE, //检查网络状态
                        Manifest.permission.INTERNET,               //发送邮件
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,//忽略电池优化，避免JobScheduler进入DOZE模式
                        Manifest.permission.GET_ACCOUNTS};
                requestPermissions(permissions, 1);
            }
        }
        setContentView(R.layout.activity_main);
        addShortcut("yangshuo");

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.txtHello);
        //tv.setText(stringFromJNI());
    }
    @Override
    protected void onStart(){
        txtHello = (TextView) findViewById(R.id.txtHello);
        btnUnlock = (Button)findViewById(R.id.btnUnlock);
        Button btnTmp = (Button)findViewById(R.id.btnLoadGPSinfo);
        btnTmp.setEnabled(false);
        btnTmp.setText("1");
        btnTmp = (Button)findViewById(R.id.btnViewMate9Wifi);
        btnTmp.setEnabled(false);
        btnTmp.setText("2");
        btnTmp = (Button)findViewById(R.id.btnView360);
        btnTmp.setEnabled(false);
        btnTmp.setText("3");
        btnTmp = (Button)findViewById(R.id.btnViewMyMate9);
        btnTmp.setEnabled(false);
        btnTmp.setText("4");
        btnTmp = (Button)findViewById(R.id.btnStart);
        btnTmp.setText("5");
        btnTmp.setEnabled(false);
        btnTmp = (Button)findViewById(R.id.btnStop);
        btnTmp.setText("6");
        btnTmp.setEnabled(false);
        btnTmp = (Button)findViewById(R.id.btnSpeechToText);
        btnTmp.setText("0");
        btnTmp.setEnabled(false);

        if(bNeedRequestPermission==false)
        {
            startMyService();
            txtHello.setText(strHello);
        }
        super.onStart();
    }
    private void addShortcut(String name) {
        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
        // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
        // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
        // 屏幕上没有空间时会提示
        // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

        // 名字
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

        // 图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(MainActivity.this,
                        R.drawable.ic_launcher));

        // 设置关联程序
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(MainActivity.this, MainActivity.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        addShortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        // 发送广播
        sendBroadcast(addShortcutIntent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(bNeedRequestPermission)
            {
                startMyService();
                txtHello.setText(strHello);
            }
        } else {
            strHello = "permission denied.";
            txtHello.setText(strHello);
        }
    }

    private void startMyService(){
        //开启服务
        if(bServiceStarted==false)
        {
            //创建文件夹
            CommonParams.path.mkdirs();
            strHello = "service start: " + CfgParamMgr.getInstance().getCurrentTime();
            txtHello.setText(strHello);
            //2019.2.11: keepalive for android 8.0
            Bundle paramBundle = new Bundle();
            paramBundle.putBoolean(CommonParams.TXT_ONCREATE,false);
//            Intent intent = new Intent(this,LocalService.class);
            Intent intent = new Intent(this,MyShieldService.class);
            intent.putExtras(paramBundle);
            //if (Build.VERSION.SDK_INT >= 26) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //安卓8.0以上
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            bServiceStarted = true;
        }
    }

    public void speechToText(View view) {
        PhoneRecordProcess tmpProcess = new PhoneRecordProcess();
        tmpProcess.init(this, true);
    }

    public void loadGPSinfo(View view) {
        String strDeviceID = "865970037231410"+CommonParams.PATTERN_COMMA_SPLIT+"867662020947601"+CommonParams.PATTERN_COMMA_SPLIT
                +"868062022152970"+CommonParams.PATTERN_COMMA_SPLIT+"864678039924814"+CommonParams.PATTERN_COMMA_SPLIT+"868062020476223";
        MailManager.getInstance().getGPSFromOutBox(strDeviceID);
    }

    public void viewMapMate9wifi(View view) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        Bundle paramBundle = new Bundle();
        paramBundle.putString(CommonParams.PARAM_NAME, "865970037231410");
        intent.putExtras(paramBundle);
        startActivity(intent);
    }

    public void viewMap360(View view) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        Bundle paramBundle = new Bundle();
        paramBundle.putString(CommonParams.PARAM_NAME, "867662020947601");
        intent.putExtras(paramBundle);
        startActivity(intent);
    }
    public void viewMapMyMate9(View view) {
        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        Bundle paramBundle = new Bundle();
        paramBundle.putString(CommonParams.PARAM_NAME, "864678039924814");
        intent.putExtras(paramBundle);
        startActivity(intent);
    }

    public void start(View view){
        strHello = "GPS service start: " + CfgParamMgr.getInstance().getCurrentTime();
        txtHello.setText(strHello);
        if(CommonParams.bUseAlarmManager==false)
        {
            if(GPSscheduler.getInstance().getJobStatus()==false)
            {
                GPSscheduler.getInstance().setContext(this);//如果用getApplicationContext()，就会kill掉
                GPSscheduler.getInstance().startScheduler();
            }
        }
        //启动alarm，定时启动GPS
        CfgParamMgr.getInstance().initContext(getApplicationContext());
        CfgParamMgr.getInstance().setGPSserviceFlag(true);
        CfgParamMgr.getInstance().writeCfgFile();
        CfgParamMgr.getInstance().checkAlarmGPS(true);
    }

    public void stop(View view){
        strHello = "GPS service stop: " + CfgParamMgr.getInstance().getCurrentTime();
        txtHello.setText(strHello);
        if(CommonParams.bUseAlarmManager==false)
        {
            if(GPSscheduler.getInstance().getJobStatus()==true)
            {
                GPSscheduler.getInstance().stopScheduler();
            }
        }
        //停止alarm，停止GPS
        CfgParamMgr.getInstance().initContext(getApplicationContext());
        CfgParamMgr.getInstance().setGPSserviceFlag(false);
        CfgParamMgr.getInstance().writeCfgFile();
//        CfgParamMgr.getInstance().checkAlarmGPS(true);
        //TODO: 现在没调用
        //stopMyService();
    }

    public void checkUnlock(View view) {
        if(CfgParamMgr.getInstance().getServerFlag())
        {
            Button btnTmp = (Button)findViewById(R.id.btnLoadGPSinfo);
            btnTmp.setEnabled(true);
            btnTmp.setText("收GPS邮件");
            btnTmp = (Button)findViewById(R.id.btnViewMate9Wifi);
            btnTmp.setEnabled(true);
            btnTmp.setText("查看Mate9无wifi");
//            btnTmp.setClickable(true);
            btnTmp = (Button)findViewById(R.id.btnView360);
            btnTmp.setEnabled(true);
            btnTmp.setText("查看360极客");
            btnTmp = (Button)findViewById(R.id.btnViewMyMate9);
            btnTmp.setEnabled(true);
            btnTmp.setText("查看Mate9金");
            btnTmp = (Button)findViewById(R.id.btnStart);
            btnTmp.setEnabled(true);
            btnTmp.setText("启动GPS");
            btnTmp = (Button)findViewById(R.id.btnStop);
            btnTmp.setEnabled(true);
            btnTmp.setText("停止GPS");
            btnTmp = (Button)findViewById(R.id.btnSpeechToText);
            btnTmp.setText("语音识别");
            btnTmp.setEnabled(true);
            return;
        }
        else
        {
            Button btnTmp = (Button)findViewById(R.id.btnLoadGPSinfo);
            btnTmp.setEnabled(false);
            btnTmp.setText("1");
            btnTmp = (Button)findViewById(R.id.btnViewMate9Wifi);
            btnTmp.setEnabled(false);
            btnTmp.setText("2");
            btnTmp = (Button)findViewById(R.id.btnView360);
            btnTmp.setEnabled(false);
            btnTmp.setText("3");
            btnTmp = (Button)findViewById(R.id.btnViewMyMate9);
            btnTmp.setEnabled(false);
            btnTmp.setText("4");
            btnTmp = (Button)findViewById(R.id.btnStart);
            btnTmp.setText("5");
            btnTmp.setEnabled(false);
            btnTmp = (Button)findViewById(R.id.btnStop);
            btnTmp.setText("6");
            btnTmp.setEnabled(false);
            btnTmp = (Button)findViewById(R.id.btnSpeechToText);
            btnTmp.setText("0");
            btnTmp.setEnabled(false);
        }

        if(bMailTaskWorking==false)
        {
            bMailTaskWorking = true;
            if(MailManager.getInstance().bTaskRunning==false)
            {
                strHello = "Mail process start";
                txtHello.setText(strHello);
                MailManager.getInstance().sendMail(CommonParams.MAIL_TITLE_HEARTBEAT+"设备ID:"+CfgParamMgr.getInstance().getDeviceID()+"."+CfgParamMgr.getInstance().getMachineName(), "Heartbeat sent");
                MailManager.getInstance().receiveCommandMail();
                strHello = "Mail task finished";
                txtHello.setText(strHello);
            }
            else
            {
                strHello = "Mail processing, please wait";
                txtHello.setText(strHello);
            }
            bMailTaskWorking = false;
        }
    }
    protected void onResume(){
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date(System.currentTimeMillis());
//        txtHello.setText("定位重启onResume："+df.format(date));
//        MailManager.getInstance().sendMail("定位重启onResume："+df.format(date), "XXXX");
        super.onResume();
    }
    protected void onPause(){
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date(System.currentTimeMillis());
//        txtHello.setText("定位暂停onPause："+df.format(date));
//        MailManager.getInstance().sendMail("定位暂停onPause："+df.format(date), "XXXX");
        super.onPause();
    }

    private void stopMyService(){
        txtHello = (TextView) findViewById(R.id.txtHello);
        strHello = "service stop";
        txtHello.setText(strHello);
        //停止服务
        if(bServiceStarted==true) {
            Intent intent = new Intent(this, MyShieldService.class);
            stopService(intent);
//        Intent intentGPS = new Intent(this,GPSService.class);
//        stopService(intentGPS);
            GPSscheduler.getInstance().stopScheduler();
            bServiceStarted = false;
        }
    }
    public void onDestroy() {
        super.onDestroy();
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
//TODO: signed key 密码: 810426     http://lbs.amap.com/dev/key/app
/**
 * 1.装好之后要立即运行，才会创建图标，并开启服务
 * 2.设置->隐私管理->给自启动权限
 * 3.电池->省电管理->应用保护
 * 4.通话设置->电话录音打开
 * 360：
 * 通话设置->电话录音打开
 * 应用管理->自启动/权限控制
 * 应用权限管理->权限管理/受保护应用
 * 应用自启动
 * 电池和省电->锁屏受保护应用
 <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
 <uses-permission android:name="android.permission.BIND_INCALL_SERVICE" />
 <uses-permission android:name="android.permission.BIND_TELECOM_CONNECTION_SERVICE" />
 <uses-permission android:name="android.permission.CALL_PHONE" />

获取sha1 key--release
 1. Build->Generate signed apk-->Key store path-->Create new, then generate a jks file
 2.  C:\Program Files\Java\jdk-11.0.1\bin>keytool -v -list -keystore C:\AndroidApp\office.yangshuo.jks
  SHA1: 3F:EF:CD:90:25:90:80:6D:AF:03:3E:53:15:A5:65:75:3C:B1:28:28
 获取sha1 key--debug
 1.  C:\Program Files\Java\jdk-11.0.1\bin>keytool -v -list -keystore C:\Users\shuoyang\.android\debug.keystore
 password : android
 SHA1: C1:B2:5B:06:FB:27:88:AD:36:43:05:28:28:38:40:C6:6A:54:8E:03
 */