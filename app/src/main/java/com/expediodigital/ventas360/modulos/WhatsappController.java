package com.expediodigital.ventas360.modulos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.expediodigital.ventas360.DTO.DTOClientWhatsapp;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.WhatsappFormActivity;
import com.expediodigital.ventas360.view.fragment.ClientesListaFragment;

import java.util.ArrayList;

public class WhatsappController {
    Context mContext;
    ClienteModel mClienteModel;
    DataBaseHelper dataBaseHelper;
    DTOClientWhatsapp currModel;
    Fragment fragment;

    public WhatsappController(ClientesListaFragment fragment) {
        this.fragment = fragment;
        this.mContext = fragment.getContext();
        dataBaseHelper = DataBaseHelper.getInstance(mContext);
    }

    public int getDrawable(ClienteModel clienteModel)
    {
        mClienteModel = clienteModel;
        if(!checkWhatsapp()){
            return R.drawable.ic_whatsapp_off;
        }else {
            return R.drawable.ic_whatsapp;
        }
    }

    public boolean checkWhatsapp()
    {
        if(mClienteModel == null || mClienteModel.getIdCliente() == null || mClienteModel.getIdCliente().isEmpty()){
            return false;
        }

        String rawQuery = "SELECT * FROM " + TablesHelper.ClienteWathsapp.Table + " WHERE " + TablesHelper.ClienteWathsapp.idCliente + " = ?";
        String[] args = { mClienteModel.getIdCliente() };
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, args);
        Util.LogCursorInfo(cur, mContext);

        ArrayList<DTOClientWhatsapp> lista_clientes = new ArrayList<>();
        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            currModel = new DTOClientWhatsapp();
            currModel.setIdCliente(cur.getString(0));
            currModel.setWhathsapp(cur.getString(1));
            currModel.setCodigociudad(cur.getString(2));
            currModel.setTelefonofijo(cur.getString(3));
            currModel.setEmail(cur.getString(4));
            currModel.setFechaRegistro(cur.getString(5));
            lista_clientes.add(currModel);
            cur.moveToNext();
        }
        cur.close();
        db.close();

        if(lista_clientes.size() > 0){
            return true;
        }
        else {
            return false;
        }
    }

    public void revisarWhatsapp(ClienteModel cliente)
    {
        mClienteModel = cliente;
        if(checkWhatsapp()){
            boolean installed = appInstalledOrNot("com.whatsapp");
            if(!installed) {
                showDialogoNoWhatsapp();
                return;
            }
            showDialogoOpcionWhatsapp();
        }else{
            Intent intent = new Intent(mContext, WhatsappFormActivity.class);
            intent.putExtra("idClient",mClienteModel.getIdCliente());
            fragment.startActivityForResult(intent,124);
        }
    }

    public void showDialogoOpcionWhatsapp(){

        Intent intent = new Intent(mContext, WhatsappFormActivity.class);
        intent.putExtra("idClient",mClienteModel.getIdCliente());
        intent.putExtra("email",currModel.getEmail());
        intent.putExtra("whatsapp",currModel.getWhathsapp());
        intent.putExtra("ciudad",currModel.getCodigociudad());
        intent.putExtra("fijo",currModel.getTelefonofijo());
        fragment.startActivityForResult(intent,124);

    }

    public void showDialogoNoWhatsapp() {
        final AlertDialog.Builder alerta = new AlertDialog.Builder(mContext);
        alerta.setIcon(R.drawable.ic_dialog_block);
        alerta.setTitle("¡Atencion!");
        alerta.setMessage("Es necesario que instales el app de Whatsapp.");
        alerta.setCancelable(true);
        alerta.setPositiveButton("ENTENDIDO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alerta.show();
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = mContext.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}
