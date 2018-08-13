package com.example.auser.nxtcontroller;

/*
 *  Modified by Yuchen Yao and Zhiqin Yao
 *  The name of the original project is NxtRemoteControl. Its licence is below
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

/*
 * This file is derived from:
 *
 * http://developer.android.com/resources/samples/BluetoothChat/src/com/example/android/BluetoothChat/BluetoothChat.html
 *
 * Copyright (c) 2009 The Android Open Source Project
 */

//package org.jfedor.nxtremotecontrol;

/*
 * TODO:
 *
 * tilt controls
 * data manager class
 * class manager class
 * movement control for NXT
 * store and edit drawn map
 * add manual control mode
 */


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import static com.example.auser.nxtcontroller.R.id.map_component;

public class MainController extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {


    private int mSavedState = NXTConnector.STATE_NONE;
    private int mPower = 80;
    private int mActiveMode = MAIN_CONTROL;
    private int mState = NXTConnector.STATE_NONE;
    private int[] mBitmapDimension = new int[2];


    private List<List<PointF>> mAutoMapCoordinateSet = new LinkedList<>();

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_CONNECT_DEVICE = 2;
    private final int REQUEST_SETTINGS = 3;

    private static final int MAIN_CONTROL = 1;
    private static final int AUTO_MAPPING = 2;

    private boolean mNewLaunch = true;
    private boolean NO_BT = false;

    private Bitmap mAutoMapBitmap;

    private String mDeviceAddress = null;

    private TextView mStateDisplay;
    private Button mConnectButton;
    private Button mDisconnectButton;
    private Button mAutoScanButton;
    private Button mManualScanButton;
    private Menu mMenu;
    private ListView mAutoMapListView;
    private ActionBar mActionBar;


    private BluetoothAdapter mBluetoothAdapter;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private NXTConnector mNXTConnector;
    private AutoMappingView mAutoMappingView;
    private DynamicListView mDynamicListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        if (savedInstanceState != null) {
            mNewLaunch = false;
            mDeviceAddress = savedInstanceState.getString("device_address");
            if (mDeviceAddress != null) {
                mSavedState = mNXTConnector.STATE_CONNECTED;
            }

            if (savedInstanceState.containsKey("power")) {
                mPower = savedInstanceState.getInt("power");
            }
        }

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "NXT Controller");

        if (!NO_BT) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        mActionBar = getActionBar();
        mActionBar.setHomeButtonEnabled(true);


        setupUI();


        mNXTConnector = new NXTConnector(mHandler);

    }

    @SuppressLint("WrongViewCast")
    public void setupUI() {

        if (mActiveMode == MAIN_CONTROL) {

            setContentView(R.layout.main_controller);

            updateMenu(R.id.menuitem_main_control);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            if (findViewById(R.id.power_seekbar) != null) {

                SeekBar powerSeekBar = (SeekBar) findViewById(R.id.power_seekbar);
                powerSeekBar.setProgress(mPower);
                powerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        mPower = progress;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }

            connectionViewElements();

            if (findViewById(R.id.auto_scan_button) != null) {

                mAutoScanButton = (Button) findViewById(R.id.auto_scan_button);
                mAutoScanButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!(mState == NXTConnector.STATE_NONE || mState == NXTConnector.STATE_CONNECTING)) {
                            mNXTConnector.startScan(NXTConnector.autoScanKey);
                            mActiveMode = AUTO_MAPPING;
                            setupUI();
                        }

                    }

                });

            }

            if (findViewById(R.id.manual_scan_button) != null) {
                mManualScanButton = (Button) findViewById(R.id.manual_scan_button);

            }


        } else if (mActiveMode == AUTO_MAPPING) {
            setContentView(R.layout.auto_mapping);
            mAutoMappingView = (AutoMappingView) findViewById(map_component);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            updateMenu(R.id.menuitem_raw_data);

            ViewTreeObserver viewTreeObserver = mAutoMappingView.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mBitmapDimension = getBitmapDimension(mAutoMappingView);
                        if (mBitmapDimension[0] != 0) {

                            mAutoMappingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }

            mAutoMapListView = (ListView) findViewById(R.id.data_list);
            mDynamicListView = new DynamicListView();
            mDynamicListView.initialize(mAutoMapListView, this);


            connectionViewElements();
            //      mSensorRawData = (SensorRawData) findViewById(R.id.raw_data_view);
        }

    }

    private void updateMenu(int disabled) {
        if (mMenu != null) {
            mMenu.findItem(R.id.menuitem_main_control).setEnabled(disabled != R.id.menuitem_main_control).setVisible(disabled != R.id.menuitem_main_control);
            mMenu.findItem(R.id.menuitem_raw_data).setEnabled(disabled != R.id.menuitem_raw_data).setVisible(disabled != R.id.menuitem_raw_data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.i("NXT", "NXTRemoteControl.onStart()");
        if (!NO_BT) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                if (mSavedState == mNXTConnector.STATE_CONNECTED) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mNXTConnector.connect(device);
                } else {
                    if (mNewLaunch) {
                        mNewLaunch = false;
                        findBrick();
                    }
                }
            }
        }
    }

    private void findBrick() {
        Intent intent = new Intent(this, ChooseDeviceActivity.class);
        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    findBrick();
                } else {
                    Toast.makeText(this, "Bluetooth not enabled, exiting.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras().getString(
                            ChooseDeviceActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    //Toast.makeText(this, address, Toast.LENGTH_LONG).show();
                    mDeviceAddress = address;
                    mNXTConnector.connect(device);
                }
                break;
            case REQUEST_SETTINGS:
                //XXX?
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mState == mNXTConnector.STATE_CONNECTED) {
            outState.putString("device_address", mDeviceAddress);
        }
        outState.putInt("power", mPower);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Log.i("NXT", "NXTRemoteControl.onConfigurationChanged()");
        setupUI();
    }

    private void displayState() {
        String stateText = null;
        int color = 0;
        switch (mState) {
            case NXTConnector.STATE_NONE:
                stateText = "Not connected";
                color = 0xffff0000;
                mConnectButton.setVisibility(View.VISIBLE);
                mDisconnectButton.setVisibility(View.GONE);
                setProgressBarIndeterminateVisibility(false);
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
                break;
            case NXTConnector.STATE_CONNECTING:
                stateText = "Connecting...";
                color = 0xffffff00;
                mConnectButton.setVisibility(View.GONE);
                mDisconnectButton.setVisibility(View.GONE);
                setProgressBarIndeterminateVisibility(true);
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire();
                }
                break;
            case NXTConnector.STATE_CONNECTED:
                stateText = "Connected";
                color = 0xff00ff00;
                mConnectButton.setVisibility(View.GONE);
                mDisconnectButton.setVisibility(View.VISIBLE);
                setProgressBarIndeterminateVisibility(false);
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire();
                }
                break;
        }
        mStateDisplay.setText(stateText);
        mStateDisplay.setTextColor(color);

    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NXTConnector.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(NXTConnector.TOAST), Toast.LENGTH_LONG).show();
                    break;
                case NXTConnector.MESSAGE_STATE_CHANGE:
                    mState = msg.arg1;
                    displayState();
                    break;
                case NXTConnector.MESSAGE_DATA_FROM_NXT:
                    int tmp = msg.getData().getInt(NXTConnector.NXTData);

                    if (mAutoMapCoordinateSet.size() == 0) {
                        mAutoMapBitmap = Bitmap.createBitmap(mBitmapDimension[0],
                                mBitmapDimension[1], Bitmap.Config.ARGB_8888);

                    }

                    mAutoMappingView.updateTrackData(tmp);
                    mDynamicListView.updataData(tmp);

                    //     drawOnBitmap(mAutoMappingView, mAutoMapBitmap);

                    break;
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        //Log.i("NXT", "NXTRemoteControl.onStop()");
        mSavedState = mState;
        mNXTConnector.stop();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_main_control:
                mActiveMode = MAIN_CONTROL;
                setupUI();
                break;
            case R.id.menuitem_raw_data:
                mActiveMode = AUTO_MAPPING;
                setupUI();
                break;
            case android.R.id.home:
                mActiveMode = MAIN_CONTROL;
                setupUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        readPreferences(sharedPreferences, key);
    }

    private void readPreferences(SharedPreferences prefs, String key) {
     /*   if (key == null) {
            mReverse = prefs.getBoolean("PREF_SWAP_FWDREV", false);
            mReverseLR = prefs.getBoolean("PREF_SWAP_LEFTRIGHT", false);
            mRegulateSpeed = prefs.getBoolean("PREF_REG_SPEED", false);
            mSynchronizeMotors = prefs.getBoolean("PREF_REG_SYNC", false);
            if (!mRegulateSpeed) {
                mSynchronizeMotors = false;
            }
        } else if (key.equals("PREF_SWAP_FWDREV")) {
            mReverse = prefs.getBoolean("PREF_SWAP_FWDREV", false);
        } else if (key.equals("PREF_SWAP_LEFTRIGHT")) {
            mReverseLR = prefs.getBoolean("PREF_SWAP_LEFTRIGHT", false);
        } else if (key.equals("PREF_REG_SPEED")) {
            mRegulateSpeed = prefs.getBoolean("PREF_REG_SPEED", false);
            if (!mRegulateSpeed) {
                mSynchronizeMotors = false;
            }
        } else if (key.equals("PREF_REG_SYNC")) {
            mSynchronizeMotors = prefs.getBoolean("PREF_REG_SYNC", false);
        }*/
    }
/*
    public class MapDraw extends View {
        public MapDraw(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public MapDraw(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MapDraw(Context context) {
            super(context);
        }

        private Paint mmPaint = new Paint();

        @Override
        public void onDraw(Canvas canvas) {
            if (mNewDataWhere) {
                int mmTmpAngle = mDataCount * 9, mmTmpDistance = mRawDataSet[mTmpDataCount];
         //       drawLineMap(mmTmpAngle, mmTmpDistance, canvas, true);
                mNewDataWhere = false;
            }
        }



    }*/


    private void connectionViewElements() {

        if (findViewById(R.id.state_display) != null) {

            mStateDisplay = (TextView) findViewById(R.id.state_display);

        }

        if (findViewById(R.id.connect_button) != null) {

            mConnectButton = (Button) findViewById(R.id.connect_button);
            mConnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NO_BT) {
                        findBrick();
                    } else {
                        mState = NXTConnector.STATE_CONNECTED;
                        displayState();
                    }
                }
            });
        }

        if (findViewById(R.id.disconnect_button) != null) {

            mDisconnectButton = (Button) findViewById(R.id.disconnect_button);
            mDisconnectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNXTConnector.stop();
                }
            });

        }

        displayState();
    }

    private int[] getBitmapDimension(View view) {
        int[] tmpIntArray = new int[2];
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        tmpIntArray[0] = view.getWidth();
        tmpIntArray[1] = view.getHeight();

        return tmpIntArray;
    }


}
