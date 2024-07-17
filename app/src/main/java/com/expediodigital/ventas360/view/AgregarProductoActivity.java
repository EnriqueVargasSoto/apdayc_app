package com.expediodigital.ventas360.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.DAO.DAOProducto;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.adapter.SpinnerPoliticaPrecioAdapter;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.PoliticaPrecioModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.UnidadMedidaModel;
import com.expediodigital.ventas360.model.VendedorModel;
import com.expediodigital.ventas360.util.SoapManager;
import com.expediodigital.ventas360.util.TablesHelper;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;
import java.util.List;

public class AgregarProductoActivity extends AppCompatActivity {
    public static final String TAG = "AgregarProductoActivity";
    private int REQUEST_CODE_BUSCAR = 1;
    private int ACCION_PRODUCTO;
    DAOProducto daoProducto;
    DAOCliente daoCliente;

    TextInputEditText edt_descripcion, edt_stock, edt_precio, edt_cantidad;
    TextView tv_idProducto;
    Switch switch_stockEnLinea;
    private Spinner spn_politicaPrecio,spn_unidadMedida,spn_subunidadMedida;
    LinearLayout linearStock;

    String idCliente="";
    String idAlmacen="";
    String idProducto="";
    String descripcion="";
    String numeroPedido="";
    double precioBruto = 0.0;

    //tipoProducto indica si el producto es de Venta o Publicidad
    String tipoProducto = "";

    //FactorConversion: indica cuantas "unidades" vienen en una "caja"
    int factorConversion = 1;

    //Peso: indica el peso del producto (de solo uno), se está considerando que el peso es de la unidad mayor
    double peso = 0.0;

    int stockUnidadMenor = 0, stockUnidadMayor = 0;

    /**  Variables de Preferencias **/
    boolean settings_validarStock;
    boolean settings_stockEnLinea;
    boolean settings_productoSinPrecio;
    boolean bloquearPrecio = false;

    ArrayList<UnidadMedidaModel> listaUnidadMedida = new ArrayList<>();
    ArrayList<PoliticaPrecioModel> listaPoliticaPrecio = new ArrayList<>();
    SpinnerPoliticaPrecioAdapter politicaPrecioAdapter;
    Ventas360App ventas360App;

    PoliticaPrecioModel selecc;
    List<String> subUnidades = new ArrayList<>();
    ArrayAdapter<String> subAdapter;
    int seleccSubUnidad = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);
        Log.i(TAG,"ONCREATE SOLO DEBE SER UNA VEZ !!!");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Util.actualizarToolBar("Agregar Producto",true,this,R.drawable.ic_action_close);

        ventas360App = (Ventas360App) getApplicationContext();

        daoProducto = new DAOProducto(getApplicationContext());
        daoCliente = new DAOCliente(getApplicationContext());

        tv_idProducto = (TextView) findViewById(R.id.tv_idProducto);
        edt_descripcion = (TextInputEditText) findViewById(R.id.edt_descripcion);
        edt_stock = (TextInputEditText) findViewById(R.id.edt_stock);
        edt_precio = (TextInputEditText) findViewById(R.id.edt_precio);
        edt_cantidad = (TextInputEditText) findViewById(R.id.edt_cantidad);
        spn_politicaPrecio = (Spinner) findViewById(R.id.spn_politicaPrecio);
        spn_unidadMedida = (Spinner) findViewById(R.id.spn_unidadMedida);
        spn_subunidadMedida = (Spinner) findViewById(R.id.spn_subunidadMedida);
        switch_stockEnLinea = (Switch) findViewById(R.id.switch_stockEnLinea);
        linearStock = (LinearLayout) findViewById(R.id.linearStock);

        idAlmacen = ventas360App.getIdAlmacen();
        //Obtener las variables de Configuración
        settings_validarStock = ventas360App.getSettings_validarStock();
        settings_stockEnLinea = ventas360App.getSettings_stockEnLinea();

        settings_productoSinPrecio = ventas360App.getSettings_productoSinPrecio();
        //----------------------------------

        //No editable
        edt_precio.setInputType(InputType.TYPE_NULL);

        edt_precio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //Log.i(TAG,"terminoCargar: "+terminoCargar);
            }
        });

        spn_politicaPrecio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (bloquearPrecio == false){
                    if (spn_unidadMedida.getSelectedItemPosition() == 0) {//La posicion 0 siempre debe ser la unidadMayor para calcular por "Unidad de venta (Solo se venden en unidadMayor)"
                        //Si la unidad de medida seleccionada no es la primera se muestra el precio mayor (cajas, pack, etc.)
                        edt_precio.setText(politicaPrecioAdapter.getItem(position).getPrecioManejo() + "");
                        Log.w(TAG,"spn_politicaPrecio edt_precio.setText");
                    } else {
                        //Si la unidad de medida seleccionada es la primera se muestra el precio por unidad Menor
                        edt_precio.setText(politicaPrecioAdapter.getItem(position).getPrecioContenido() + "");
                        Log.w(TAG,"spn_politicaPrecio edt_precio.setText");
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spn_politicaPrecio.setEnabled(false);

        /*Hay un tema con los spinner, los onItemSelectedListener de ambos spinner se ejecutan justo después de terminar el OnCreate,
        * las acciones que se realizan dentro de los listener (settear el precio) no deben ejecutarse, ya que al modificar un producto tipo Servicio
        * se debe cargar el precio que se tenía.
        * Para esto, se utiliza el flag bloquearPrecio que es true solo cuando se está modificando un producto tipo Servicio
        * Siempre el ultimo en ejecutarse es el listener de spn_unidadMedida, asi que ahi es donde se cambia el valor por false. Para que en adelante
        * ya no se bloquee el precio y si se modifique con los spinner automáticamente.
        * En otras palabras al terminar de cargar la vista, el precio del Servicio será colocado y no se alterará, y los spinner funcionarán correctamente.

        * En caso se esté agregando el producto, no se hacen validaciones ya que por defecto el flag bloquearPrecio está inactivo y solo se activa si se
        * está modificando un producto y que sea tipo Servicio. Por tanto esta funcionalidad no se verá afectado.
        * */
        spn_unidadMedida.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                try{
                    if (bloquearPrecio == false){

                        String unidadMedida = listaUnidadMedida.get(position).getIdUnidadManejo();

                        subUnidades.set(0,listaUnidadMedida.get(position).getDescripcion());
                        seleccSubUnidad = 0;
                        subAdapter = new ArrayAdapter<String>(AgregarProductoActivity.this,android.R.layout.simple_spinner_dropdown_item,subUnidades);
                        spn_subunidadMedida.setAdapter(subAdapter);
                        subAdapter.notifyDataSetChanged();

                        if(listaUnidadMedida.get(position).getIdUnidadContable().equals(listaUnidadMedida.get(position).getIdUnidadManejo()) &&
                                listaUnidadMedida.get(position).getIdUnidadContable().equals("UND")){
                            factorConversion = 1;
                        }else{
                            factorConversion = Integer.valueOf(listaUnidadMedida.get(position).getContenido());
                        }

                        selecc = null;
                        for (PoliticaPrecioModel ppm:listaPoliticaPrecio) {
                            if(unidadMedida.equals(ppm.getIdUnidadManejo()))
                            {
                                selecc = ppm;
                            }
                        }

                        if(selecc != null) {
                            if(listaUnidadMedida.get(position).getIdUnidadContable().equals(listaUnidadMedida.get(position).getIdUnidadManejo()) &&
                                    listaUnidadMedida.get(position).getIdUnidadContable().equals("UND")){
                                double contenido = (double)Integer.valueOf(listaUnidadMedida.get(position).getContenido());
                                //double pm = (selecc.getPrecioManejo()/contenido);
                                double pm = selecc.getPrecioContenido();
                                String spm = pm + "";
                                edt_precio.setText( spm );
                            }
                            else{
                                edt_precio.setText(selecc.getPrecioManejo() + "");
                            }
                            Log.w(TAG,"spn_unidadMedida edt_precio.setText");
                        }
                        else{

                            edt_precio.setText("0.0");
                            new AlertDialog.Builder(AgregarProductoActivity.this)
                                    .setTitle("Alerta")
                                    .setMessage("La unidad de medida seleccionada no tiene informacion relacionada a la politica de precio x producto")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    })
                                    .create().show();
                        }

                        /*if (position == 0){//La posicion 0 siempre debe ser la unidadMayor para calcular por "Unidad de venta (Solo se venden en unidadMayor)"
                            //Si la unidad de medida seleccionada es la primera se muestra el precio mayor (cajas, pack, etc.)
                            edt_precio.setText(politicaPrecioAdapter.getItem(spn_politicaPrecio.getSelectedItemPosition()).getPrecioManejo()+"");
                            Log.w(TAG,"spn_unidadMedida edt_precio.setText");
                        }else {
                            //Si la unidad de medida seleccionada no es la primera se muestra el precio por unidad Menor
                            edt_precio.setText(politicaPrecioAdapter.getItem(spn_politicaPrecio.getSelectedItemPosition()).getPrecioContenido()+"");
                            Log.w(TAG,"spn_unidadMedida edt_precio.setText");
                        }*/
                    }else{
                        //Si actualmente está bloqueado el precio porque se está cargando un producto tipo Servicio
                        bloquearPrecio = false;
                    }

                    getStockLocal();
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    Snackbar.make(findViewById(android.R.id.content), "Ocurrió un error con la política de precio", Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        subUnidades.add("");
        subUnidades.add("Unidad");
        subAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,subUnidades);
        spn_subunidadMedida.setAdapter(subAdapter);
        spn_subunidadMedida.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                seleccSubUnidad = i;
                try{
                    if (bloquearPrecio == false){

                        if(selecc != null && !selecc.getIdUnidadManejo().equals("UND")) {

                            if(i==0){//unidad de manejo
                                edt_precio.setText(selecc.getPrecioManejo() + "");
                                Log.w(TAG,"spn_subunidadMedida edt_precio.setText");
                            }else{
                                edt_precio.setText(selecc.getPrecioContenido() + "");
                                Log.w(TAG,"spn_subunidadMedida edt_precio.setText");
                            }
                        }

                    }else{
                        //Si actualmente está bloqueado el precio porque se está cargando un producto tipo Servicio
                        bloquearPrecio = false;
                    }

                    getStockLocal();
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    Snackbar.make(findViewById(android.R.id.content), "Ocurrió un error con la política de precio", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        switch_stockEnLinea.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                settings_stockEnLinea = checked;
                ventas360App.setSettings_stockEnLinea(checked);

                if (checked){
                    if (!idProducto.equals("")){
                        new async_getStockEnLinea().execute(false);
                    }
                }
            }
        });
        switch_stockEnLinea.setChecked(ventas360App.getSettings_stockEnLinea());

        //Validacion por modo de venta
        if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()){
            switch_stockEnLinea.setChecked(false);
            switch_stockEnLinea.setEnabled(false);//True si es que se se puede cambiar
            linearStock.setVisibility(View.GONE);
        }else if(ventas360App.getTipoVendedor().equals(VendedorModel.TIPO_MERCADEO)){
            switch_stockEnLinea.setChecked(false);
            switch_stockEnLinea.setEnabled(false);
        }else {//Si es autoventa o preventa en linea, se activa y no se puede cambiar nada
            switch_stockEnLinea.setChecked(true);
            switch_stockEnLinea.setEnabled(false);
        }
        edt_cantidad.requestFocus();

        //Se obtiene el parametro del tipo para saber si la accion a realizar es para agregar o modificar un producto
        Bundle data = getIntent().getExtras();
        ACCION_PRODUCTO = data.getInt("accion",PedidoActivity.ACCION_AGREGAR_PRODUCTO);
        numeroPedido = data.getString("numeroPedido");
        idCliente = data.getString("idCliente");

        if (ACCION_PRODUCTO == PedidoActivity.ACCION_AGREGAR_PRODUCTO){
            //Si es para agregar, lo primero que se debe hacer es mostrar la actividad BuscarProductoActivity, para que desde ahí se retorne los datos del producto seleccionado
            //hacia esta actividad. Y una vez se tenga los datos resultantes desde aquí se pueda retornar al PedidoActivity con el producto concreto.
            Intent intent = new Intent(this, BuscarProductoActivity.class);
            intent.putExtra("idCliente",idCliente);
            startActivityForResult(intent,REQUEST_CODE_BUSCAR);
        }else{
            //Si es para modificar, lo primero que se debe hacer es cargar los datos del producto a la vista, para que se pueda editar
            descripcion = data.getString("descripcion");
            idProducto = data.getString("idProducto");
            precioBruto = data.getDouble("precioBruto");
            peso = daoProducto.getPeso(idProducto);//Se obtiene el peso original del producto, mas no el del detalle ya que ese ya se ha modificado
            factorConversion = daoProducto.getFactorConversion(idProducto);
            tipoProducto = daoProducto.getTipoProducto(idProducto);

            tv_idProducto.setText(idProducto);
            edt_descripcion.setText(descripcion);

            cargarUnidadMedida();//Siempre cargar antes de obtener stock
            cargarPoliticasPrecio();

            if (settings_stockEnLinea) {
                new async_getStockEnLinea().execute(false);
            }else{
                getStockLocal();
            }

            DAOPedido daoPedido = new DAOPedido(getApplicationContext());
            PedidoDetalleModel producto = daoPedido.getProductoPedido(numeroPedido,idProducto);
            if (producto != null){
                //Seleccionar la unidad de medida
                for (int i=0; i < listaUnidadMedida.size(); i++){
                    if (listaUnidadMedida.get(i).getIdUnidadManejo().equals(producto.getIdUnidadMedida())){
                        Log.i(TAG,"SET SELECCTION PARA UNIDAD DE MEDIDA");
                        //spn_unidadMedida.setSelection(i,true);
                        spn_unidadMedida.setSelection(i);//avisa al listener luego de un rato
                        break;
                    }
                }

                /*Seleccionar la politica de precio *La politica no debe seleccionarse, ya que se cargará nuevamente la que corresponde al cliente y al final se validará su cantidadMinima para mantener su politica**/
                for (int i=0; i < politicaPrecioAdapter.getCount(); i++){
                    Log.i(TAG,"adapter getSecuenciaPolitica:"+politicaPrecioAdapter.getIdPolitica(i)+"-"+producto.getIdPoliticaPrecio()+":producto.getIdPolitica");
                    if (politicaPrecioAdapter.getIdPolitica(i).equals(producto.getIdPoliticaPrecio())){
                        //spn_politicaPrecio.setSelection(i,true);
                        spn_politicaPrecio.setSelection(i);
                        break;
                    }
                }


                edt_cantidad.setText(String.valueOf(producto.getCantidad()));
                edt_cantidad.requestFocus();
            }else{
                Toast.makeText(getApplicationContext(),"No se obtuvo el producto",Toast.LENGTH_SHORT).show();
            }

            //Validacion del tipo de Producto
            if (tipoProducto.equals(ProductoModel.TIPO_SERVICIO)){
                /*Se settea por defecto el precio con el que fue agregado el servicio, mas no el que manda la politica de precio, ya que el precio de un servicio puede ser modificado por el usuario.
                Esto siempre y cuando se esté modificando el producto ya que ahí si tendrá un precioBruto, de lo contrario se forzaría a setear un precioBruto en cero, y eso no es correcto*/
                edt_precio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                Log.i(TAG,"edt_precio.setText "+precioBruto);
                edt_precio.setText(String.valueOf(precioBruto));
                bloquearPrecio = true;//validacion para que luego de que termine el onCreate al ejecutarse los listener de los spinner, no cambie el precio que se está cargando en este momento
            }else{
                edt_precio.setInputType(InputType.TYPE_NULL);
            }
        }
        //terminoCargar = 0;
        Log.i(TAG,"terminó el OnCreate");
    }

    private void cargarUnidadMedida(){
        Log.i(TAG,"cargarUnidadMedida");
        listaUnidadMedida = daoProducto.getUnidadMedida(idProducto);
        ArrayList<String> unidades = new ArrayList<>();
        for (UnidadMedidaModel unidadMedida: listaUnidadMedida){
            unidades.add(unidadMedida.getDescripcion());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,unidades);
        spn_unidadMedida.setAdapter(adapter);
    }

    /**
     * Permite cargar la política de precio asignado para el cliente.
     */
    private void cargarPoliticasPrecio(){
        String idPoliticaCliente = daoCliente.getIdPoliticaPrecio(idCliente);
        String idPoliticaPorDefecto = daoProducto.getIdPoliticaPrecioPorDefecto();

        ArrayList<PoliticaPrecioModel> listaPoliticaPrecioGeneral = daoProducto.getPoliticaPrecios(idProducto);
        listaPoliticaPrecio.clear();

        //Se busca la política de precio del cliente, si existe se agrega a la lista de politicas que se van a mostrar en el spinner
        boolean politicaClienteValida = false;
        for (int i=0; i<listaPoliticaPrecioGeneral.size(); i++){
            if (listaPoliticaPrecioGeneral.get(i).getIdPoliticaPrecio().equals(idPoliticaCliente)){
                listaPoliticaPrecio.add(listaPoliticaPrecioGeneral.get(i));
                politicaClienteValida = true;
            }
        }
        if (!politicaClienteValida){
            edt_cantidad.setEnabled(false);//Si la politica no es valida entonces no se permite agregar la cantidad del producto.
            Snackbar.make(findViewById(android.R.id.content), "No se obtuvo una política válida para el cliente", Snackbar.LENGTH_LONG).show();
        }
/*
        //Se busca la política de precio general (Sucursal en configuracion), si existe se agrega a la lista de politicas que se van a mostrar en el spinner
        for (int i=0; i<listaPoliticaPrecioGeneral.size(); i++){
            if (listaPoliticaPrecioGeneral.get(i).getIdPoliticaPrecio().equals(idPoliticaPorDefecto)){
                listaPoliticaPrecio.add(listaPoliticaPrecioGeneral.get(i));
            }
        }

 */
        Log.w(TAG,"listaPoliticaPrecio size:"+listaPoliticaPrecio.size());
        //Al hacer doble for nos aseguramos de que la PoliticaPrecio del cliente se guarde en la posición 0, y que la políticaPorDefecto se guarde en segundo lugar.
        // Es decir que tendremos la certeza de que la posicion 0 es la del cliente y la 1 es la por defecto. Solo llamando a este método nos aseguramos de que se cargue la politica correspondiente
        politicaPrecioAdapter = new SpinnerPoliticaPrecioAdapter(this,listaPoliticaPrecio);
        spn_politicaPrecio.setAdapter(politicaPrecioAdapter);
    }

    private void getStockLocal(){
        stockUnidadMenor = daoProducto.getStockProducto(idProducto);
        stockUnidadMayor = (int)(stockUnidadMenor / factorConversion);

        if(listaUnidadMedida.size() == 0){
            Log.i(TAG,"getStockLocal: listaUnidadMedida es cero");
            return;
        }

        String descripcionStock = ""+stockUnidadMenor;
        if (seleccSubUnidad==1){//unidad de manejo menor
            int idx = spn_unidadMedida.getSelectedItemPosition();
            descripcionStock = stockUnidadMenor+" "+listaUnidadMedida.get(idx).getDescripcionContable()+"(S)";
        }else{
            int idx = spn_unidadMedida.getSelectedItemPosition();
            descripcionStock = stockUnidadMayor+" "+listaUnidadMedida.get(idx).getDescripcion()+"(S)";
            /*
            if (idx == 0){//La posicion 0 siempre debe ser la unidadMayor para calcular por "Unidad de venta (Solo se venden en unidadMayor)"
                //Si la unidad de medida seleccionada es la primera se muestra el stock mayor (cajas, pack, etc.)
                descripcionStock = stockUnidadMayor+" "+listaUnidadMedida.get(0).getDescripcion()+"(S)";
            }else {
                //Si la unidad de medida seleccionada no es la primera se muestra el stock por unidad Menor
                descripcionStock = stockUnidadMenor+" "+listaUnidadMedida.get(spn_unidadMedida.getSelectedItemPosition()).getDescripcion()+"(S)";
            }

             */

        }

        edt_stock.setText(descripcionStock);

        /*getStockLocal está dentro de un asyncTask, por tanto se resuelve luego de un determinado tiempo, al finalizar se mueven las unidades de medida y demás, ocasionando que se cargue
          la politica de precio correspondiente. Sin embargo, cuando se carga un producto tipo Servicio, se debe cargar el precio con el que estaba agregado, mas no el que manda la politicad e precio
           a fin de que cuando modifiquen cantidades de un servicio ya agregado, no tengan que estas modificando tambien el precio siempre.
           Esto siempre y cuando se esté modificando el producto ya que ahí si tendrá un precioBruto, de lo contrario se forzaría a setear un precioBruto en cero, y eso no es correcto*/
        Log.i(TAG,"getStockLocal finish");
        /*if (ACCION_PRODUCTO == PedidoActivity.ACCION_MODIFICAR_PRODUCTO && tipoProducto.equals(ProductoModel.TIPO_SERVICIO)){
            edt_precio.setText(String.valueOf(precioBruto));
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_check, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_aceptar:
                if (ventas360App.getModoVenta().equals(VendedorModel.MODO_PREVENTA) && !ventas360App.getSettings_preventaEnLinea()){
                    agregarProducto();
                }else{
                    if (settings_stockEnLinea) {//En autoventa siempre estará activo, asi que siempre valida en Linea
                        new async_getStockEnLinea().execute(true);
                    }else {
                        agregarProducto();
                    }
                }
                break;
            case android.R.id.home:
                //Cuando AgregarProductoActivity esté mostrandose el usuario pensará que si cierra retornará al activity de búsqueda
                //Sin embargo pasa lo contrario ya que el flujo es el siguiente PedidoActivity->AgregarProductoActivity->BuscarProductoActivity
                //Por lo tanto si estamos en AgregarProductoActivity y se quere ir a BuscarProductoActivity, este último debe llamarse y matener
                //la actividad esperando por los datos resultantes de la búsqueda (No finalizarla)
                Intent intent = new Intent(this, BuscarProductoActivity.class);
                intent.putExtra("idCliente",idCliente);
                startActivityForResult(intent,REQUEST_CODE_BUSCAR);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //terminoCargar = 1;

        if (requestCode == REQUEST_CODE_BUSCAR ){
            if (resultCode == RESULT_OK){
                Log.v(TAG,"RESULT OK:");
                //Limpiar los campos
                descripcion = "";
                idProducto = "";
                factorConversion = 1;
                peso = 0.0;
                tipoProducto = "";
                edt_cantidad.setEnabled(true);
                edt_cantidad.setText("");
                edt_cantidad.setError(null);

                if (data != null){
                    descripcion = data.getStringExtra("descripcion");
                    idProducto = data.getStringExtra("idProducto");
                    factorConversion = data.getIntExtra("factorConversion",1);
                    peso = data.getDoubleExtra("peso",0.0);
                    tipoProducto = data.getStringExtra("tipoProducto");

                    cargarUnidadMedida();//Siempre cargar antes de obtener stock
                    cargarPoliticasPrecio();

                    tv_idProducto.setText(idProducto);
                    edt_descripcion.setText(descripcion);

                    if (settings_stockEnLinea) {
                        new async_getStockEnLinea().execute(false);
                    }else{
                        getStockLocal();
                    }

                    if (tipoProducto.equals(ProductoModel.TIPO_VENTA)){
                        edt_precio.setInputType(InputType.TYPE_NULL);
                    }else{
                        edt_precio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    }
                }
            } else if (resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }

    void agregarProducto(){
        if (validarCampos()){
            /*if (validarPoliticaPrecio()){ *//*La validación de la politica ya no se hará por cada producto, si no al final del pedido*/
                Log.w(TAG,"Select item position spinner:"+spn_politicaPrecio.getSelectedItemPosition()+" Antes de retornar");
                Intent returnIntent = new Intent();
                returnIntent.putExtra("accion", ACCION_PRODUCTO);
                returnIntent.putExtra("idProducto", idProducto);
                returnIntent.putExtra("descripcion", descripcion);
                Log.w(TAG,"edt_precio.getText "+edt_precio.getText().toString()+" Antes de retornar");
                returnIntent.putExtra("precio", edt_precio.getText().toString());
                if(seleccSubUnidad == 1){
                    returnIntent.putExtra("idUnidadMedida", listaUnidadMedida.get(spn_unidadMedida.getSelectedItemPosition()).getIdUnidadContable());
                }else{
                    returnIntent.putExtra("idUnidadMedida", listaUnidadMedida.get(spn_unidadMedida.getSelectedItemPosition()).getIdUnidadManejo());
                }
                returnIntent.putExtra("idPoliticaPrecio", politicaPrecioAdapter.getItem(spn_politicaPrecio.getSelectedItemPosition()).getIdPoliticaPrecio());
                returnIntent.putExtra("cantidad", edt_cantidad.getText().toString());
                returnIntent.putExtra("factorConversion", factorConversion);
                Log.e(TAG,"Select item position spinner:"+spn_politicaPrecio.getSelectedItemPosition()+" despues");
                //El peso de los productos estará registrado en la unidad maxima, por lo tanto si se agregan unidades minimas se tiene que transformar
                if(seleccSubUnidad==0){
                    String dum = listaUnidadMedida.get(spn_unidadMedida.getSelectedItemPosition()).getDescripcion();
                    returnIntent.putExtra("descripcionUnidadMedida", dum);
                }else{
                    String dum = "Unid. desde " + listaUnidadMedida.get(spn_unidadMedida.getSelectedItemPosition()).getDescripcion();
                    returnIntent.putExtra("descripcionUnidadMedida", dum);
                    peso = peso / factorConversion;
                }

                /*
                int flagUnidadMedida = daoProducto.isUnidadMinima(idProducto,listaUnidadMedida.get(spn_unidadMedida.getSelectedItemPosition()).getIdUnidadMedida());
                if (flagUnidadMedida == -1){
                    //Si ambas unidades tanto minima como mayor son lo mismo, no hay necesidad de transformar el peso
                }else{
                    if (flagUnidadMedida == 1){
                        //Si la unidad es la minima se tiene que transformar
                        peso = peso / factorConversion;
                    }
                }
                */

                returnIntent.putExtra("peso", peso);
                returnIntent.putExtra("tipoProducto", tipoProducto);
                returnIntent.putExtra("porcentajePercepcion",daoProducto.getPorcentajePercepcion(idProducto));
                returnIntent.putExtra("porcentajeISC",daoProducto.getPorcentajeISC(idProducto));
                //Log.d(TAG,"retornando factorConversion:"+factorConversion+"  peso:"+peso);
                setResult(RESULT_OK, returnIntent);
                finish();
            /*}else{
                Snackbar.make(findViewById(android.R.id.content), "No se obtuvo una política de precio válida", Snackbar.LENGTH_LONG).show();
            }*/
        }
    }

    private boolean validarPoliticaPrecio() {
        try{
            int cantidad = Integer.parseInt(edt_cantidad.getText().toString());
            int cantidadMinima = politicaPrecioAdapter.getItem(spn_politicaPrecio.getSelectedItemPosition()).getCantidadMinima();

            if (cantidad < cantidadMinima){//Si no se alcanza la cantidad para mantener la política del cliente, se selecciona la politicaPorDefecto (Minorista)
                spn_politicaPrecio.setSelection(1);//Se selecciona la segunda posición (politica general o por defecto)

                //Ejecutamos la funcionalidad del listener desde ya, porque (setOnItemSelectedListener) no se ejecuta de forma sincrona y puede que no se llegue a ejecutar el codigo que tiene dentro
                if (spn_unidadMedida.getSelectedItemPosition() == 0){//La posicion 0 siempre debe ser la unidadMayor para calcular por "Unidad de venta (Solo se venden en unidadMayor)"
                    //Si la unidad de medida seleccionada es la primera se muestra el precio mayor (cajas, pack, etc.)
                    edt_precio.setText(politicaPrecioAdapter.getItem(1).getPrecioManejo()+"");
                }else {
                    //Si la unidad de medida seleccionada no es la primera se muestra el precio por unidad Menor
                    edt_precio.setText(politicaPrecioAdapter.getItem(1).getPrecioContenido()+"");
                }
                Log.d(TAG,"No se cumplió con la cantidadMinima de la politicaPrecio ("+cantidadMinima+") se seleccionó la politicaPorDefecto de sucursal");
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    boolean validarCampos(){
        if (idProducto.isEmpty()){
            Snackbar.make(findViewById(android.R.id.content), "El producto no es válido", Snackbar.LENGTH_LONG).show();
            return false;
        }
        if ((edt_precio.getText().toString()).equals("0.0")|| (edt_precio.getText().toString()).equals("0")) {
            if (settings_productoSinPrecio) {

            }else{
                edt_precio.setError("No tiene precio");
                return false;
            }
        }
        if (edt_cantidad.getText().toString().matches("")|| (Integer.parseInt(edt_cantidad.getText().toString())) == 0) {
            edt_cantidad.setError("Ingrese una cantidad");
            return false;
        }else{
            if (settings_validarStock) {
                int cant = Integer.parseInt(edt_cantidad.getText().toString());
                if(seleccSubUnidad == 1)
                {
                    if (cant > stockUnidadMenor) {
                        edt_cantidad.setError("No hay stock suficiente ("+(int)stockUnidadMenor+")");
                        return false;
                    }
                }else{
                    //Si la unidad de medida seleccionada es la primera se valida el stock por la unidad mayor
                    if (spn_unidadMedida.getSelectedItemPosition()==0) {
                        if (cant > stockUnidadMayor) {
                            edt_cantidad.setError("No hay stock suficiente ("+(int)stockUnidadMayor+")");
                            return false;
                        }
                    }else{
                        //Si la unidad de medida seleccionada no es la primera se valida el stock por la unidad menor
                        if (cant > stockUnidadMenor) {
                            edt_cantidad.setError("No hay stock suficiente ("+(int)stockUnidadMenor+")");
                            return false;
                        }
                    }
                }
            }
        }

        if (listaPoliticaPrecio.isEmpty()){
            Snackbar.make(findViewById(android.R.id.content), "El cliente no tiene una politica asignada", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    class async_getStockEnLinea extends AsyncTask<Boolean, Void, Boolean>{
        ProgressDialog pDialog;
        boolean agregarElProducto = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AgregarProductoActivity.this);
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(true);
            pDialog.setMessage("Consultando stock...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Boolean... booleans) {
            agregarElProducto = booleans[0];
            try{
                SoapManager soapManager = new SoapManager(getApplicationContext());
                soapManager.obtenerStockProductox(TablesHelper.Producto.ObtenerStockLinea,idProducto,numeroPedido);
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (!result){
                Snackbar.make(findViewById(android.R.id.content), "No se pudo obtener Stock en linea, se tomó el stock local", Snackbar.LENGTH_LONG).show();
            }
            //Para este punto el stockLocal ya está actualizado
            getStockLocal();

            if (agregarElProducto){
                agregarProducto();
            }
        }
    }
}
