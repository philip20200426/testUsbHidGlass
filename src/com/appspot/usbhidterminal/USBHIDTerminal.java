package com.appspot.usbhidterminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceControl;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.appspot.usbhidterminal.core.Consts;
import com.appspot.usbhidterminal.core.events.DeviceAttachedEvent;
import com.appspot.usbhidterminal.core.events.DeviceDetachedEvent;
import com.appspot.usbhidterminal.core.events.LogMessageEvent;
import com.appspot.usbhidterminal.core.events.PrepareDevicesListEvent;
import com.appspot.usbhidterminal.core.events.SelectDeviceEvent;
import com.appspot.usbhidterminal.core.events.ShowDevicesListEvent;
import com.appspot.usbhidterminal.core.events.USBDataReceiveEvent;
import com.appspot.usbhidterminal.core.events.USBDataSendEvent;
import com.appspot.usbhidterminal.core.services.SocketService;
import com.appspot.usbhidterminal.core.services.USBHIDService;
import com.appspot.usbhidterminal.core.services.WebServerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class USBHIDTerminal extends Activity implements View.OnClickListener {

    private SharedPreferences sharedPreferences;

    private Intent usbService;

    private Button btnSelectHIDDevice;
    private Button btnHandShake;
    private Button btnClear;

    private Button btnStartImu;
    private Button btnStopImu;
    private Button btn2D;
    private Button btn3D;


    private Button btnAcc;
    private Button btnGyr;
    private Button btnMag;
    private Button btnPSensor;
    private Button btnASensor;

    private Button btnOled;
    private EditText edtOled;

    private EditText etBright;
    private EditText etOled;
    private EditText etPSensor;
    private EditText etTEMP;
    private EditText etCustom;

    private EditText etGyr;
    private EditText etGyrX;
    private EditText etGyrY;
    private EditText etGyrZ;

    private EditText etAcc;
    private EditText etAccX;
    private EditText etAccY;
    private EditText etAccZ;

    private EditText etMag;
    private EditText etMagX;
    private EditText etMagY;
    private EditText etMagZ;
    private EditText etMagR;

    private EditText etGTime;
    private EditText etATime;
    private EditText etMTime;
    private EditText etDate;

    private EditText edtlogText;


    private String settingsDelimiter;
    private String receiveDataFormat;
    private String delimiter;

    protected EventBus eventBus;

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if ("enable_socket_server".equals(key) || "socket_server_port".equals(key)) {
                socketServiceIsStart(false);
                socketServiceIsStart(sharedPreferences.getBoolean("enable_socket_server", false));
            } else if ("enable_web_server".equals(key) || "web_server_port".equals(key)) {
                webServerServiceIsStart(false);
                webServerServiceIsStart(sharedPreferences.getBoolean("enable_web_server", false));
            }
        }
    };

    private void prepareServices() {
        usbService = new Intent(this, USBHIDService.class);
        startService(usbService);
        webServerServiceIsStart(sharedPreferences.getBoolean("enable_web_server", false));
        socketServiceIsStart(sharedPreferences.getBoolean("enable_socket_server", false));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        } catch (EventBusException e) {
            eventBus = EventBus.getDefault();
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        initUI();
//        SurfaceControl.setStereoMode(true);
    }

    private void initUI() {
        setVersionToTitle();
        btnSelectHIDDevice = (Button) findViewById(R.id.btnSelectHIDDevice);
        btnSelectHIDDevice.setOnClickListener(this);
        btnHandShake = (Button) findViewById(R.id.btnHandShake);
        btnHandShake.setOnClickListener(this);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);
        btnStartImu = (Button) findViewById(R.id.btnStartImu);
        btnStartImu.setOnClickListener(this);
        btnStopImu = (Button) findViewById(R.id.btnStopImu);
        btnStopImu.setOnClickListener(this);
        btn2D = (Button) findViewById(R.id.btn2D);
        btn2D.setOnClickListener(this);
        btn3D = (Button) findViewById(R.id.btn3D);
        btn3D.setOnClickListener(this);

        btnAcc = (Button) findViewById(R.id.btnAcc);
        btnAcc.setOnClickListener(this);
        btnGyr = (Button) findViewById(R.id.btnGyr);
        btnGyr.setOnClickListener(this);
        btnMag = (Button) findViewById(R.id.btnMag);
        btnMag.setOnClickListener(this);
        btnPSensor = (Button) findViewById(R.id.btnPSensor);
        btnPSensor.setOnClickListener(this);
        btnASensor = (Button) findViewById(R.id.btnASensor);
        btnASensor.setOnClickListener(this);
        btnOled = (Button) findViewById(R.id.btnOled);
        btnOled.setOnClickListener(this);

        edtOled = (EditText) findViewById(R.id.edtOled);

        etBright = (EditText) findViewById(R.id.Bright);
        etOled = (EditText) findViewById(R.id.Oled);
        etPSensor = (EditText) findViewById(R.id.PSensor);
        etTEMP = (EditText) findViewById(R.id.TEMP);
        etCustom = (EditText) findViewById(R.id.Custom);

        etGyr = (EditText) findViewById(R.id.Gyr);
        etGyrX = (EditText) findViewById(R.id.GyrX);
        etGyrY = (EditText) findViewById(R.id.GyrY);
        etGyrZ = (EditText) findViewById(R.id.GyrZ);

        etAcc = (EditText) findViewById(R.id.Acc);
        etAccX = (EditText) findViewById(R.id.AccX);
        etAccY = (EditText) findViewById(R.id.AccY);
        etAccZ = (EditText) findViewById(R.id.AccZ);

        etMag = (EditText) findViewById(R.id.Mag);
        etMagX = (EditText) findViewById(R.id.MagX);
        etMagY = (EditText) findViewById(R.id.MagY);
        etMagZ = (EditText) findViewById(R.id.MagZ);
        etMagR = (EditText) findViewById(R.id.MagR);

        etGTime = (EditText) findViewById(R.id.GTime);
        etATime = (EditText) findViewById(R.id.ATime);
        etMTime = (EditText) findViewById(R.id.MTime);
        etDate = (EditText) findViewById(R.id.Date);

        edtlogText = (EditText) findViewById(R.id.edtlogText);

        mLog("Initialized\nPlease select your USB HID device\n", false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSelectHIDDevice:
                eventBus.post(new PrepareDevicesListEvent());
                break;
            case R.id.btnHandShake:
                byte[] bytes = {6, 7, 13};
                eventBus.post(new USBDataSendEvent(bytes));
                break;
            case R.id.btnClear:
                edtlogText.setText("");
                break;
            case R.id.btnStartImu:
                byte[] bytes1 = {2, 7, 1};
                eventBus.post(new USBDataSendEvent(bytes1));
                break;
            case R.id.btnStopImu:
                byte[] bytes2 = {2, 7, 0};
                eventBus.post(new USBDataSendEvent(bytes2));
                break;
            case R.id.btn2D:
                byte[] bytes3 = {3, 2};
                eventBus.post(new USBDataSendEvent(bytes3));
                break;
            case R.id.btn3D:
                byte[] bytes4 = {3, 3};
                eventBus.post(new USBDataSendEvent(bytes4));
                break;
            case R.id.btnOled:
                try {
                    int value = Integer.parseInt(edtOled.getText().toString());
                    if (value < 0 || value > 190)
                        Toast.makeText(this, "Value of Oled is 0-190", Toast.LENGTH_SHORT).show();
                    else {
                        byte[] bytes5 = {4, (byte) (value < 128 ? value : value - 256)};
                        eventBus.post(new USBDataSendEvent(bytes5));
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Value of Oled is 0-190", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnAcc:
                byte[] bytes6 = {2, 23, 1};
                eventBus.post(new USBDataSendEvent(bytes6));
                break;
            case R.id.btnGyr:
                byte[] bytes7 = {2, 23, 2};
                eventBus.post(new USBDataSendEvent(bytes7));
                break;
            case R.id.btnMag:
                byte[] bytes8 = {2, 23, 3};
                eventBus.post(new USBDataSendEvent(bytes8));
                break;
            case R.id.btnPSensor:
                byte[] bytes9 = {2, 23, 4};
                eventBus.post(new USBDataSendEvent(bytes9));
                break;
            case R.id.btnASensor:
                byte[] bytes10 = {2, 23, 5};
                eventBus.post(new USBDataSendEvent(bytes10));
                break;
        }
    }


    void showListOfDevices(CharSequence devicesName[]) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (devicesName.length == 0) {
            builder.setTitle(Consts.MESSAGE_CONNECT_YOUR_USB_HID_DEVICE);
        } else {
            builder.setTitle(Consts.MESSAGE_SELECT_YOUR_USB_HID_DEVICE);
        }

        builder.setItems(devicesName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventBus.post(new SelectDeviceEvent(which));
            }
        });
        builder.setCancelable(true);
        builder.show();
    }

    public void onEvent(USBDataReceiveEvent event) {
//		event.getBytes();105，-122，29  ，1，8，8
        byte[] data = event.getBytes();
        if (data[0] == 105) {
            if (data[1] == -122 && data[2] == 29)
                mLog("recevie:Ack", true);
        } else if (data[0] == 1) {
            if (data[1] == 8)
                mLog("recevie:Heartbeat", true);
            else if (data[1] == 52) {
                //imu
                readIMU(data);
            }
        }
    }

    private int setShort(EditText et, short value, boolean unsign) {
        //交换高八位和低八位，并转为int
        int a = ((value & 0xff) << 8) | ((value >> 8) & 0xff);
        et.setText(et.getHint().toString() + "\n" + (unsign ? a : (short) a));
        return a;
    }

    private void setGyrShort(EditText et, short value, int range) {
        //交换高八位和低八位
        short a = (short) (((value & 0xff) << 8) | ((value >> 8) & 0xff));
        float result = (float) (range * 2 * a * 1000 / 65536);
        et.setText(et.getHint().toString() + "\n" + result);
    }

    private void setAccShort(EditText et, byte low, byte high, int range) {
        short a = (short) ((high << 4) | low);
        float result = (float) (range * 2 * 9.8 * a / 4096);
        et.setText(et.getHint().toString() + "\n" + result);
    }

    private void setMagShort(EditText et, byte low, byte high, int move, boolean unsign) {
        if (unsign) {
            int a = (((high << 8) & 0xff00) | low) >>> move;
            et.setText(et.getHint().toString() + "\n" + a);
        } else {
            short a = (short) (((high << 8) | low) >> move);
            et.setText(et.getHint().toString() + "\n" + a);
        }
    }

    private void setByte(EditText et, byte value) {
        //byte转为无符号int
        int a = value & 0xff;
        et.setText(et.getHint().toString() + "\n" + a);
    }

    private void setInt(EditText et, DataInputStream dis) throws IOException {
        byte[] value = new byte[4];
        value[3] = dis.readByte();
        value[2] = dis.readByte();
        value[1] = dis.readByte();
        value[0] = dis.readByte();
        DataInputStream ddis = new DataInputStream(new ByteArrayInputStream(value));
        //int转为无符号long
        et.setText(et.getHint().toString() + "\n" + ddis.readInt());
    }

    private long lastTime = 0;
    private int REFRESH_LIMIT = 30;

    private void readIMU(byte[] data) {
        if (System.currentTimeMillis() - lastTime > REFRESH_LIMIT) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            try {
                byte[] value = new byte[2];
                dis.skipBytes(2);
                value[0] = dis.readByte();//高八位
                setShort(etBright, dis.readShort(), true);//环境亮度
                setByte(etOled, dis.readByte());//oled亮度
                value[1] = dis.readByte();//低八位
                //自定义
                int custom = ((value[0] & 0xff) << 8) | (value[1] & 0xff);
                etCustom.setText(etCustom.getHint().toString() + "\n" + custom);

                setByte(etPSensor, dis.readByte());//P-Sensor状态
                setShort(etTEMP, dis.readShort(), true);//Temperature


                int gyr = setShort(etGyr, dis.readShort(), true);//GyrRange
                setGyrShort(etGyrX, dis.readShort(), gyr);//GyrXValue
                setGyrShort(etGyrY, dis.readShort(), gyr);//GyrYValue
                setGyrShort(etGyrZ, dis.readShort(), gyr);//GyrZValue

                int acc = setShort(etAcc, dis.readShort(), true);//AccRange
                setAccShort(etAccX, dis.readByte(), dis.readByte(), acc);//AccXValue
                setAccShort(etAccY, dis.readByte(), dis.readByte(), acc);//AccYValue
                setAccShort(etAccZ, dis.readByte(), dis.readByte(), acc);//AccZValue


                setShort(etMag, dis.readShort(), true);//MagRange

                setMagShort(etMagX, dis.readByte(), dis.readByte(), 3, false);//MagXValue signed 13bit
                setMagShort(etMagY, dis.readByte(), dis.readByte(), 3, false);//MagYValue signed 13bit
                setMagShort(etMagZ, dis.readByte(), dis.readByte(), 1, false);//MagZValue signed 15bit
                setMagShort(etMagR, dis.readByte(), dis.readByte(), 2, true);//MagRHall unsigned 14bit

                setInt(etGTime, dis);//GTimestamp
                setInt(etATime, dis);//ATimestamp
                setInt(etMTime, dis);//MTimestamp

                dis.readByte();
                byte month = dis.readByte();
                byte day = dis.readByte();
                int date = ((month & 0xff) << 8) | (day & 0xff);
                etDate.setText(etDate.getHint().toString() + "\n" + Integer.toHexString(date));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //输出日志
            StringBuffer sb = new StringBuffer();
            String temp;
            for (int i = 0; i < data.length; i++) {

                temp = Integer.toHexString(data[i] & 0xff);
                if (temp.length() <= 1)
                    sb.append("0");
                sb.append(temp);
                sb.append(" ");
            }
            mLog(sb.toString(), true);
            lastTime = System.currentTimeMillis();
        }
    }

    public void onEvent(LogMessageEvent event) {
        mLog(event.getData(), true);
    }

    public void onEvent(ShowDevicesListEvent event) {
        showListOfDevices(event.getCharSequenceArray());
    }

    public void onEvent(DeviceAttachedEvent event) {
        Toast.makeText(this, "Device attached!", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(DeviceDetachedEvent event) {
        Toast.makeText(this, "Device Dettached!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiveDataFormat = sharedPreferences.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT);
        prepareServices();
        setDelimiter();
        eventBus.register(this);
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        setSelectedMenuItemsFromSettings(menu);
        return true;
    }

    private void setSelectedMenuItemsFromSettings(Menu menu) {
        receiveDataFormat = sharedPreferences.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT);
        if (receiveDataFormat != null) {
            if (receiveDataFormat.equals(Consts.BINARY)) {
                menu.findItem(R.id.menuSettingsReceiveBinary).setChecked(true);
            } else if (receiveDataFormat.equals(Consts.INTEGER)) {
                menu.findItem(R.id.menuSettingsReceiveInteger).setChecked(true);
            } else if (receiveDataFormat.equals(Consts.HEXADECIMAL)) {
                menu.findItem(R.id.menuSettingsReceiveHexadecimal).setChecked(true);
            } else if (receiveDataFormat.equals(Consts.TEXT)) {
                menu.findItem(R.id.menuSettingsReceiveText).setChecked(true);
            }
        }

        setDelimiter();
        if (settingsDelimiter.equals(Consts.DELIMITER_NONE)) {
            menu.findItem(R.id.menuSettingsDelimiterNone).setChecked(true);
        } else if (settingsDelimiter.equals(Consts.DELIMITER_NEW_LINE)) {
            menu.findItem(R.id.menuSettingsDelimiterNewLine).setChecked(true);
        } else if (settingsDelimiter.equals(Consts.DELIMITER_SPACE)) {
            menu.findItem(R.id.menuSettingsDelimiterSpace).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.menuSettings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, Consts.RESULT_SETTINGS);
                break;
            case R.id.menuSettingsReceiveBinary:
                editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.BINARY).apply();
                break;
            case R.id.menuSettingsReceiveInteger:
                editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.INTEGER).apply();
                break;
            case R.id.menuSettingsReceiveHexadecimal:
                editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.HEXADECIMAL).apply();
                break;
            case R.id.menuSettingsReceiveText:
                editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT).apply();
                break;
            case R.id.menuSettingsDelimiterNone:
                editor.putString(Consts.DELIMITER, Consts.DELIMITER_NONE).apply();
                break;
            case R.id.menuSettingsDelimiterNewLine:
                editor.putString(Consts.DELIMITER, Consts.DELIMITER_NEW_LINE).apply();
                break;
            case R.id.menuSettingsDelimiterSpace:
                editor.putString(Consts.DELIMITER, Consts.DELIMITER_SPACE).apply();
                break;
        }

        receiveDataFormat = sharedPreferences.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT);
        setDelimiter();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case Consts.WEB_SERVER_CLOSE_ACTION:
                stopService(new Intent(this, WebServerService.class));
                break;
            case Consts.USB_HID_TERMINAL_CLOSE_ACTION:
                stopService(new Intent(this, SocketService.class));
                stopService(new Intent(this, WebServerService.class));
                stopService(new Intent(this, USBHIDService.class));
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Consts.USB_HID_TERMINAL_NOTIFICATION);
                finish();
                break;
            case Consts.SOCKET_SERVER_CLOSE_ACTION:
                stopService(new Intent(this, SocketService.class));
                sharedPreferences.edit().putBoolean("enable_socket_server", false).apply();
                break;
        }
    }

    private void setDelimiter() {
        settingsDelimiter = sharedPreferences.getString(Consts.DELIMITER, Consts.DELIMITER_NEW_LINE);
        if (settingsDelimiter != null) {
            if (settingsDelimiter.equals(Consts.DELIMITER_NONE)) {
                delimiter = "";
            } else if (settingsDelimiter.equals(Consts.DELIMITER_NEW_LINE)) {
                delimiter = Consts.NEW_LINE;
            } else if (settingsDelimiter.equals(Consts.DELIMITER_SPACE)) {
                delimiter = Consts.SPACE;
            }
        }
        usbService.setAction(Consts.RECEIVE_DATA_FORMAT);
        usbService.putExtra(Consts.RECEIVE_DATA_FORMAT, receiveDataFormat);
        usbService.putExtra(Consts.DELIMITER, delimiter);
        startService(usbService);
    }

    void sendToUSBService(String action) {
        usbService.setAction(action);
        startService(usbService);
    }

    void sendToUSBService(String action, boolean data) {
        usbService.putExtra(action, data);
        sendToUSBService(action);
    }

    void sendToUSBService(String action, int data) {
        usbService.putExtra(action, data);
        sendToUSBService(action);
    }

    private void mLog(String log, boolean newLine) {
        if (newLine) {
            edtlogText.append(Consts.NEW_LINE);
        }
        edtlogText.append(log);
    }

    private void webServerServiceIsStart(boolean isStart) {
        if (isStart) {
            Intent webServerService = new Intent(this, WebServerService.class);
            webServerService.setAction("start");
            webServerService.putExtra("WEB_SERVER_PORT", Integer.parseInt(sharedPreferences.getString("web_server_port", "7799")));
            startService(webServerService);
        } else {
            stopService(new Intent(this, WebServerService.class));
        }
    }

    private void socketServiceIsStart(boolean isStart) {
        if (isStart) {
            Intent socketServerService = new Intent(this, SocketService.class);
            socketServerService.setAction("start");
            socketServerService.putExtra("SOCKET_PORT", Integer.parseInt(sharedPreferences.getString("socket_server_port", "7899")));
            startService(socketServerService);
        } else {
            stopService(new Intent(this, SocketService.class));
        }
    }

    private void setVersionToTitle() {
        try {
            this.setTitle(Consts.SPACE + this.getTitle() + Consts.SPACE + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}