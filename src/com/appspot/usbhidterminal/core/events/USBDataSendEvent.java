package com.appspot.usbhidterminal.core.events;

public class USBDataSendEvent {
    private String data;
    private byte[] bytes;

    public USBDataSendEvent(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }


    public USBDataSendEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

}