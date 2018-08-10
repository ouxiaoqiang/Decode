package com.jmgo.decode;

/**
 * Created by ouxiaoqiang on 2018/7/24.
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CasearOutputStream  extends FileOutputStream {
    private byte[] keyBytes;
    private int currentKeyIndex;
    private boolean isDoCaesar;

    public CasearOutputStream(String name, byte[] keyBytes, boolean isDoCaesar) throws FileNotFoundException {
        super(name);
        this.keyBytes = keyBytes.clone();
        currentKeyIndex = 0;
        this.isDoCaesar = isDoCaesar;
    }

    public void write(int b) throws IOException {
        byte c;
        if (isDoCaesar == true) {
            c = (byte) (b + keyBytes[currentKeyIndex]);
        } else {
            c = (byte) (b - keyBytes[currentKeyIndex]);
        }

        currentKeyIndex = (currentKeyIndex + 1) % keyBytes.length;

        super.write(c);
    }

    public void write(byte[] bytes) throws IOException {
        if (isDoCaesar == true) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (bytes[i] + keyBytes[currentKeyIndex]);
                currentKeyIndex = (currentKeyIndex + 1) % keyBytes.length;
            }
        } else {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (bytes[i] - keyBytes[currentKeyIndex]);
                currentKeyIndex = (currentKeyIndex + 1) % keyBytes.length;
            }
        }

        super.write(bytes);
    }
}