package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class TcpServer {
    private ServerSocket serverSocket = null;

    TcpServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Socket accept() {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
