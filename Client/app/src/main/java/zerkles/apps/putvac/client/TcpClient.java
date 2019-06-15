package zerkles.apps.putvac.client;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


class TcpClient {
    String SERVER_IP = "0.0.0.0";
    int SERVER_PORT = 0;
    private Socket socket = new Socket();


    private DataOutputStream outputStream;

    /**
     * Sends the message entered by client to the server
     *
     * @param bytes byte sequence entered by client
     */
    void sendBytes(final byte[] bytes) {
        this.sendInt(bytes.length);
        if (outputStream != null) {
            Log.d("tcpClient_sendBytes", "Sending: bytes");
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
     * @param message text entered by client
     */
    void sendString(final String message) {
        this.sendBytes(message.getBytes());
    }

    /**
     * Sends number to server
     *
     * @param number value entered by client
     */
    private void sendInt(final int number) {
        byte[] data = new byte[]{
                (byte) ((number >> 24) & 0xff),
                (byte) ((number >> 16) & 0xff),
                (byte) ((number >> 8) & 0xff),
                (byte) (number & 0xff),
        };
        if (outputStream != null) {
            Log.d("tcpClient_sendInt", "Sending: integer");
            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void connect() {
        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);
            socket.setSendBufferSize(10000000);

            outputStream = new DataOutputStream(socket.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void disconnect() {
        HttpClient.sendRequest("GET", this.SERVER_IP, "/VAC/disconnect");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Socket getSocket() {
        return socket;
    }
}
