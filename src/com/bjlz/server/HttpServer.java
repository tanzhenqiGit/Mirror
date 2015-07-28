package com.bjlz.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.bjlz.util.SMLog;



public abstract class HttpServer extends Thread {

  

    public HttpServer(int port)
            throws IOException {
    	super("HttpServerThread");
        mServerSock = new ServerSocket(port, REQUEST_NUMBER);
        mReadBuffer = new byte[2048];
    }

    @Override
    public void run() {
        SMLog.i("SM", "HttpServer thread started");
        while (!isInterrupted()) {
            try {
                // read request
                Socket reqSock = mServerSock.accept();
                reqSock.setSendBufferSize(32 * 1024);
                InputStream is = reqSock.getInputStream();
                int size = 0;
                if ((size = is.read(mReadBuffer)) != -1) {
                    // parse
                    String req = new String(mReadBuffer, "UTF-8");
                    SMLog.i("SM" , "HttpServer size = " + size);
                    String[] lines = req.split("\\r?\\n");
                    printArray(lines);
                    if (lines.length > 0) {
                        String[] head = lines[0].split("\\s+");
                        if (head.length == 3 && head[0].equals("GET")) {
                            // handle GET
                            handlePath(reqSock, head[1]);
                        } else {
                            SMLog.i("SM", "Method unsupported or wrong head");
                        }
                    } else {
                        SMLog.i("SM", "Empty request");
                        reqSock.close();
                    }
                } else {
                    SMLog.i("SM", "Server read error size = " + size);
                }
            } catch (Exception e) {
                SMLog.e("SM", "Server error"+ e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    return;
                }
            }
        }
        try {
            mServerSock.close();
        } catch (IOException e) {
            SMLog.e("SM", "Error closing server socket" + e);
        }
        SMLog.i("SM", "HttpServer thread finished");
    }

    private void printArray(String array[]) 
    {
    	if(array != null) {
    		for (int index = 0; index < array.length; index++){
    			SMLog.i("SM", "req ->" + array[index]);
    		}
    	}
    }
    public abstract void handlePath(Socket sock, String path);
    private ServerSocket mServerSock;
    private byte[] mReadBuffer;
    private final int REQUEST_NUMBER = 5;
}
