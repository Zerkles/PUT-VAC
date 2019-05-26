package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class TcpServer {
    private ServerSocket serverSocket = null;

    TcpServer(int tcpPort) {
        try {
            serverSocket = new ServerSocket(tcpPort);
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
