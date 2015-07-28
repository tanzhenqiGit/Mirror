package com.bjlz.handler;

import java.io.OutputStream;
import java.net.Socket;


import android.util.Log;

public class HomeHandler extends HttpHandler {
    public HomeHandler(Socket sock) {
        super(sock);
    }

    private static final String HTML = "<div style=\"width:100%; text-align:center\"><img src=\"frame.mjpeg\" alt=\"screen\" /></div>";

    @Override
    public void run() {
        try {
            OutputStream os = getSock().getOutputStream();
            os.write(("HTTP/1.1 200 OK\r\n" +
                    "Date: " + getServerTime() + "\r\n" +
                    "Server: " + SERVER_NAME + "\r\n" +
                    "Content-Type: text/html;charset=utf-8\r\n" +
                    "Connection: close\r\n\r\n" + HTML).getBytes());
            getSock().close();
        } catch (Exception e) {
            Log.e("SM", "Home page handler error", e);
        }
    }
}
