package com.muttbyte.tcpproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
  public static final int NUM_THREADS = 3;
  public static final int DEFAULT_BACKLOG = 1000;

  public static void main(String[] argv) throws IOException {
    if (argv.length != 3) {
      System.out.println("Usage: tcpproxy [in-port] [out addr] [out port]");
      System.exit(2);
    }
    int inPort = Integer.parseInt(argv[0]);
    String outAddr = argv[1];
    int outPort = Integer.parseInt(argv[2]);

    System.out.println("Proxy port " + inPort + " to addr " + outAddr + " port " + outPort);
    ServerSocket proxy = new ServerSocket(inPort, DEFAULT_BACKLOG);

    for (int i = 0; i < NUM_THREADS; i++) {
      SingleProxy singleProxy = new SingleProxy(proxy, outAddr, outPort);
      singleProxy.run();
    }

    System.out.println("Done.");
  }

  public static class SingleProxy extends Thread {

    ServerSocket proxy;
    String outAddr;
    int outPort;

    public SingleProxy(ServerSocket proxy, String outAddr, int outPort) {
      this.proxy = proxy;
      this.outAddr = outAddr;
      this.outPort = outPort;
    }

    @Override
    public void run() {
      while (true) {
        try {
          Socket toLocal = proxy.accept();
          InetAddress outInetAddr = InetAddress.getByName(outAddr);
          Socket toRemote = new Socket(outInetAddr, outPort);

          Object lock = new Object();
          StreamCopyThread lToR = new StreamCopyThread(toLocal, toRemote, lock);
          StreamCopyThread rToL = new StreamCopyThread(toRemote, toLocal, lock);
          lToR.setPeer(rToL);
          rToL.setPeer(lToR);
          lToR.start();
          rToL.start();
        } catch (Exception exc) {
          System.out.println(exc.toString());
          exc.printStackTrace();
        }
      }
    }
  }
}
