package zerkles.apps.putvac.client;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class TcpClient {
    private static final String TAG = TcpClient.class.getSimpleName();
    String SERVER_IP = "0.0.0.0"; //server IP address
    int SERVER_PORT = 0;
    private boolean isConnected = false;
    Socket socket = new Socket();

    private DataOutputStream outputStream;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    TcpClient() { }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendString(final String message) {
        if (outputStream != null) {
            Log.d(TAG, "Sending: " + message);
            try {
                outputStream.write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param bytes byte sequence entered by client
     */
    public void sendBytes(final byte[] bytes) {
        if (outputStream != null) {
            Log.d(TAG, "Sending: bytes");
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param number value entered by client
     */
    public void sendNumber(final int number) {
        if (outputStream != null) {
            Log.d(TAG, "Sending: bytes");
            try {
                outputStream.write(String.valueOf(number).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the connection and release the members
     */
    void stopClient() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void connect() {
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);
            socket.setSendBufferSize(10000000);

            isConnected = socket.isConnected();
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }
}
