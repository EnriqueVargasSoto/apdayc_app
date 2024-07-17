package com.expediodigital.ventas360.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.expediodigital.ventas360.model.EncuestaAlternativaModel;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.model.EncuestaDetallePreguntaModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaDetalleModel;
import com.expediodigital.ventas360.model.EncuestaRespuestaModel;
import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.TablesHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class DAOEncuesta {
    public final String TAG = this.getClass().getName();
    DataBaseHelper dataBaseHelper;

    public DAOEncuesta(Context context) {
        dataBaseHelper = DataBaseHelper.getInstance(context);
    }

    public ArrayList<EncuestaDetalleModel> getListaEncuestaDetalle() {
        ArrayList<EncuestaDetalleModel> lista = new ArrayList<>();

        try {
            String rawQuery =
                    "SELECT e.idEncuesta,e.descripcion, ifnull(et.descripcion,''), ed.idEncuestaDetalle, ed.fechaInicio, ed.fechaFin "+
                    "FROM "+TablesHelper.EncuestaDetalle.Table + " ed "+
                    "INNER JOIN "+TablesHelper.Encuesta.Table + " e ON ed."+TablesHelper.EncuestaDetalle.PKeyName +" = e."+TablesHelper.Encuesta.PKeyName + " "+
                    "LEFT JOIN "+TablesHelper.EncuestaTipo.Table + " et ON e."+TablesHelper.Encuesta.FKTipoEncuesta +" = et."+TablesHelper.EncuestaTipo.PKeyName + " "+
                    "WHERE date('now','localtime') BETWEEN date(fechaInicio) AND date(fechaFin) "+
                    "ORDER BY date(fechaInicio) DESC";
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    EncuestaDetalleModel item = new EncuestaDetalleModel();
                    item.setIdEncuesta(cur.getInt(0));
                    item.setDescripcionEncuesta(cur.getString(1));
                    item.setTipoEncuesta(cur.getString(2));

                    item.setIdEncuestaDetalle(cur.getInt(3));
                    item.setFechaInicio(cur.getString(4));
                    item.setFechaFin(cur.getString(5));
                    lista.add(item);
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return lista;
    }

    public EncuestaDetalleModel getEncuestaDetalle(int idEncuesta, int idEncuestaDetalle) {
        EncuestaDetalleModel item = null;

        try {
            String rawQuery =
                    "SELECT idEncuesta,idEncuestaDetalle,clientesObligatorios,clientesAnonimos,encuestasMinimas,fotosMinimas,maximoIntentosCliente,filtroOcasion,filtroCanalVentas,filtroGiro,filtroSubGiro,porCliente,porSegmento "+
                    "FROM "+TablesHelper.EncuestaDetalle.Table + " "+
                    "WHERE idEncuesta = '"+idEncuesta+"' AND idEncuestaDetalle = '"+idEncuestaDetalle+"'";
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    item = new EncuestaDetalleModel();
                    item.setIdEncuesta(cur.getInt(0));
                    item.setIdEncuestaDetalle(cur.getInt(1));
                    item.setClientesObligatorios(cur.getInt(2));
                    item.setClientesAnonimos(cur.getInt(3));
                    item.setEncuestasMinimas(cur.getInt(4));
                    item.setFotosMinimas(cur.getInt(5));
                    item.setMaximoIntentosCliente(cur.getInt(6));
                    item.setFiltroOcasion(cur.getString(7));
                    item.setFiltroCanalVentas(cur.getString(8));
                    item.setFiltroGiro(cur.getString(9));
                    item.setFiltroSubGiro(cur.getString(10));
                    item.setPorCliente(cur.getInt(11));
                    item.setPorSegmento(cur.getInt(12));

                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return item;
    }

    public EncuestaDetalleModel getEncuestaDetalle(String idTipoEncuesta) {
        EncuestaDetalleModel item = null;

        try {
            String rawQuery =
                    "SELECT e.idEncuesta,e.descripcion, ifnull(et.descripcion,''), ed.idEncuestaDetalle, ed.fechaInicio, ed.fechaFin, clientesObligatorios,e.idTipoEncuesta "+
                            "FROM "+TablesHelper.EncuestaDetalle.Table + " ed "+
                            "INNER JOIN "+TablesHelper.Encuesta.Table + " e ON ed."+TablesHelper.EncuestaDetalle.PKeyName +" = e."+TablesHelper.Encuesta.PKeyName + " "+
                            "LEFT JOIN "+TablesHelper.EncuestaTipo.Table + " et ON e."+TablesHelper.Encuesta.FKTipoEncuesta +" = et."+TablesHelper.EncuestaTipo.PKeyName + " "+
                            "WHERE e.idTipoEncuesta='"+idTipoEncuesta+"' AND date('now','localtime') BETWEEN date(fechaInicio) AND date(fechaFin)  "+
                            "ORDER BY date(fechaInicio) DESC LIMIT 1";
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    item = new EncuestaDetalleModel();
                    item.setIdEncuesta(cur.getInt(0));
                    item.setDescripcionEncuesta(cur.getString(1));
                    item.setTipoEncuesta(cur.getString(2));

                    item.setIdEncuestaDetalle(cur.getInt(3));
                    item.setFechaInicio(cur.getString(4));
                    item.setFechaFin(cur.getString(5));

                    item.setClientesObligatorios(cur.getInt(6));
                    item.setIdTipoEncuesta(cur.getString(7));
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return item;
    }

    public List<EncuestaDetallePreguntaModel> getListaDetallePreguntas(int idEncuesta, int idEncuestaDetalle) {
        List<EncuestaDetallePreguntaModel> listaPreguntasModel = new ArrayList<>();

        try {
            String rawQuery =
            "SELECT edp.idPregunta,edp.pregunta,edp.orden,edp.idTipoRespuesta,edp.requerido,ifnull(eap.idAlternativa,0),ifnull(eap.alternativa,''),ifnull(eap.orden,0) " +
            "FROM EncuestaDetallePregunta edp " +
            "LEFT JOIN EncuestaAlternativaPregunta eap ON edp.idEncuesta=eap.idEncuesta AND edp.idEncuestaDetalle=eap.idEncuestaDetalle AND edp.idPregunta=eap.idPregunta " +
            "WHERE edp.idEncuesta='"+idEncuesta+"' AND edp.idEncuestaDetalle='"+idEncuestaDetalle+"' "+
            "ORDER BY edp.orden,eap.orden";
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            //Transformar la lista de la base de datos a objetos
            int idPreguntaAnterior = 0;
            List<HashMap<String, Object>> listaItems = new ArrayList<>();
            ArrayList<EncuestaAlternativaModel> listaAlternativas = new ArrayList<>();
            EncuestaDetallePreguntaModel preguntaModel = new EncuestaDetallePreguntaModel();

            if (cur.moveToFirst()) {
                int contador = 0;
                do {
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("idPregunta", cur.getInt(0));
                    item.put("pregunta", cur.getString(1));
                    item.put("ordenPregunta", cur.getInt(2));
                    item.put("idTipoRespuesta", cur.getString(3));
                    item.put("requerido", cur.getInt(4));
                    item.put("idAlternativa", cur.getInt(5));
                    item.put("alternativa", cur.getString(6));
                    item.put("ordenAlternativa", cur.getInt(7));
                    listaItems.add(item);
                } while (cur.moveToNext());
            }
            cur.close();

            for (int i=0 ; i<listaItems.size(); i++){
                HashMap<String, Object> pregunta = listaItems.get(i);
                if ((int)pregunta.get("idPregunta") != idPreguntaAnterior){
                    preguntaModel = new EncuestaDetallePreguntaModel();
                    preguntaModel.setIdPregunta((int) pregunta.get("idPregunta"));
                    preguntaModel.setPregunta((String) pregunta.get("pregunta"));
                    preguntaModel.setOrdenPregunta((int) pregunta.get("ordenPregunta"));
                    preguntaModel.setTipoRespuesta((String) pregunta.get("idTipoRespuesta"));
                    preguntaModel.setRequerido((int) pregunta.get("requerido"));

                    listaAlternativas = new ArrayList<>();
                    if ((int) pregunta.get("idPregunta") != 0){
                        EncuestaAlternativaModel alternativaModel = new EncuestaAlternativaModel();
                        alternativaModel.setIdAlternativa((int) pregunta.get("idAlternativa"));
                        alternativaModel.setDescripcion((String) pregunta.get("alternativa"));
                        alternativaModel.setOrden((int) pregunta.get("ordenAlternativa"));
                        listaAlternativas.add(alternativaModel);
                    }
                    //Si es el último item de la lista, entonces se termina de agregar la lista de alternativas a la pregunta actual
                    if ( (i+1) >= listaItems.size()){
                        preguntaModel.setListaAlternativas(listaAlternativas);
                        listaPreguntasModel.add(preguntaModel);
                    }else{
                        //Si el ID de la siguiente pregunta es distinta, entonces se termina de agregar la lista de alternativas a la pregunta actual
                        if ((int) listaItems.get(i+1).get("idPregunta") != (int) pregunta.get("idPregunta")){
                            preguntaModel.setListaAlternativas(listaAlternativas);
                            listaPreguntasModel.add(preguntaModel);
                        }
                    }
                }else{
                    if ((int) pregunta.get("idAlternativa") != 0){
                        EncuestaAlternativaModel alternativaModel = new EncuestaAlternativaModel();
                        alternativaModel.setIdAlternativa((int) pregunta.get("idAlternativa"));
                        alternativaModel.setDescripcion((String) pregunta.get("alternativa"));
                        alternativaModel.setOrden((int) pregunta.get("ordenAlternativa"));
                        listaAlternativas.add(alternativaModel);
                    }

                    //Si es el último item de la lista, entonces se termina de agregar la lista de alternativas a la pregunta actual
                    if ( (i+1) >= listaItems.size()){
                        preguntaModel.setListaAlternativas(listaAlternativas);
                        listaPreguntasModel.add(preguntaModel);
                    }else{
                        //Si el ID de la siguiente pregunta es distinta, entonces se termina de agregar la lista de alternativas a la pregunta actual
                        if ((int) listaItems.get(i+1).get("idPregunta") != (int) pregunta.get("idPregunta")){
                            preguntaModel.setListaAlternativas(listaAlternativas);
                            listaPreguntasModel.add(preguntaModel);
                        }
                    }
                }
                idPreguntaAnterior = (int) pregunta.get("idPregunta");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return listaPreguntasModel;
    }

    public void guardarEncuestaRespuesta(EncuestaRespuestaModel encuestaRespuestaModel) {
        try {
            /*Eliminar toda la EncuestaRespuesta*/
            SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
            String where =  TablesHelper.EncuestaRespuestaDetalle.PKEncuesta+" = ? AND " +
                            TablesHelper.EncuestaRespuestaDetalle.PKEncuestaDetalle+" = ? AND " +
                            TablesHelper.EncuestaRespuestaDetalle.PKCliente+" = ? ";
            String[] args = { String.valueOf(encuestaRespuestaModel.getIdEncuesta()), String.valueOf(encuestaRespuestaModel.getIdEncuestaDetalle()), encuestaRespuestaModel.getIdCliente() };
            Log.i(TAG, "guardarEncuestaRespuesta: eliminando detalle...");
            db.delete(TablesHelper.EncuestaRespuestaDetalle.Table, where, args);
            Log.i(TAG, "guardarEncuestaRespuesta: eliminando cabecera...");
            db.delete(TablesHelper.EncuestaRespuestaCabecera.Table, where, args);
            /*Insertar toda la EncuestaRespuesta (cabecera y detalle)*/
            ContentValues Nreg = new ContentValues();
            Nreg.put(TablesHelper.EncuestaRespuestaCabecera.PKEncuesta,         encuestaRespuestaModel.getIdEncuesta());
            Nreg.put(TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle,  encuestaRespuestaModel.getIdEncuestaDetalle());
            Nreg.put(TablesHelper.EncuestaRespuestaCabecera.PKCliente,          encuestaRespuestaModel.getIdCliente());
            Nreg.put(TablesHelper.EncuestaRespuestaCabecera.FKVendedor,         encuestaRespuestaModel.getIdVendedor());
            Nreg.put(TablesHelper.EncuestaRespuestaCabecera.Fecha,              encuestaRespuestaModel.getFecha());
            Nreg.put(TablesHelper.EncuestaRespuestaCabecera.Flag,               encuestaRespuestaModel.getFlag());
            Log.i(TAG, "guardarEncuestaRespuesta: Guardando cabecera...");
            db.insert(TablesHelper.EncuestaRespuestaCabecera.Table, null, Nreg);

            try{
                Log.i(TAG, "guardarEncuestaRespuesta: Guardando detalle...");
                for (EncuestaRespuestaDetalleModel respuestaDetalle: encuestaRespuestaModel.getDetalle()) {
                    Nreg = new ContentValues();
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.PKEncuesta,      encuestaRespuestaModel.getIdEncuesta());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.PKEncuestaDetalle,encuestaRespuestaModel.getIdEncuestaDetalle());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.PKCliente,       encuestaRespuestaModel.getIdCliente());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.PKPregunta,      respuestaDetalle.getIdPregunta());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.PKAlternativas,  respuestaDetalle.getIdAlternativas());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.Descripcion,     respuestaDetalle.getDescripcion());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.TipoRespuesta,   respuestaDetalle.getTipoRespuesta());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.Latitud,         respuestaDetalle.getLatitud());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.Longitud,        respuestaDetalle.getLongitud());
                    Nreg.put(TablesHelper.EncuestaRespuestaDetalle.FotoURL,         respuestaDetalle.getFotoURL());
                    db.insert(TablesHelper.EncuestaRespuestaDetalle.Table, null, Nreg);
                }
            }catch (Exception e){
                Log.e(TAG,""+e.getMessage());
                e.printStackTrace();
            }
            Log.i(TAG, "guardarEncuestaRespuesta: Registros insertados");
        } catch (Exception e) {
            Log.e(TAG, "GuardarPedidoCabecera: Error al insertar registro "+e.getMessage());
            e.printStackTrace();
        }
    }

    public String actualizarFlagEncuesta (int idEncuesta, int idEncuestaDetalle, String idCliente, String flag){
        try {
            if (flag.equals(EncuestaRespuestaModel.FLAG_ENVIADO) || flag.equals(EncuestaRespuestaModel.FLAG_PENDIENTE) || flag.equals(EncuestaRespuestaModel.FLAG_INCOMPLETO)){
                SQLiteDatabase db = dataBaseHelper.getWritableDatabase();
                ContentValues updateValues = new ContentValues();
                updateValues.put(TablesHelper.EncuestaRespuestaCabecera.Flag, flag);
                String where = TablesHelper.EncuestaRespuestaCabecera.PKEncuesta + " = ? AND "+TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle + " = ? AND "+TablesHelper.EncuestaRespuestaCabecera.PKCliente + " = ? ";
                String[] args = { String.valueOf(idEncuesta), String.valueOf(idEncuestaDetalle), idCliente };

                Log.i(TAG, "Actualizar "+TablesHelper.EncuestaRespuestaCabecera.Table+": Modificando..."+flag);
                db.update(TablesHelper.EncuestaRespuestaCabecera.Table, updateValues, where, args );
            }
        } catch (Exception e) {
            Log.e(TAG, "Modificar "+TablesHelper.EncuestaRespuestaCabecera.Table+": Error al modificar registro");
            e.printStackTrace();
            flag = e.getMessage();
        }
        return flag;
    }

    public EncuestaRespuestaModel getEncuestaRespuesta(int idEncuesta, int idEncuestaDetalle, String idCliente) {
        EncuestaRespuestaModel encuestaRespuestaModel = null;
        try {
            String rawQuery =
                    "SELECT "+TablesHelper.EncuestaRespuestaCabecera.PKEncuesta+","+
                            TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle+","+
                            TablesHelper.EncuestaRespuestaCabecera.PKCliente+","+
                            TablesHelper.EncuestaRespuestaCabecera.FKVendedor+","+
                            TablesHelper.EncuestaRespuestaCabecera.Fecha+","+
                            TablesHelper.EncuestaRespuestaCabecera.Flag+" "+
                            "FROM "+TablesHelper.EncuestaRespuestaCabecera.Table + " "+
                            "WHERE "+TablesHelper.EncuestaRespuestaCabecera.PKEncuesta+"="+idEncuesta+" AND "+TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle+"="+idEncuestaDetalle+" AND "+TablesHelper.EncuestaRespuestaCabecera.PKCliente+"='"+idCliente+"'";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    encuestaRespuestaModel = new EncuestaRespuestaModel();
                    encuestaRespuestaModel.setIdEncuesta(cur.getInt(0));
                    encuestaRespuestaModel.setIdEncuestaDetalle(cur.getInt(1));
                    encuestaRespuestaModel.setIdCliente(cur.getString(2));
                    encuestaRespuestaModel.setIdVendedor(cur.getString(3));
                    encuestaRespuestaModel.setFecha(cur.getString(4));
                    encuestaRespuestaModel.setFlag(cur.getString(5));
                } while (cur.moveToNext());
            }
            cur.close();

            if (encuestaRespuestaModel != null){
                ArrayList<EncuestaRespuestaDetalleModel> detalle = new ArrayList<>();
                String rawQuery2 =
                        "SELECT "+TablesHelper.EncuestaRespuestaDetalle.PKPregunta+","+
                                TablesHelper.EncuestaRespuestaDetalle.PKAlternativas+","+
                                TablesHelper.EncuestaRespuestaDetalle.Descripcion+","+
                                TablesHelper.EncuestaRespuestaDetalle.TipoRespuesta+","+
                                TablesHelper.EncuestaRespuestaDetalle.Latitud+","+
                                TablesHelper.EncuestaRespuestaDetalle.Longitud+","+
                                TablesHelper.EncuestaRespuestaDetalle.FotoURL+" "+
                                "FROM "+TablesHelper.EncuestaRespuestaDetalle.Table + " "+
                                "WHERE "+TablesHelper.EncuestaRespuestaDetalle.PKEncuesta+"="+idEncuesta+" AND "+TablesHelper.EncuestaRespuestaDetalle.PKEncuestaDetalle+"="+idEncuestaDetalle+" AND "+TablesHelper.EncuestaRespuestaDetalle.PKCliente+"='"+idCliente+"'";
                Log.i(TAG,rawQuery2);
                Cursor cur2 = db.rawQuery(rawQuery2, null);

                if (cur2.moveToFirst()) {
                    do {
                        EncuestaRespuestaDetalleModel item = new EncuestaRespuestaDetalleModel();
                        item.setIdPregunta(cur2.getInt(0));
                        item.setIdAlternativas(cur2.getString(1));
                        item.setDescripcion(cur2.getString(2));
                        item.setTipoRespuesta(cur2.getString(3));
                        item.setLatitud(cur2.getDouble(4));
                        item.setLongitud(cur2.getDouble(5));
                        item.setFotoURL(cur2.getString(6));
                        detalle.add(item);
                    } while (cur2.moveToNext());
                }
                cur2.close();
                encuestaRespuestaModel.setDetalle(detalle);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return encuestaRespuestaModel;
    }

    public ArrayList<EncuestaRespuestaModel> getEncuestaPendientesEnvio(int idEncuesta, int idEncuestaDetalle) {
        ArrayList<EncuestaRespuestaModel> lista = new ArrayList<>();

        try {
            String rawQuery =
                    "SELECT "+TablesHelper.EncuestaRespuestaCabecera.PKEncuesta+","+
                            TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle+","+
                            TablesHelper.EncuestaRespuestaCabecera.PKCliente+","+
                            TablesHelper.EncuestaRespuestaCabecera.FKVendedor+","+
                            TablesHelper.EncuestaRespuestaCabecera.Fecha+","+
                            TablesHelper.EncuestaRespuestaCabecera.Flag+" "+
                            "FROM "+TablesHelper.EncuestaRespuestaCabecera.Table + " "+
                            "WHERE "+TablesHelper.EncuestaRespuestaCabecera.PKEncuesta+"="+idEncuesta+" AND "+TablesHelper.EncuestaRespuestaCabecera.PKEncuestaDetalle+"="+idEncuestaDetalle+" AND "+TablesHelper.EncuestaRespuestaCabecera.Flag+" in ('"+EncuestaRespuestaModel.FLAG_PENDIENTE+"','"+EncuestaRespuestaModel.FLAG_INCOMPLETO+"')";

            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    EncuestaRespuestaModel encuestaRespuestaModel = new EncuestaRespuestaModel();
                    encuestaRespuestaModel.setIdEncuesta(cur.getInt(0));
                    encuestaRespuestaModel.setIdEncuestaDetalle(cur.getInt(1));
                    encuestaRespuestaModel.setIdCliente(cur.getString(2));
                    encuestaRespuestaModel.setIdVendedor(cur.getString(3));
                    encuestaRespuestaModel.setFecha(cur.getString(4));
                    encuestaRespuestaModel.setFlag(cur.getString(5));
                    lista.add(encuestaRespuestaModel);
                } while (cur.moveToNext());
            }
            cur.close();

            for (int i = 0; i < lista.size(); i++) {
                // Seteo del detalle del pedido por el oc_numero
                ArrayList<EncuestaRespuestaDetalleModel> detalles = getEncuestaRespuestaDetalle(lista.get(i).getIdEncuesta(),lista.get(i).getIdEncuestaDetalle(),lista.get(i).getIdCliente());
                lista.get(i).setDetalle(detalles);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return lista;
    }

    public ArrayList<EncuestaRespuestaDetalleModel> getEncuestaRespuestaDetalle(int idEncuesta, int idEncuestaDetalle, String idCliente){
        ArrayList<EncuestaRespuestaDetalleModel> detalle = new ArrayList<>();

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        String rawQuery2 =
                "SELECT "+TablesHelper.EncuestaRespuestaDetalle.PKPregunta+","+
                        TablesHelper.EncuestaRespuestaDetalle.PKAlternativas+","+
                        TablesHelper.EncuestaRespuestaDetalle.Descripcion+","+
                        TablesHelper.EncuestaRespuestaDetalle.TipoRespuesta+","+
                        TablesHelper.EncuestaRespuestaDetalle.Latitud+","+
                        TablesHelper.EncuestaRespuestaDetalle.Longitud+","+
                        TablesHelper.EncuestaRespuestaDetalle.FotoURL+" "+
                        "FROM "+TablesHelper.EncuestaRespuestaDetalle.Table + " "+
                        "WHERE "+TablesHelper.EncuestaRespuestaDetalle.PKEncuesta+"="+idEncuesta+" AND "+TablesHelper.EncuestaRespuestaDetalle.PKEncuestaDetalle+"="+idEncuestaDetalle+" AND "+TablesHelper.EncuestaRespuestaDetalle.PKCliente+"='"+idCliente+"'";
        Log.i(TAG,rawQuery2);
        Cursor cur2 = db.rawQuery(rawQuery2, null);

        if (cur2.moveToFirst()) {
            do {
                EncuestaRespuestaDetalleModel item = new EncuestaRespuestaDetalleModel();
                item.setIdPregunta(cur2.getInt(0));
                item.setIdAlternativas(cur2.getString(1));
                item.setDescripcion(cur2.getString(2));
                item.setTipoRespuesta(cur2.getString(3));
                item.setLatitud(cur2.getDouble(4));
                item.setLongitud(cur2.getDouble(5));
                item.setFotoURL(cur2.getString(6));
                detalle.add(item);
            } while (cur2.moveToNext());
        }
        cur2.close();
        return detalle;
    }

    /**
     * Las encuestas con flag Enviado o Incompleto son las que están en el servidor
     * @return La lista de encuestas que han sido enviadas al servidor
     */
    public ArrayList<EncuestaRespuestaModel> getEncuestasEnviadas() {
        ArrayList<EncuestaRespuestaModel> lista = new ArrayList<>();

        try {
            String rawQuery =
                    "SELECT idEncuesta,idEncuestaDetalle,idCliente,idVendedor,fecha,flag "+
                    "FROM "+TablesHelper.EncuestaRespuestaCabecera.Table + " "+
                    "WHERE flag = 'E' OR flag = 'I'";
            Log.d(TAG,rawQuery);
            SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
            Cursor cur = db.rawQuery(rawQuery, null);

            if (cur.moveToFirst()) {
                do {
                    EncuestaRespuestaModel item = new EncuestaRespuestaModel();
                    item.setIdEncuesta(cur.getInt(0));
                    item.setIdEncuestaDetalle(cur.getInt(1));
                    item.setIdCliente(cur.getString(2));
                    item.setIdVendedor(cur.getString(3));
                    item.setFecha(cur.getString(4));
                    item.setFlag(cur.getString(5));
                    lista.add(item);
                } while (cur.moveToNext());
            }
            cur.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return lista;
    }

    public boolean tieneEncuestaRealizada(int idEncuesta, int idEncuestaDetalle, String idCliente){
        boolean flag = false;
        String rawQuery = "SELECT * FROM "+TablesHelper.EncuestaRespuestaCabecera.Table+" WHERE idEncuesta='"+idEncuesta+"' AND idEncuestaDetalle='"+idEncuestaDetalle+"' AND idCliente='"+idCliente+"'";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            flag = true;
            cur.moveToNext();
        }
        cur.close();
        return flag;
    }

    public EncuestaDetalleModel getEncuestaPrePedido(){
        EncuestaDetalleModel encuestaDetalleModel = null;
        String rawQuery =
                "SELECT e.idEncuesta, ed.idEncuestaDetalle, e.descripcion, ifnull(et.descripcion, ''), ifnull(clientesObligatorios,0), e.idTipoEncuesta " +
                "FROM "+TablesHelper.Encuesta.Table + " e "+
                "INNER JOIN "+TablesHelper.EncuestaDetalle.Table + " ed ON e.idEncuesta = ed.idEncuesta "+
                "LEFT JOIN "+TablesHelper.EncuestaTipo.Table + " et ON e.idTipoEncuesta = et.idTipoEncuesta "+
                "WHERE e."+TablesHelper.Encuesta.FKTipoEncuesta+" = '"+EncuestaDetalleModel.TIPO_PRE_PEDIDO+"' "+
                "AND date('now','localtime') BETWEEN date(fechaInicio) AND date(fechaFin) "+
                "ORDER BY date(fechaInicio) DESC";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            encuestaDetalleModel = new EncuestaDetalleModel();
            encuestaDetalleModel.setIdEncuesta(cur.getInt(0));
            encuestaDetalleModel.setIdEncuestaDetalle(cur.getInt(1));
            encuestaDetalleModel.setDescripcionEncuesta(cur.getString(2));
            encuestaDetalleModel.setTipoEncuesta(cur.getString(3));
            encuestaDetalleModel.setClientesObligatorios(cur.getInt(4));
            encuestaDetalleModel.setIdTipoEncuesta(cur.getString(5));
            cur.moveToNext();
        }
        cur.close();
        return encuestaDetalleModel;
    }
    public EncuestaDetalleModel getEncuestaPostPedido(){
        EncuestaDetalleModel encuestaDetalleModel = null;
        String rawQuery =
                "SELECT e.idEncuesta, ed.idEncuestaDetalle, e.descripcion, ifnull(et.descripcion, ''), ifnull(clientesObligatorios,0), e.idTipoEncuesta " +
                "FROM "+TablesHelper.Encuesta.Table + " e "+
                "INNER JOIN "+TablesHelper.EncuestaDetalle.Table + " ed ON e.idEncuesta = ed.idEncuesta "+
                "LEFT JOIN "+TablesHelper.EncuestaTipo.Table + " et ON e.idTipoEncuesta = et.idTipoEncuesta "+
                "WHERE e."+TablesHelper.Encuesta.FKTipoEncuesta+" = '"+EncuestaDetalleModel.TIPO_POST_PEDIDO+"' "+
                "AND date('now','localtime') BETWEEN date(fechaInicio) AND date(fechaFin) "+
                "ORDER BY date(fechaInicio) DESC";

        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cur = db.rawQuery(rawQuery, null);

        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            encuestaDetalleModel = new EncuestaDetalleModel();
            encuestaDetalleModel.setIdEncuesta(cur.getInt(0));
            encuestaDetalleModel.setIdEncuestaDetalle(cur.getInt(1));
            encuestaDetalleModel.setDescripcionEncuesta(cur.getString(2));
            encuestaDetalleModel.setTipoEncuesta(cur.getString(3));
            encuestaDetalleModel.setClientesObligatorios(cur.getInt(4));
            encuestaDetalleModel.setIdTipoEncuesta(cur.getString(5));
            cur.moveToNext();
        }
        cur.close();
        return encuestaDetalleModel;
    }
}
