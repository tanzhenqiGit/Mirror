package com.bjlz.handler;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.bjlz.util.EncodeThread;
import com.bjlz.util.SMLog;

import android.util.DisplayMetrics;

public class MJPEGHandler extends HttpHandler {


    public MJPEGHandler(Socket sock, DisplayMetrics dm) {
        super(sock);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mFrameBuffer = new byte[mWidth * mHeight * 4];
        mSendThread = new EncodeThread("sendBufferThread", sock, dm);
        mSendThread.start();
    }

    @Override
    public void run() {
        try {
        	SMLog.i("SM", "Send buffer size: " + getSock().getSendBufferSize());
            OutputStream os = getSock().getOutputStream();
            String boundary = "screenFrame";
            os.write(("HTTP/1.1 200 OK\r\n" +
                    "Date: " + getServerTime() + "\r\n" +
                    "Server: " + SERVER_NAME + "\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=--" +
                    boundary + "\r\n" +
                    "Cache-Control: no-cache, private\r\n" +
                    "Pragma: no-cache\r\n" +
                    "Max-Age: 0\r\n" +
                    "Expires: 0\r\n" +
                    "Connection: keep-alive\r\n\r\n").getBytes());
            while (!isInterrupted()) {

                // grab screenshot ~ 10ms no root
                DataInputStream dis = new DataInputStream(new FileInputStream("/dev/graphics/fb0"));
                int size = dis.read(mFrameBuffer);
                dis.close();
                sleep(600);
//                if (!needToSend()) {
//                	continue;
//                }
                SMLog.i("SM", "read size = " + size);
                mSendThread.add(mFrameBuffer);
            }
            getSock().close();
        } catch (Exception e) {
        	SMLog.e("MJPEG stream error" + e);
        }
    }

    @SuppressWarnings("unused")
    private class Timer {
        private Map<String, Long> mTimers;

        public Timer() {
            mTimers = new HashMap<String, Long>();
        }

        public void reset(String name) {
            mTimers.put(name, System.currentTimeMillis());
        }

        public void stop(String name) {
            if (mTimers.containsKey(name)) {
                long curr = System.currentTimeMillis();
                SMLog.i("SM", name + " timing is " + (curr - mTimers.get(name))
                        + "ms");
            }
        }
    }

    @SuppressWarnings("unused")
    private class Speed {
        private long mSpeedStart;

        public Speed() {
            reset();
        }

        public void reset() {
            mSpeedStart = System.currentTimeMillis();
        }

        public void print(int sent) {
            float kb = sent / 1024.0f;
            float dt = (System.currentTimeMillis() - mSpeedStart) / 1000.0f;
            if (dt != 0) {
            	SMLog.i("SM", "Speed " + (kb / dt) + "KB/s");
            }
        }
    }
    

    boolean needToSend()
    {
    	mIndex++;
    	if (mIndex == 20)
    	{
    		mIndex = 0;
    		return true;
    	} else {
    		return false;
    	}
    }
    
    private int mIndex = 0;
    /**
     * value 0 ~ 30
     * used for control get frames in one second.
     */
    private byte[] mFrameBuffer;
    private int mWidth;
    private int mHeight;
    private EncodeThread mSendThread;
}
