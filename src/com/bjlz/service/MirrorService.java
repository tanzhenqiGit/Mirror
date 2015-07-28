package com.bjlz.service;

import java.io.IOException;

import com.bjlz.receiver.RootStatusReceiver;
import com.bjlz.server.ScreenServer;
import com.bjlz.util.SMLog;



import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.DisplayMetrics;

public class MirrorService extends Service {

    private ScreenServer mServer;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent statusUpdate = new Intent(RootStatusReceiver.ACTION);
        try {
            // fix permissions
            Process proc = 
            		Runtime.getRuntime().exec(new String[]{
            				"/system/bin/su",
            				"-c",
            				"chmod 777 /dev/graphics/fb0"}
            		);
            if (proc.waitFor() != 0) {
                throw new IOException("chmod failed");
            }

            // start server
            Resources res = getApplicationContext().getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            mServer = new ScreenServer(dm, 6100);
            mServer.start();

            // set ok status
            SMLog.i("Server started");
            statusUpdate.putExtra("ret", 0);
            statusUpdate.putExtra("addr", getIp() + ":" + 6100);
            sendBroadcast(statusUpdate);
        } catch (Exception e) {
            // set status with error
            SMLog.e("Cannot start server" + e);
            statusUpdate.putExtra("ret", 1);
            if (e.getMessage() != null) {
                statusUpdate.putExtra("err", e.getMessage());
            } else {
                statusUpdate.putExtra("err", e.getClass());
            }
            sendBroadcast(statusUpdate);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServer != null) {
            mServer.interrupt();
            SMLog.i("Server stopped");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * may use usb dirver.
     * 
     * @return ip address for http service.
     */

    @SuppressWarnings("deprecation")
	private String getIp() 
    {
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String sip = Formatter.formatIpAddress(ip);
        SMLog.d("ip:" + ip + ",sip:" +sip);
        return sip;
    }
}
