package com.teragrep.rlp_03;

import java.nio.channels.SocketChannel;

public class TransportInfo {

    private final SocketChannel socketChannel;

    TransportInfo(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String getLocalAddress() {
        return socketChannel.socket().getLocalAddress().toString();
    }

    public int getLocalPort() {
        return socketChannel.socket().getLocalPort();
    }

    public String getPeerAddress() {
        return socketChannel.socket().getInetAddress().toString();
    }

    public int getPeerPort() {
        return socketChannel.socket().getPort();
    }
}
