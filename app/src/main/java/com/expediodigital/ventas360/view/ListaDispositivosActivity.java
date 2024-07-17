package com.expediodigital.ventas360.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewDispositivoAdapter;
import com.expediodigital.ventas360.util.ItemDecoratorConvertido;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ListaDispositivosActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter bluetoothAdapter;

    private ProgressBar progressBar;
    private RecyclerView recycler_emparejados, recycler_otros;
    private RecyclerViewDispositivoAdapter adapterEmparejados;
    private RecyclerViewDispositivoAdapter adapterOtros;
    private ArrayList<HashMap<String,String>> listaEmparejados = new ArrayList<>();
    private ArrayList<HashMap<String,String>> listaOtros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_dispositivos);

        setTitle("Seleccioa un dispositivo");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recycler_emparejados = (RecyclerView) findViewById(R.id.recycler_emparejados);
        recycler_otros = (RecyclerView) findViewById(R.id.recycler_otros);

        recycler_emparejados.addItemDecoration(new ItemDecoratorConvertido(this, R.integer.margen_8dp,ItemDecoratorConvertido.TODOS_LADOS));
        recycler_otros.addItemDecoration(new ItemDecoratorConvertido(this, R.integer.margen_8dp,ItemDecoratorConvertido.TODOS_LADOS));

        adapterEmparejados = new RecyclerViewDispositivoAdapter(listaEmparejados,this);
        adapterOtros = new RecyclerViewDispositivoAdapter(listaOtros,this);

        recycler_emparejados.setAdapter(adapterEmparejados);
        recycler_otros.setAdapter(adapterOtros);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.btn_escanear);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getListaDispositivos();
    }

    private void getListaDispositivos() {
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                HashMap<String, String> item = new HashMap<>();
                item.put("dispositivo",device.getName());
                item.put("direccion",device.getAddress());
                listaEmparejados.add(item);
                adapterEmparejados.notifyDataSetChanged();
            }
        } else {
            String noDevices = "No hay dispositivos emparejados";
            HashMap<String, String> item = new HashMap<>();
            item.put("dispositivo",noDevices);
            item.put("direccion","");
            listaEmparejados.add(item);
            adapterEmparejados.notifyDataSetChanged();
        }
    }

    public void onClickDispositivo(HashMap<String,String> item){
        Log.i(TAG,"OnItemClickListener OnItemClickListener");
        // Cancel discovery because it's costly and we're about to connect
        bluetoothAdapter.cancelDiscovery();

        // Get the device MAC address
        String address = item.get("direccion");

        if (!address.isEmpty()){
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        progressBar.setVisibility(View.VISIBLE);
        setTitle("Escaneando dispositivos...");

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();
    }

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("dispositivo",device.getName());
                    item.put("direccion",device.getAddress());
                    listaOtros.add(item);
                    adapterOtros.notifyDataSetChanged();
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(View.GONE);
                setTitle("Seleccioa un dispositivo");
                if (listaOtros.size() == 0) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("dispositivo","No se encontraron dispositivos");
                    item.put("direccion","");
                    listaOtros.add(item);
                    adapterOtros.notifyDataSetChanged();
                }
            }
        }
    };
}
