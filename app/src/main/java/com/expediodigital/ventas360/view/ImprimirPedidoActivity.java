package com.expediodigital.ventas360.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.RT_Printer.BluetoothPrinter.BLUETOOTH.BluetoothPrintDriver;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.util.Util;
import com.example.tscdll.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ImprimirPedidoActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private static final int HEADER_FOOTER_SIZE = 39;

    // Intent request codes
    private static final int REQUEST_CONECTAR_DISPOSITIVO = 1;
    private static final int REQUEST_HABILITAR_BLUETOOTH = 2;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private Button btn_conectarBluetooth, btn_imprimir;
    private TextView tv_mensaje;
    private EditText edt_contenido;

    private BluetoothAdapter mBluetoothAdapter = null;
    //private BluetoothPrintDriver mPrintService = null;

    private boolean fuePreguntado = false;
    private String mConnectedDeviceName = null;
    private String deviceAddress = "";
    Ventas360App ventas360App;

    private String headerToPrint;
    private String bodyToPrint;
    private String footerToPrint;
    TSCActivity bluetooth2 = new TSCActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imprimir_pedido);

        ventas360App = (Ventas360App) getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Imprimir Pedido", true, this);

        headerToPrint = "\r\n" + getIntent().getExtras().getString("headerToPrint", "");
        bodyToPrint = getIntent().getExtras().getString("bodyToPrint", "");
        footerToPrint = getIntent().getExtras().getString("footerToPrint", "");

        tv_mensaje = (TextView) findViewById(R.id.tv_mensaje);
        edt_contenido = (EditText) findViewById(R.id.edt_contenido);
        btn_conectarBluetooth = (Button) findViewById(R.id.btn_conectarBluetooth);
        btn_imprimir = (Button) findViewById(R.id.btn_imprimir);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btn_conectarBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conectarBluetooth();
            }
        });

        btn_imprimir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                BluetoothPrintDriver.SetAlignMode((byte) 0x01);//0x01 Centrado, 0x00 Izquierda, 0x02 Derecha
//                BluetoothPrintDriver.SetBold((byte) 0x01);
//                BluetoothPrintDriver.BT_Write(headerToPrint);
//                BluetoothPrintDriver.BT_Write("\r");
//
//                BluetoothPrintDriver.SetAlignMode((byte) 0x00);//0x01 Centrado, 0x00 Izquierda, 0x02 Derecha
//                BluetoothPrintDriver.SetBold((byte) 0x00);
//                BluetoothPrintDriver.BT_Write(bodyToPrint);
//                BluetoothPrintDriver.BT_Write("\r");
//
//                //BluetoothPrintDriver.SetAlignMode((byte)0x01);//0x01 Centrado, 0x00 Izquierda, 0x02 Derecha
//                BluetoothPrintDriver.BT_Write(footerToPrint + "\n\n\n");
//                BluetoothPrintDriver.BT_Write("\r");

                String[] head_arr = headerToPrint.split("\n");
                String[] body_arr = bodyToPrint.split("\n");
                String[] footer_arr = footerToPrint.split("\n");
                String[] documento = new String[head_arr.length + body_arr.length + footer_arr.length + 6];
                for (int i = 0; i < head_arr.length; i++) {
                    documento[i] = head_arr[i];
                }
                int offset1 = head_arr.length;
                documento[offset1] = "\r\n";
                offset1 += 1;
                for (int i = 0; i < body_arr.length; i++) {
                    documento[i + offset1] = body_arr[i];
                }
                int offset2 = offset1 + body_arr.length;
                documento[offset2] = "\r\n";
                offset2 += 1;
                for (int i = 0; i < footer_arr.length; i++) {
                    documento[i + offset2] = footer_arr[i];
                }
                int offset3 = offset2 + footer_arr.length;
                documento[offset3] = "\r\n";
                //documento[offset3 + 1] = "\r\n";
                //documento[offset3 + 2] = "\r\n";

                int numLines = documento.length;
                int numProducts = (numLines - HEADER_FOOTER_SIZE)/2;
                double rowLong = 3.4;
                if (numProducts > 10) {
                    int residue = (int) Math.ceil(numProducts / 5.0); //Math.round(numProducts % 10);
                    rowLong = rowLong + (0.03 * residue);
                }
                double height = numLines * rowLong; //3.7
                int heightRounded = (int) Math.round(height);
                bluetooth2.setup(80, heightRounded, 4, 4, 0, 0, 0);
                bluetooth2.clearbuffer();
                for (int i = 0; i < documento.length; i++) {
                    int yy = 30 + i * 30;
                    if (documento[i] != null && !documento[i].equals("null")) {
                        bluetooth2.printerfont(10, yy, "2", 0, 1, 1, documento[i]);
                    }
                }
                bluetooth2.printlabel(1, 1);
            }
        });
        setup();
    }

    private void setup() {

        String textToPrint = headerToPrint + bodyToPrint + footerToPrint;

        /*
        textToPrint="CLIENTE:"+"CORNEJO GOMEZ, TERESA RINA"+"\n"+
                "RUC: 20129627947"+"\n"+
                "PEDIDO: "+"003019"+" "+"FECHA: "+"30/04/2018"+"\n"+
                "--------------------------------\n";
        textToPrint=textToPrint+"CAN.UNI.DESCRIP."+"PUNIT.IMPORT.\n\n";
        textToPrint += " 10 PQT SRADE PNR500"+"  17.6 176.0\n";
        textToPrint += "  2 UNI BIO ALOE UVA"+"  19.2  38.4\n";
        textToPrint += " 10 PQT PULP DUR TET"+"  12.9 129.0\n";
        textToPrint += "                    TOTAL: 343.4"+"\n\n\n";*/
        edt_contenido.setText(textToPrint);

        deviceAddress = ventas360App.getDeviceAddress();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            enableBluetooth();
            // Otherwise, setup the chat session
        } else {
            if (!deviceAddress.equals("")) {
//                if (mPrintService == null) setupPrint();
                if (bluetooth2 == null) setupPrint();

                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                // Attempt to connect to the device
//                mPrintService.connect(device);
                bluetooth2.openport(device.getAddress());
                if (bluetooth2.IsConnected) {
                    mConnectedDeviceName = device.getName();
                    tv_mensaje.setText("Conectado: " + mConnectedDeviceName);
                    tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.green_A400));
                    ventas360App.setDeviceAddress(deviceAddress);
                } else {
                    tv_mensaje.setText("No conectado");
                    tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.red_400));
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            enableBluetooth();
            // Otherwise, setup the chat session
        } else {
//            if (mPrintService == null) setupPrint();
            if (bluetooth2 == null) setupPrint();
        }
    }

    public void enableBluetooth() {
        if (!fuePreguntado) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_HABILITAR_BLUETOOTH);
            fuePreguntado = true;
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
//        if (mPrintService != null) {
//            // Only if the state is STATE_NONE, do we know that we haven't started already
//            if (mPrintService.getState() == BluetoothPrintDriver.STATE_NONE) {
//                // Start the Bluetooth chat services
//                mPrintService.start();
//            }
//        }
        if (bluetooth2 != null) {
        }

    }

    private void setupPrint() {
        Log.d(TAG, "setupChat()");
        // Initialize the BluetoothChatService to perform bluetooth connections
//        mPrintService = new BluetoothPrintDriver(this, mHandler);
        bluetooth2 = new TSCActivity();

        showMessage(bluetooth2.status());
    }

    void showMessage(String estado) {

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "- ON DESTROY -");
        // Stop the Bluetooth chat services
//        if (mPrintService != null) mPrintService.stop();
        if (bluetooth2.IsConnected) bluetooth2.closeport();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void conectarBluetooth() {
        Intent serverIntent = null;

        // Launch the DeviceListActivity to see devices and do scan
        serverIntent = new Intent(ImprimirPedidoActivity.this, ListaDispositivosActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONECTAR_DISPOSITIVO);

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothPrintDriver.STATE_CONNECTED:
                            tv_mensaje.setText("Conectado: " + mConnectedDeviceName);
                            tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.green_A400));
                            ventas360App.setDeviceAddress(deviceAddress);
                            break;
                        case BluetoothPrintDriver.STATE_CONNECTING:
                            tv_mensaje.setText("Conectando...");
                            tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.blue_400));
                            break;
                        case BluetoothPrintDriver.STATE_LISTEN:

                        case BluetoothPrintDriver.STATE_NONE:
                            tv_mensaje.setText("No conectado");
                            tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.red_400));
                            break;
                        default:
                            tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.grey_500));
                    }
                    break;
                case MESSAGE_READ:
                    String ErrorMsg = null;
                    byte[] readBuf = (byte[]) msg.obj;
                    float Voltage = 0;
                    Log.i(TAG, "readBuf[0]:" + readBuf[0] + "  readBuf[1]:" + readBuf[1] + "  readBuf[2]:" + readBuf[2]);

                    if (readBuf[2] == 0)
                        ErrorMsg = "NO ERROR!         ";
                    else {
                        if ((readBuf[2] & 0x02) != 0)
                            ErrorMsg = "ERROR: No printer connected!";
                        if ((readBuf[2] & 0x04) != 0)
                            ErrorMsg = "ERROR: No paper!  ";
                        if ((readBuf[2] & 0x08) != 0)
                            ErrorMsg = "ERROR: Voltage is too low!  ";
                        if ((readBuf[2] & 0x40) != 0)
                            ErrorMsg = "ERROR: Printer Over Heat!  ";
                    }
                    Voltage = (float) ((readBuf[0] * 256 + readBuf[1]) / 10.0);
                    //if(D) Log.i(TAG, "Voltage: "+Voltage);
                    Toast.makeText(ImprimirPedidoActivity.this, "Voltaje de la batería " + Voltage + " V", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONECTAR_DISPOSITIVO:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    deviceAddress = data.getExtras().getString(ListaDispositivosActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                    // Attempt to connect to the device
//                    mPrintService.connect(device);
                    bluetooth2.openport(device.getAddress());
                    if (bluetooth2.IsConnected) {
                        mConnectedDeviceName = device.getName();
                        tv_mensaje.setText("Conectado: " + mConnectedDeviceName);
                        tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.green_A400));
                        ventas360App.setDeviceAddress(deviceAddress);
                    } else {
                        tv_mensaje.setText("No conectado");
                        tv_mensaje.setTextColor(ContextCompat.getColor(ImprimirPedidoActivity.this, R.color.red_400));
                    }
                }
                break;
            case REQUEST_HABILITAR_BLUETOOTH:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupPrint();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    //Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


}
