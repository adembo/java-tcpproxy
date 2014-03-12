package com.muttbyte.tcpproxy;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {

    public static void main(String[] argv) {
        int inPort = Integer.parseInt(argv[0]);
        String outAddr = argv[1];
        int outPort = Integer.parseInt(argv[2]);

        System.out.println("Proxy port " + inPort + " to addr " + outAddr + " port " + outPort);

        singleProxy.run(inPort, outAddr, outPort);

        System.out.println("Done.");
    }

    public static class singleProxy {

        public static void run(int inPort, String outAddr, int outPort) {

            try {

                InetAddress outInetAddr = InetAddress.getByName(outAddr);

                ServerSocket servSock = new ServerSocket(inPort);
                Socket cliSock = new Socket(outInetAddr, outPort);

                Socket serverSocket = servSock.accept();

                StreamCopyThread sToC = new StreamCopyThread(serverSocket, cliSock);
                StreamCopyThread cToS = new StreamCopyThread(cliSock, serverSocket);
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
            }

        }

    }
}
