/**
 * @author tan_zhenq E-mail: tan_zhenqi@163.com
 * @date 创建时间：2015-7-25 下午1:51:03 
 * @version 1.0 
 * @parameter  
 * @since  
 * @return  
 */
package com.bjlz.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;

/**
 * @author lz100
 * thread used for write buffer in file.
 */
public class EncodeThread extends Thread {

	public EncodeThread(String name,Socket sock, DisplayMetrics dm) 
	{
		super(name);
		mList = new LinkedList<EncodeItem>();
		mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
        mColors = new int[mWidth * mHeight];
        mSock = sock;
        try {
			mOs = mSock.getOutputStream();
		} catch (IOException e) {
			SMLog.e("EncodeThread catch exception " + e);
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		while(!isInterrupted()) {
			EncodeItem item = pop();
			//mFileRecord.writeTofile(item.getBuffer());
			sendToOut(item.getBuffer());
			synchronized (mList) {
				if (mList.size() >= 50) {
				mList.clear();
				}
			}
			SMLog.d("sendToOut end");
		}
	}
	

	public void add(byte[] buffer){
		synchronized (mList) {
			if (mList.size() == 0) {
				mList.notifyAll();
			}
			mList.add(new EncodeItem(mIndex, buffer));
			mIndex++;
		}
	}
	
	private EncodeItem pop()
	{
		EncodeItem item = null;
		synchronized (mList) {
			if (mList.size() == 0) {
				try {
					SMLog.i("EncodThread run wait");
					mList.wait();
					SMLog.i("EncodThread run wait notify");
				} catch (InterruptedException e) {
					SMLog.e("EncodeThread wait interrupted exception:" + e);
					e.printStackTrace();
				}
			}
			
			item = mList.poll();
			SMLog.d("pop list.size:" + mList.size() + ",index=" + item.getIndex());
		}
		return item;
	}
	
	private void sendToOut(byte[] buffer)
	{
		  int r, g, b, a, index;
          int hash = 0;
          for (int m = 0; m < mColors.length; m++) {
              index = m * 4;
              r = (buffer[index] & 0xFF);
              g = (buffer[index + 1] & 0xFF);
              b = (buffer[index + 2] & 0xFF);
              a = (buffer[index + 3] & 0xFF);
              mColors[m] = (a << 24) | (b << 16) | (g << 8) | r;
              hash ^= mColors[m];
          }
          
          if (prevHash != hash) {
              prevHash = hash;
              Bitmap bmp = Bitmap.createBitmap(mColors, mWidth,
                      mHeight, Bitmap.Config.RGB_565);
  
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, baos);
              try {
				baos.flush();
				byte[] imageData = baos.toByteArray();
	              SMLog.i(" DBG imgageData:size=" + imageData.length);
	              baos.close();
	              bmp.recycle();

	              mOs.write(("--" + boundary + "\r\n" +
	                      "Content-type: image/jpeg\r\n" +
	                      "Content-Length: " + imageData.length +
	                      "\r\n\r\n").getBytes());
	              mOs.write(imageData);
	              mOs.write(("\r\n").getBytes());
	              mOs.flush();
			} catch (IOException e) {
				SMLog.e("sendToOut catch exception:" + e);
				e.printStackTrace();
				mList.clear();
				try {
					interrupt();
					join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
              
          }
	}
	
	private LinkedList<EncodeItem> mList;
	//private FileRecord mFileRecord = new FileRecord("/data/data/video.mov");
	private int mIndex = 0;
    private final int IMAGE_QUALITY = 50;
    /**
     * value 0 ~ 30
     * used for control get frames in one second.
     */
    String boundary = "screenFrame";
    private Socket mSock;
    private int[] mColors;
    private int mWidth;
    private int mHeight;
    private int prevHash;
    private OutputStream mOs;
}
