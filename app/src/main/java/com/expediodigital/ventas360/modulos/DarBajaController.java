package com.expediodigital.ventas360.modulos;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.format.DateFormat;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.expediodigital.ventas360.DAO.DAOMotivoBaja;
import com.expediodigital.ventas360.DTO.DTOMotivoBaja;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.interfaces.Darbaja_listener;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;
import com.expediodigital.ventas360.view.fragment.ClientesListaFragment;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DarBajaController {
    Context mContext;
    ClienteModel mClienteModel;
    DataBaseHelper dataBaseHelper;
    Fragment fragment;
    ProgressDialog progressDialog;
    SoapManager soap_manager;
    int indexSelected = 0;
    DAOMotivoBaja daoMotivoBaja;
    Darbaja_listener listener;

    public DarBajaController(ClientesListaFragment fragment) {
        this.fragment = fragment;
        this.mContext = fragment.getContext();
        dataBaseHelper = DataBaseHelper.getInstance(mContext);
        soap_manager = new SoapManager(mContext);
        daoMotivoBaja = new DAOMotivoBaja(mContext);
    }

    public void setListener(Darbaja_listener listener) {
        this.listener = listener;
    }

    public void showOpcionesMotivo(ClienteModel cliente)
    {
        mClienteModel = cliente;

        AlertDialog.Builder alerta = new AlertDialog.Builder(mContext);
        alerta.setIcon(R.drawable.ic_dialog_block);
        alerta.setTitle("Motivo de Baja");

        //mostrar los motivos de baja
        final ArrayList<DTOMotivoBaja> listaMotivos = daoMotivoBaja.getListaMotivos();
        final ArrayList<String> lista = new ArrayList<String>();
        for (DTOMotivoBaja motivo:listaMotivos) {
            lista.add(motivo.getDescripcion());
        }

        String[] array = new String[lista.size()];
        lista.toArray(array);

        alerta.setSingleChoiceItems(array, indexSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                indexSelected = which;
            }
        });

        alerta.setCancelable(true);
        alerta.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO enviar al servidor usando indexSelected
//                final DTOMotivoNoVenta dtoMotivoNoVenta = listaMotivos.get(noVentaSelected);
//                guardarMotivoNoVenta(numeroPedido,dtoMotivoNoVenta.getIdMotivoNoVenta());
                send_server();

            }
        });
        alerta.setNegativeButton("CANCELAR", null);
        alerta.show();
    }

    private void send_server()
    {
        (new AsyncSend()).execute("");
    }

    class AsyncSend extends AsyncTask<String, String, String> {
        String mensajeSincronizacion = "Sincronizando....";

        protected void onPreExecute() {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setTitle("");
            progressDialog.setMessage(mensajeSincronizacion);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (Util.isConnectingToInternet()) {
                    //TODO flag y magic
                    int rpta = soap_manager.darBajaClienteJSON(TablesHelper.ClienteBaja.Create, TablesHelper.ClienteBaja.Table, mClienteModel.getIdCliente(), String.valueOf(indexSelected),
                            "0", "0");
                    if(rpta == 0){
                        return "1";
                    }
                    else{
                        return "Error al registrar los datos de contacto";
                    }
                }
                else {
                    return "NoConnectedToInternet";
                }
            }
            catch (NoSuchMethodException ex) {
                ex.printStackTrace();
                return "NoSuchMethodException";
            } catch (SocketTimeoutException ex) {
                ex.printStackTrace();
                return "SocketTimeoutException";
            } catch (IOException ex) {
                ex.printStackTrace();
                return ex.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();

            if(s != null && s.equals("1")){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setTitle("");
                builder.setMessage("El cliente a sido dado de baja.");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO actualizar lista de clientes
                        insertOnTable();
                        if(listener != null)
                        {
                            listener.actualizarLista();
                        }
                    }
                });
                builder.show();
            }
            else{
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setTitle("¡Error?");
                builder.setMessage(s);
                builder.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        }


        void insertOnTable() {

            String rawQuery = "SELECT COUNT(*) FROM " + TablesHelper.ClienteBaja.Table + " WHERE " + TablesHelper.ClienteBaja.idCliente + " = ?";
            String[] args = { mClienteModel.getIdCliente() };
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, args);
            Util.LogCursorInfo(cur, mContext);
            cur.moveToFirst();
            db.close();

            if(cur.getInt(0)>0)
            {
                Date currentTime = Calendar.getInstance().getTime();
                String str_fecha = DateFormat.format("yyyy-MM-dd HH:mm:ss", currentTime).toString();

                SQLiteDatabase db5 = dataBaseHelper.getWritableDatabase();
                String where = TablesHelper.ClienteBaja.idCliente + " = ?";
                ContentValues updateValues = new ContentValues();
                updateValues.put("idCliente", mClienteModel.getIdCliente());
                updateValues.put("motivo", String.valueOf(indexSelected));
                updateValues.put("flag", "0");
                updateValues.put("created_at", str_fecha);
                updateValues.put("updated_at", str_fecha);
                updateValues.put("magic", "0");
                db5.update(TablesHelper.ClienteWathsapp.Table, updateValues, where, args );

                String rawQuery3 = "SELECT * FROM " + TablesHelper.ClienteWathsapp.Table + " WHERE " + TablesHelper.ClienteWathsapp.idCliente + " = ?";
                SQLiteDatabase db3 = dataBaseHelper.getReadableDatabase();
                Cursor cur3 = db3.rawQuery(rawQuery3, args);
                Util.LogCursorInfo(cur3, mContext);
                cur3.moveToFirst();
                db3.close();
            }
            else{
                Date currentTime = Calendar.getInstance().getTime();
                String str_fecha = DateFormat.format("yyyy-MM-dd HH:mm:ss", currentTime).toString();
                String rawQuery2 = "INSERT INTO " + TablesHelper.ClienteBaja.Table +
                        " ('idCliente','motivo','flag','created_at','updated_at','magic') VALUES ( " +
                        "'"+ mClienteModel.getIdCliente() +"', "+"'"+ String.valueOf(indexSelected) +"', "+"'"+ 0 +"', "+"'"+ str_fecha +"', "+
                        "'"+ str_fecha +"', "+"'"+ 0 +"' )";

                SQLiteDatabase db2 = dataBaseHelper.getWritableDatabase();
                db2.execSQL(rawQuery2);
                db2.close();
            }
        }

    }
}
