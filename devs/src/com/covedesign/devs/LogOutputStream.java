package com.covedesign.devs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.util.Log;

public class LogOutputStream extends OutputStream {

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private String name;

    public LogOutputStream(String name) {
        this.name = name;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == (int) '\n') {
            String s = new String(this.bos.toByteArray());
            Log.v(this.name, s);
            this.bos = new ByteArrayOutputStream();
        } else {
            this.bos.write(b);
        }
    }
}
