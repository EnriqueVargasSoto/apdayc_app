package com.expediodigital.ventas360.DAO;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.DTO.DTOMotivoBaja;
import com.expediodigital.ventas360.model.DevolucionDetalleModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;

import java.util.ArrayList;

public class DAOMotivoBaja {
    public static final String TAG = "DAOMotivoBaja";
    DataBaseHelper dataBaseHelper;
    Context context;

    public DAOMotivoBaja(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
        this.context = context;
    }

    public ArrayList<DTOMotivoBaja> getListaMotivos() {
        ArrayList<DTOMotivoBaja> listaProducto =  new ArrayList<>();

        String rawQuery =
                "SELECT * "+
                        "FROM "+ TablesHelper.MotivoBaja.Table;
        //Se consulta en flag Pendiente porque si existe una guia en el servidor estará en flag E. Por lo tanto el que se puede modificar y cambiar es el Pendiente y al final este reemplazará al Enviado tomando su lugar.

        Log.d(TAG,rawQuery);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);
        cur.moveToFirst();

        try {
            while (!cur.isAfterLast()) {
                DTOMotivoBaja item = new DTOMotivoBaja();
                item.setIdMotivoBaja(cur.getString(0));
                item.setDescripcion(cur.getString(1));

                listaProducto.add(item);
                cur.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cur.close();
        return listaProducto;
    }


}
