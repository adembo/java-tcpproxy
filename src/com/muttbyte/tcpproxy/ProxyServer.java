package com.muttbyte.tcpproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    public final static int NUM_THREADS = 3;
    public static final int DEFAULT_BACKLOG = 1000;

    public static void main(String[] argv) {
        int inPort = Integer.parseInt(argv[0]);
        String outAddr = argv[1];
        int outPort = Integer.parseInt(argv[2]);

        System.out.println("Proxy port " + inPort + " to addr " + outAddr + " port " + outPort);
        ServerSocket servSock = null;
        try {
            servSock = new ServerSocket(inPort, DEFAULT_BACKLOG);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            SingleProxy singleProxy = new SingleProxy(servSock, outAddr, outPort);
            singleProxy.run();
        }

        System.out.println("Done.");
    }

    public static class SingleProxy extends Thread {

        ServerSocket servSock;
        String outAddr;
        int outPort;

        public SingleProxy(ServerSocket servSock, String outAddr, int outPort) {
            this.servSock = servSock;
            this.outAddr = outAddr;
            this.outPort = outPort;
        }

        @Override
        public void run() {

            while (true) {

                try {

                    Socket serverSocket = servSock.accept();
                    InetAddress outInetAddr = InetAddress.getByName(outAddr);
                    Socket cliSock = new Socket(outInetAddr, outPort);

                    Object lock = new Object();
                    StreamCopyThread sToC = new StreamCopyThread(serverSocket, cliSock, lock);
                    StreamCopyThread cToS = new StreamCopyThread(cliSock, serverSocket, lock);
                    sToC.setPeer(cToS);
                    cToS.setPeer(sToC);
                    // synchronized (lock) {
                    // connections.addElement(cToS);
                    // connections.addElement(sToC);
                    sToC.start();
                    cToS.start();
                    // }

                } catch (Exception exc) {
                    System.out.println(exc.toString());
                    exc.printStackTrace();
                }
            }
        }
    }
}
