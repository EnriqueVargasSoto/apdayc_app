package com.expediodigital.ventas360.migraciones;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.expediodigital.ventas360.util.DataBaseHelper;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;

public class MigrarPedidoDetalle1 {

    Context mContexto;
    SoapManager soap_manager;
    DataBaseHelper mDataBaseHelper;

    public MigrarPedidoDetalle1(DataBaseHelper dataBaseHelper, Context contexto)
    {

        mContexto = contexto;
        mDataBaseHelper = dataBaseHelper;
        soap_manager = new SoapManager(contexto);

    }

    public void moverTabla()
    {
        String rawQuery = "ALTER TABLE \"PedidoDetalle\" RENAME TO \"PedidoDetalleOld\"";
        //String rawQuery = "DROP TABLE \"PedidoDetalle\" ";
        SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();
        db.execSQL(rawQuery);

        String rawQuery1 = "CREATE TABLE IF NOT EXISTS \"PedidoDetalle\" (\"numeroPedido\" TEXT NOT NULL, \"idProducto\" TEXT NOT NULL, \"idPoliticaPrecio\" TEXT, \"tipoProducto\" TEXT NOT NULL,\"precioBruto\" REAL, \"cantidad\" INTEGER, \"precioNeto\" REAL, \"idUnidadMedida\" TEXT, \"pesoNeto\" REAL, \"item\" INTEGER, \"sinStock\" INTEGER, \"percepcion\" REAL, \"ISC\" TEXT, \"malla\" TEXT )";
        db.execSQL(rawQuery1);

        String rawQuery2 = "INSERT INTO PedidoDetalle(numeroPedido, idProducto, tipoProducto, idPoliticaPrecio, precioBruto, cantidad, precioNeto, idUnidadMedida, pesoNeto, item, sinStock, percepcion, ISC)\n" +
                "SELECT numeroPedido, idProducto, tipoProducto, idPoliticaPrecio, precioBruto, cantidad, precioNeto, idUnidadMedida, pesoNeto, item, sinStock, percepcion, ISC\n" +
                "FROM PedidoDetalleOld";
        db.execSQL(rawQuery2);

    }

    public void checkMalla(){
        boolean isMalla = mDataBaseHelper.isFieldExist(TablesHelper.PedidoDetalle.Table,"malla");
        boolean isISC = mDataBaseHelper.isFieldExist(TablesHelper.PedidoDetalle.Table,"ISC");
        if(!isMalla || !isISC){
            moverTabla();
        }
    }
}
