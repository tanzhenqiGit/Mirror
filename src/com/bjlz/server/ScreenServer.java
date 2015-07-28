package com.bjlz.server;

import java.io.IOException;
import java.net.Socket;

import com.bjlz.handler.ErrorHandler;
import com.bjlz.handler.HomeHandler;
import com.bjlz.handler.MJPEGHandler;
import com.bjlz.handler.RawHandler;

import android.util.DisplayMetrics;
import android.util.Log;


public class ScreenServer extends HttpServer {

    private DisplayMetrics mDm;

    public ScreenServer(DisplayMetrics dm, int port) throws IOException {
        super(port);
        mDm = dm;
    }

    @Override
    public void handlePath(Socket sock, String path) {
        Log.i("SM", "HTTP req: " + path);
        if (path.equals("/")) {
            new HomeHandler(sock);
        } else if (path.equals("/frame.mjpeg")) {
            new MJPEGHandler(sock, mDm);
        } else if (path.equals("/raw")) {
            new RawHandler(sock, mDm);
        } else {
            new ErrorHandler(sock);
        }
    }
}
