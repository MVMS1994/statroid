package subbiah.veera.statroid.core;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import subbiah.veera.statroid.data.Logger;

/**
 * Created by Veera.Subbiah on 16/10/17.
 */

public class WebServer implements Runnable {

    private static final String TAG = "WebServer";

    private final int port;
    private final Context context;
    private boolean isRunning;

    private ServerSocket serverSocket;

    public WebServer(int port, Context context) {
        this.port = port;
        this.context = context;
    }

    public void start() {
        isRunning = true;
        new Thread(this).start();
    }

    public void stop() {
        try {
            isRunning = false;
            if (null != serverSocket) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            Logger.e(TAG, "Error closing the server socket.", e);
        }
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(SystemUtils.getIpAddr(context)));
            while (isRunning) {
                Socket socket = serverSocket.accept();
                handle(socket);
                socket.close();
            }
        } catch (SocketException ignore) {} catch (IOException e) {
            Logger.e(TAG, "Web server error.", e);
        }
    }

    private void handle(Socket socket) throws IOException {
        BufferedReader reader = null;
        PrintStream output = null;
        try {
            String route = null;

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while (!TextUtils.isEmpty(line = reader.readLine())) {
                if (line.startsWith("GET /")) {
                    int start = line.indexOf('/') + 1;
                    int end = line.indexOf(' ', start);
                    route = line.substring(start, end);
                    break;
                }
            }

            output = new PrintStream(socket.getOutputStream());

            if (null == route) {
                writeServerError(output);
                return;
            }
            byte[] bytes = loadContent(route);
            if (null == bytes) {
                writeServerError(output);
                return;
            }

            // Send out the content.
            output.println("HTTP/1.0 200 OK");
            output.println("Content-Type: " + detectMimeType(route));
            output.println("Content-Length: " + bytes.length);
            output.println();
            output.write(bytes);
            output.flush();
        } finally {
            if (null != output) {
                output.close();
            }
            if (null != reader) {
                reader.close();
            }
        }
    }

    private void writeServerError(PrintStream output) {
        output.println("HTTP/1.0 500 Internal Server Error");
        output.flush();
    }

    private byte[] loadContent(String route) throws IOException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String input = "Hey buddy";
            byte[] buffer = new byte[1024];
            int size;
            // while (-1 != (size = input.read(buffer))) {
                output.write(input.getBytes(), 0, input.getBytes().length);
            // }
            output.flush();
            return output.toByteArray();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private String detectMimeType(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        } else if (fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else {
            return "application/octet-stream";
        }
    }
}
