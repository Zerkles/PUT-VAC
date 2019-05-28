package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;


class TcpClient {
    private Socket socket;

    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    /**
     * Constructor of the class
     */
    public TcpClient() {
        socket = new Socket();
    }

    /**
     * Constructor of the class
     *
     * @param clientSocket Socket of the client
     */
    TcpClient(Socket clientSocket) {
        socket = clientSocket;

        if (socket.isConnected()) {
            try {
                outputStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param bytes Byte sequence entered by client
     */
    private void sendBytes(final byte[] bytes) {
        if (outputStream != null) {
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
     * @param message Text entered by client
     */
    public void sendString(final String message) {
        this.sendBytes(message.getBytes());
    }

    int receiveInt() throws IOException {
        if (inputStream != null) {
            return inputStream.readInt();
        }
        return Integer.MAX_VALUE;
    }

    byte[] receiveBytes(int len) throws IOException {
        if (inputStream != null) {
            byte[] result = new byte[len];
            final int unused = inputStream.read(result, 0, len);
            return result;
        }
        return null;
    }

    String receiveString(int len) throws IOException {
        return new String(receiveBytes(len), UTF8_CHARSET);
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param number Integer entered by client
     */
    void sendInt(final int number) {
        this.sendBytes(new byte[]{
                (byte) ((number >> 24) & 0xff),
                (byte) ((number >> 16) & 0xff),
                (byte) ((number >> 8) & 0xff),
                (byte) (number & 0xff)
        });
    }

    /**
     * Connects with given address
     *
     * @param serverIp   IP address of server
     * @param serverPort Port on which to connect
     */
    public void connect(String serverIp, int serverPort) {
        try {
            InetAddress serverAddr = InetAddress.getByName(serverIp);

            socket = new Socket(serverAddr, serverPort);
            socket.setSendBufferSize(1048576);

            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the connection with server
     */
    void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

