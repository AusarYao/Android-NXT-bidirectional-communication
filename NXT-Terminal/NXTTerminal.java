

import java.io.DataInputStream;
import java.io.DataOutputStream;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;
import lejos.util.Delay;
import java.io.*;
/*
* This file contains the main class,
 */

public class NXTTerminal {

    public static void main(String[] var0) {

        DataOutputStream mOutputStream;
        DataInputStream mInputStream;
        NXTConnection bluetoothConnection;
        Scan mScan = new Scan(SensorPort.S4);
        byte[] inputData = new byte[2];
        int[] outputData;

        LCD.drawString("Not connected", 0, 0);
        bluetoothConnection = Bluetooth.waitForConnection();
        LCD.clear();
        LCD.drawString("Connected", 0, 0);
        bluetoothConnection.setIOMode(2);
        mOutputStream = bluetoothConnection.openDataOutputStream();
        mInputStream = bluetoothConnection.openDataInputStream();
        mScan.setIOStream(mInputStream, mOutputStream);

        while(!Button.ESCAPE.isDown()) {
            try {
                LCD.drawString("Connected", 0, 0);
                mInputStream.read(inputData);
                if (inputData[0] == 3) {
                    if (inputData[1] == 1) {
                        LCD.drawString("Auto scan", 0, 1);
                        mOutputStream.flush(); //flush to make sure there is no unexpected data
                        if (!mScan.autoScan()){
                            break;
                        }
                        inputData[0] = 0;
                    }

                    LCD.clear();
                    LCD.drawString("Scan complete", 0, 1);
                    Delay.msDelay(500L);
                    LCD.clear();
                }
            } catch (IOException ioe) {
                LCD.clear();
                LCD.drawString("Error occurred", 0, 0);
                break;
            }
        }

        LCD.drawString("Wait...", 2, 2);
        Button.waitForAnyPress();
    }



}
