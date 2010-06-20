package com.covedesign.devs;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import com.covedesign.devs.ClickableListAdapter.ViewHolder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends ListActivity {

    public static final String TAG = "dev";
    private Timer timer = new Timer();
    int backlightLightB = 0;
    private List<ListData> mObjectList = new ArrayList<ListData>();
    AlertDialog.Builder menuBuilder;
    int ON = 1;
    int OFF= 0;
    String[] buttonText = {"Trackball Light", "Button Light", "Green Light", "Amber light", "Blue Light", "Blink", "Background Light Blink"};
    String[] LED = {"jogball-backlight", "button-backlight", "green", "amber", "blue", "lcd-backlight"};
    String[] path = {"/sys/class/leds/", "/brightness", "/blink"};
    String[] command = {"echo 0 >", "echo 255 >", "echo 1 >", "echo 255 >", "chmod 0 ", "chmod 775 "};
    ProgressDialog myProgressDialog;

    static class ListData {

        public ListData(String t, int i) {
            text = t;
            id = i;
        }
        String text;
        boolean enable;
        int id;
    }

    static class mViewHolder extends ViewHolder {

        public mViewHolder(CheckBox t) {
            cBox = t;
        }
        CheckBox cBox;
    }

    private class mClickableListAdapter extends ClickableListAdapter {

        public mClickableListAdapter(Context context, int viewid,
                List<ListData> objects) {
            super(context, viewid, objects);
        }

        protected void bindHolder(ViewHolder h) {
            mViewHolder mvh = (mViewHolder) h;
            ListData mo = (ListData) mvh.data;
            mvh.cBox.setText(mo.text);
        }

        @Override
        protected ViewHolder createHolder(View v) {
            final CheckBox cBox = (CheckBox) v.findViewById(R.id.listitem_text);
            ViewHolder mvh = new mViewHolder(cBox);
            cBox.setOnClickListener(new ClickableListAdapter.OnClickListener(mvh) {

                public void onClick(View v, ViewHolder viewHolder) {
                    final mViewHolder mvh = (mViewHolder) viewHolder;
                    final ListData mo = (ListData) mvh.data;
                    myProgressDialog = ProgressDialog.show(Main.this, "Please wait...", "Loading...", true);
                    new Thread(new Runnable() {

                        public void run() {
                            if (cBox.isChecked()) {
                                switch (mo.id) {
                                    case (5):
                                        for (int i = 0; i < 4; i++) {
                                            setLED(2, i);
                                        }
                                        break;
                                    case (6):
                                        timer = new Timer();
                                        timer.scheduleAtFixedRate(new TimerTask() {

                                            public void run() {
                                                switch (backlightLightB) {
                                                    case 0:
                                                        setLED(ON, mo.id-1);
                                                        backlightLightB = 1;
                                                        break;
                                                    case 1:
                                                        setLED(OFF, mo.id-1);
                                                        backlightLightB = 0;
                                                        break;
                                                }
                                            }
                                        }, 0, 1000);
                                        break;
                                    default:
                                        setLED(ON, mo.id);
                                }
                            } else {
                                switch (mo.id) {
                                    case (5):
                                        for (int i = 0; i < 4; i++) {
                                            setLED(OFF, i);
                                        }
                                        break;
                                    case (6):
                                        if (timer != null) {
                                            timer.cancel();
                                            setLED(ON, mo.id-1);
                                        }
                                        break;
                                    default:
                                        setLED(OFF, mo.id);
                                }
                            }
                            myProgressDialog.dismiss();
                        }
                    }).start();
                }
            });

            return mvh;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        System.setErr(new PrintStream(new LogOutputStream("System.err")));
        System.setOut(new PrintStream(new LogOutputStream(TAG)));
        menuBuilder = new AlertDialog.Builder(this);
        for (int i = 0; i < buttonText.length; i++) {
            mObjectList.add(new ListData(buttonText[i], i));
        }

        setListAdapter(new mClickableListAdapter(this, R.layout.main, mObjectList));
        showMessage("This application is only for development use. \n\n\nSource on http://covedeign.se\n\n Use at own risk", "Welcome to Covedesign Development app");
    }

    private void showMessage(String msg, String titel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg).setCancelable(false).setTitle(titel).setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
        WindowManager.LayoutParams lp = alert.getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        alert.getWindow().setAttributes(lp);
        alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        alert.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    public void setLED(int mode, int led) {
        try {
            switch (mode) {
                case 0:
                    runCommand(command[5], path[0] + LED[led] + path[1]);
                    runCommand(command[0], path[0] + LED[led] + path[1]);
                    break;
                case 1:
                    runCommand(command[1], path[0] + LED[led] + path[1]);
                    runCommand(command[0], path[0] + LED[led] + path[2]);
                    runCommand(command[4], path[0] + LED[led] + path[1]);
                    break;
                case 2:
                    runCommand(command[2], path[0] + LED[led] + path[2]);
                    break;
                case 3:
                    runCommand(command[3], path[0] + LED[led] + path[1]);
                    break;
            }
        } catch (Exception ex) {
            showMessage("Error, Something went wrong, check if you have root privileges", "Error");
        }
    }

    public void runCommand(String command, String file) throws Exception {
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write((command + " " + file).getBytes());
            stdin.flush();

            stdin.close();
            BufferedReader brCleanUp =
                    new BufferedReader(new InputStreamReader(stdout));
            while ((line = brCleanUp.readLine()) != null) {
                System.out.println("[Devs output] " + line);
            }
            brCleanUp.close();
            brCleanUp =
                    new BufferedReader(new InputStreamReader(stderr));
            while ((line = brCleanUp.readLine()) != null) {
                System.out.println("[Devs error] " + line);
            }
            brCleanUp.close();

        } catch (IOException ex) {
        }
    }
}
