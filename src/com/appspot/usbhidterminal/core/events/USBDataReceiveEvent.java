package com.appspot.usbhidterminal.core.events;

public class USBDataReceiveEvent {
    private String data;
    private int bytesCount;
    private byte[] bytes;


    public USBDataReceiveEvent(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public USBDataReceiveEvent(String data, int bytesCount) {
        this.data = data;
        this.bytesCount = bytesCount;
    }

    public String getData() {
        return data;
    }

    public int getBytesCount() {
        return bytesCount;
    }
}