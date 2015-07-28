package com.bjlz.activity;

import java.io.IOException;
import java.io.InputStream;

import com.bjlz.receiver.RootStatusReceiver;
import com.bjlz.service.MirrorService;
import com.bjlz.util.SMLog;

import ch.cern.android.priv.sm.R;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mStatusChangeReceiver);
      	if (mCatchProcess != null) {
    		mCatchProcess.destroy();
    		mCatchProcess = null;
    	}
      	mLogThread.interrupt();
      	mLogThread = null;
    }
    
    
    @Override
	protected void onResume() {
    	SMLog.d("onResume");
		super.onResume();
	}

	private void handleOnLogAcquire()
    {
    	String ShellCmd = "logcat -v time -s SM";
    	SMLog.d("LOG handleOnLogAcquire");
       	startProcess(ShellCmd,CMD_TYPE_CATCH);
    	mInputStream = mCatchProcess.getInputStream();
    	int readLength = 2048;
    	int read = 0;
        try {
			while (!mLogThread.isInterrupted() && 
					(read = mInputStream.read(mBuffer, 0, readLength)) > 0)  {
				//Thread.sleep(500);
				sendMsg(mBuffer, read);
			}
			SMLog.d("DBG end read:" + read);
		} catch (IOException e) {
			SMLog.e("LOG handleOnLogAcquire read catch exception:" + e);
			e.printStackTrace();
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		}
           
    }
    
    private void startProcess(String command, int type)
    {
    	if (type == CMD_TYPE_CATCH) {
	    	if (mCatchProcess != null) {
	    		mCatchProcess.destroy();
	    		mCatchProcess = null;
	    	}
	        try {
	        	SMLog.d("DBG command" + command);
				mCatchProcess = Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}  else if (type == CMD_TYPE_CLEAN) {
    		try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	} else {
    		
    	}
           
    }
    
    private void sendMsg(byte[] value, int length)
    {
    	if (value == null || mHandler == null) {
    		return;
    	}
    	if (length > value.length) {
    		length = value.length;
    	}
    	String content = new String(value, 0, length);
    	Message msg = mHandler.obtainMessage();
    	msg.what = UPDATE_CONTENT;
    	msg.obj = content;
    	mHandler.sendMessage(msg);
    	
    }
    
    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler()
    {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_CONTENT:
				String str = (String)msg.obj;
				if (mContent != null) {
					mContent.append(str);
				}
				break;
			default:
				break;
			}
		}
    	
    };
    
    
    private void initialize()
    {
    	mClean = (Button) findViewById(R.id.main_clean);
    	mClean.setOnClickListener(mListener);
    	mCatch = (Button) findViewById(R.id.main_catch);
    	mCatch.setOnClickListener(mListener);
        mTextView = (TextView) findViewById(R.id.main_status_txt);
        mContent = (TextView) findViewById(R.id.main_log_txt);
        mStatusChangeReceiver = new RootStatusReceiver(mTextView);
        registerReceiver(mStatusChangeReceiver, new IntentFilter(RootStatusReceiver.ACTION));
        // start service
        Intent i = new Intent(MainActivity.this, MirrorService.class);
        i.putExtra("port", PORT);
        if (startService(i) != null) {
        	mTextView.setText("Service already running");
        }
        mLogThread = new Thread()
        {
        	@Override
        	public void run() {
        		handleOnLogAcquire();
        	};
        };
    }
    
    private View.OnClickListener mListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.main_clean:
		       	startProcess("logcat -c;",CMD_TYPE_CATCH);
		       	mContent.setText("");
				break;
			case R.id.main_catch:
				mCatch.setVisibility(View.INVISIBLE);
				if (mLogThread != null) {
					mLogThread.start();
				}
				break;
			default:
				
				break;
			}
		}
	};
    
	
	/**
	 * port for access 
	 */
    private static final int PORT = 6100;
    private final int CMD_TYPE_CLEAN = 0;
    private final int CMD_TYPE_CATCH = 1;

    private BroadcastReceiver mStatusChangeReceiver;
    private TextView mTextView;
    private Button mClean, mCatch;
    private final int UPDATE_CONTENT = 0x01;
    private Thread mLogThread;
    private TextView mContent;
    private Process mCatchProcess = null;
    private InputStream mInputStream;
    private byte [] mBuffer = new byte[2048];
    

}
