package com.example.auser.nxtcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

/*
 *
 * Modified by Yuchen Yao and Zhiqin Yao
 *
 */

/*
 * Copyright (c) 2010 Jacek Fedorynski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class NXTConnector {

    private final int SCANNING = 0;
    private int finalData;

    private final String TAG = "Connector";

    private int mState;
    private Handler mHandler;
    private BluetoothAdapter mAdapter;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;


    public static final byte[] autoScanKey = {0x03, 0x01};
    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final int MESSAGE_DATA_FROM_NXT = 3;
    public static final String NXTData = "NXTData";
    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final String TOAST = "toast";


    public NXTConnector(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        setState(STATE_NONE);
    }

    private synchronized void setState(int state) {
        mState = state;
        if (mHandler != null) {
            mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        } else {
            //XXX
        }
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void setHandler(Handler handler) {
        mHandler = handler;
    }

    private void toast(String text) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(TOAST, text);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            //XXX
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        //Log.i("NXT", "NXTTalker.connect()");

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //toast("Connected to " + device.getName());

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    private void connectionFailed() {
        setState(STATE_NONE);
        //toast("Connection failed");
    }

    private void connectionLost() {
        setState(STATE_NONE);
        //toast("Connection lost");
    }

    private class ConnectThread extends Thread {
        public BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            setName("ConnectThread");
            mAdapter.cancelDiscovery();

            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                    // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                    Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) method.invoke(mmDevice, Integer.valueOf(1));
                    mmSocket.connect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    connectionFailed();
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return;
                }
            }

            synchronized (NXTConnector.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }
        r.write(out);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            //   int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    mmInStream.read(buffer); // mminstream.read() returns int
                    int num = buffer[0];
                    if (num == 10) {
                        finalData = buffer[1];
                        sendNXTIntToHandler(mHandler, MESSAGE_DATA_FROM_NXT, NXTData, finalData);


                    }
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }




    public void startScan(byte[] scanType) {
        byte[] tmp = scanType;
        write(tmp);

    }

    private void sendNXTIntToHandler(Handler target, int receiverCase, String tag, int data) {
        Message tmpMsg = target.obtainMessage(receiverCase);
        Bundle bundle = new Bundle();
        bundle.putInt(tag, data);
        tmpMsg.setData(bundle);
        target.sendMessage(tmpMsg);

    }

}
