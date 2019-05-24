package zerkles.apps.putvac.client;

import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;


class TcpClient {
    String SERVER_IP = "0.0.0.0";
    int SERVER_PORT = 0;
    private Socket socket = new Socket();

    private DataOutputStream outputStream;

    /**
     * Constructor of the class
     */
    TcpClient() {
    }


    /**
     * Sends the message entered by client to the server
     *
     * @param bytes byte sequence entered by client
     */
    void sendBytes(final byte[] bytes) {
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
     * Sends the message entered by client to the server
     *
     * @param number value entered by client
     */
    void sendNumber(final int number) {
        this.sendBytes(String.valueOf(number).getBytes());
    }

    /**
     * Close the connection and release the members
     */

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

    void checkConnStatus(InetSocketAddress addr) {
        if (addr != null && socket.isConnected()) {
            boolean reachable = false;
            try {
                // Dla większego TTL trzebaby zwiększyć
                reachable = socket.getInetAddress().isReachable(10);
                Log.d("tcpClient_checkConn", "Connected");
            } catch (IOException ignored) {
                Log.d("tcpClient_checkConn", "Disconnected");
                ignored.printStackTrace();
            } finally {
                if (!reachable) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }
}
