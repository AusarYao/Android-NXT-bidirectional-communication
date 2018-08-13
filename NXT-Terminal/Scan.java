import lejos.nxt.SensorPort;
import lejos.nxt.*;
import lejos.robotics.subsumption.*;
import lejos.util.Delay;
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

public class Scan {
    private int MAX_DETECT = 75; // Maximum detect distance for ultrasonic sensor(if you need to
    // change this, don't forget the Android side as well)
    private UltrasonicSensor mScanner;
    private int scanTimes = 21; // The times of scan the NXT will perform per auto scan
    private DataOutputStream mOutputStream;
    private DataInputStream mInputStream;
    private int scannedtime = 0;
    private static final int RECEIVE_KEY = 10;//Receive key, if android receives this number,
                                              // it starts to read the rest of the data

    public Scan(SensorPort sensorU) {
        mScanner = new UltrasonicSensor(sensorU);
        Motor.C.setSpeed(140);

    }

    private boolean singleScan(boolean notFrist) {
        try {
            int data;
            if (notFrist) {
                Motor.C.rotate(9);
                Delay.msDelay(200);
            }
            data = mScanner.getDistance();

            if (data < 0 && data == 255){
                data = 0;

            }

            if (data > 75) {
                data = 75;

            }
            mOutputStream.write(RECEIVE_KEY);
            mOutputStream.write(data);
            mOutputStream.flush();
            LCD.drawString("DONE", 0, 3);
            Delay.msDelay(100);

            return true;
        } catch (IOException ioe)
        {
            LCD.clear();
            LCD.drawString("Error occurred", 0, 0);
            return false;
        }
    }

    public boolean autoScan() {

        if (singleScan(false)) {
            for (scannedtime = 0; scannedtime < 20; scannedtime++) {
                if (!singleScan(true)) {
                    int tmpAngle = - (scannedtime + 1) * 9;
                    Motor.C.rotate(tmpAngle);
                    return false;
                }
            }
            Motor.C.rotate(-180);
            return true;
        }else{
            int tmpAngle = - (scannedtime + 1) * 9;
            Motor.C.rotate(tmpAngle);
            return false;
        }

    }

    public void setIOStream(DataInputStream inputStream, DataOutputStream outputStream) {
        mOutputStream = outputStream;
        mInputStream = inputStream;

    }

}