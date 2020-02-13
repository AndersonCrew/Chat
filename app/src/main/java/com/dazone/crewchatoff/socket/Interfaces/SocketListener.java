package com.dazone.crewchatoff.socket.Interfaces;

public interface SocketListener {
    void onConnect();
    void onMessage(String message);
    void onMessage(byte[] data);
    void onDisconnect(int code, String reason);
    void onError(Exception error);
}