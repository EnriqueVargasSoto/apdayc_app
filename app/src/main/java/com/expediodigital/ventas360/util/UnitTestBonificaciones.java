package com.expediodigital.ventas360.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOBonificacion;
import com.expediodigital.ventas360.DAO.DAOPedido;
import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;
import com.expediodigital.ventas360.model.PromBonificacionModel;
import com.expediodigital.ventas360.model.PromocionDetalleModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class UnitTestBonificaciones {

    private static final String TAG = "unit_test_bonificacion";

    public static void initTest(Context context){

        //private void calcularBonificaciones2() {
            //valores de prueba-------
            DAOPedido daoPedido = new DAOPedido(context);
            DAOBonificacion daoBonificacion = new DAOBonificacion(context);
            Gson gson = new Gson();
            String idVendedor = "6682";
            String idCliente = "5870";
            String numeroPedido = "191109026076"; //Obtenecemos el numeroPedido
            ArrayList<PedidoDetalleModel> pedidoDetalle = daoPedido.getListaProductoPedido(numeroPedido);//Obtenemos todo el detalle del pedido
            //-----

            ArrayList<HashMap<String,Object>> listaFinalCantidadesBonificadas = new ArrayList<>();
            ArrayList<PromocionDetalleModel> listaFinalPromocionesGeneradas = new ArrayList<>();
            for (PedidoDetalleModel pedidoDetalleOriginal: pedidoDetalle){
                //Esta lista guardará todas las bonificaciones obtenidas por el producto entrada, para al final tomar seleccionar las idóneos, dándole prioridad al que sea PorCliente*/
                ArrayList<HashMap<String,Object>> listaBonificacionesDelProducto = new ArrayList<>();
                ArrayList<PromBonificacionModel> listaPromocionesGeneradasDelProducto = new ArrayList<>();
//                Aqui si se obtiene mas de una promocion para el producto, tal vez con un distinct de entrada en PromocionDetalle, se puede quitar aquí
//                o sería mejor quitar cuando se genere la bonificacion ya que a lo mejor uno no cumpla su condición y no necesite quitarlo
                ArrayList<PromBonificacionModel> promocionesValidas = daoBonificacion.getPromocionesValidas(pedidoDetalleOriginal,idCliente,idVendedor,numeroPedido);
                for (PromBonificacionModel itemPromocion : promocionesValidas){

                    //Si se altera algo en el pedidoDetalleOriginal, es mejor crear aqui un nuevo objeto con sus datos
                    Log.e(TAG,"itemPromocion.getAcumulado() ==> "+itemPromocion.getDescripcion());

                    /* ----------------- ACUMULADOS -------------------*/
                    //para cada grupo, obtenemos la lista de productos y validamos
                    ArrayList<ProductoModel> productosPromocion = daoBonificacion.getProductosPromocion(itemPromocion.getIdGrupo());//Obtenemos todo el detalle del pedido

                    for(ProductoModel prod : productosPromocion)
                    {
                        if(prod.getIdProducto().equals(pedidoDetalleOriginal.getIdProducto())){
                            listaPromocionesGeneradasDelProducto.add(itemPromocion);

                            //obtenidos las promociones que aplican al producto, se evalua si cumple las condiciones y la cantidad a bonificar|
                            int cantidadBonificada = 0;

                            float desde = Float.valueOf(itemPromocion.getDesde());
                            float hasta = Float.valueOf(itemPromocion.getHasta());
                            int unidades = Integer.valueOf(itemPromocion.getUnidad());
                            float porcada = itemPromocion.getPorcada();
                            int actual = pedidoDetalleOriginal.getCantidad();
                            if( actual > desde && actual < hasta )
                            {
                                cantidadBonificada = (int)(actual/porcada)*unidades;
                            }

                            if (cantidadBonificada > 0){
                                Log.i(TAG,"cantidadBonificada > 0 "+cantidadBonificada +" de la promocion "+itemPromocion.getIdPromocion()+" agregando a listaPromocionesGeneradasDelProducto");
                            }

                            HashMap<String,Object> map = new HashMap<>();
                            map.put("idPromocion",itemPromocion.getIdPromocion());
                            map.put("itemPromocion",itemPromocion.getDescripcion());
                            map.put("cantidad",cantidadBonificada);
                            map.put("idProducto",prod.getIdPoliticaPrecio());
                            map.put("itemPedido",pedidoDetalleOriginal.getItem());//este campo no debería ser tomando en cuenta por el HashSet(eliminar repetidos) ya que con este campo todos los valores se vuelven diferencites, cuando en realidad no lo es. Sin embargo no se puede quitar este campo porque es importante para calcular despues, es por eso que se eliminan los repetidos con un for comparando solo los campos que se requieren
                            listaBonificacionesDelProducto.add(map);
                        }
                    }
                }


//
//                /*Validamos si las bonificaciones generados por el producto son mas de uno, a fin de darle prioridad a las que son desgnadas al cliente.*/
//
//                if (!listaBonificacionesDelProducto.isEmpty()){
//                    if (listaBonificacionesDelProducto.size()>1){
//                        Log.i(TAG,"listaBonificacionesDelProducto hay mas de una bonificacion generada por el producto "+pedidoDetalleOriginal.getIdProducto());
//                        ArrayList<HashMap<String,Object>> tempListaCantidadesBonificadas = new ArrayList<>();//Lista temporal para poder analizar
//                        ArrayList<PromocionDetalleModel> tempListaPromocionesGeneradas = new ArrayList<>();//Lista temporal para poder analizar
//
//                        for (int i = 0; i < listaPromocionesGeneradasDelProducto.size(); i++){
//                            if (listaPromocionesGeneradasDelProducto.get(i).getPorCliente() == 1){
//                                tempListaCantidadesBonificadas.add(listaBonificacionesDelProducto.get(i));
//                                tempListaPromocionesGeneradas.add(listaPromocionesGeneradasDelProducto.get(i));
//                                Log.i(TAG,"agregando "+gson.toJson(listaBonificacionesDelProducto.get(i))+" como promocion PorCliente");
//                            }
//                        }
//
//                        //Verificar si la lista de listaCantidadesBonificadas está vacía(si no hay promociones PorCliente), basta que exista una promocion PorCliente, ya no se tomarán en cuenta las bonificaciones generadas de forma general
//                        if (tempListaCantidadesBonificadas.isEmpty()){//Si la lista temporal está vacía, entonces significa que no hay ProCliente y se toma todas las bonificaciones generadas
//                            Log.i(TAG,"No hay promociones filtrados PorCliente=1, agregando todas las bonificaciones generadas...");
//                            tempListaCantidadesBonificadas.addAll(listaBonificacionesDelProducto);
//                            tempListaPromocionesGeneradas.addAll(listaPromocionesGeneradasDelProducto);
//                        }
//
//                        //Luego de obtener la lista temporal de las cantidades bonificadas ya sea PorCliente o todas las generales, se agrega la lista temporal a la lista final
//                        listaFinalCantidadesBonificadas.addAll(tempListaCantidadesBonificadas);
//                        listaFinalPromocionesGeneradas.addAll(tempListaPromocionesGeneradas);
//                    }else{
//                        //Si la lista solo tiene un elemento, no hay nada que analizar, asi que lo tomamos
//                        listaFinalCantidadesBonificadas.addAll(listaBonificacionesDelProducto);
//                        listaFinalPromocionesGeneradas.addAll(listaPromocionesGeneradasDelProducto);
//                    }
//                }
            }


//            //Si la cadena no está vacía quiere decir quer si hubo productos nulos y se debe mostrar el mensaje
//            if (!productosNulos.isEmpty()){
//                productosNulos = productosNulos.substring(0,productosNulos.length()-1);//Quitamos el ultimo coma ","
//                String mensaje = "No se encontraron los productos ("+productosNulos+") para bonificar";
//                Toast.makeText(context,mensaje, Toast.LENGTH_LONG).show();
//            }
//
//            //Registramos todos los productos bonificados al pedido
//            for (PedidoDetalleModel productoB : listaProductosBonificados){
//                int cantidadRegistrada = productoEstaRegistrado(productoB.getIdProducto(),productoB.getTipoProducto());
//                if (cantidadRegistrada != -1){
//                    //Si retorna distinto a -1 quiere decir que si está registrado, se debe tomar su cantidad y sumarle a la nueva cantidad
//                    productoB.setCantidad(productoB.getCantidad() + cantidadRegistrada);
//                    daoPedido.modificarItemDetallePedido(productoB);
//                }else{
//                    daoPedido.agregarItemPedidoDetalle(productoB);
//                }
//            }

            //Actualizar la lista detalle del pedido
            //pedidoDetalleFragment.mostrarListaProductos();


//
//            Log.e(TAG, "---------------------------------");
//            Log.d(TAG, "lista PromocionesGeneradas:\n"	+ gson.toJson(listaFinalPromocionesGeneradas));
//            Log.e(TAG, "---------------------------------");
//            Log.d(TAG, "lista CantidadesBonificadas Inicial:\n"	+ gson.toJson(listaFinalCantidadesBonificadas));
//            Log.e(TAG, "---------------------------------");
//        /*Log.d(TAG, "lista PromocionCompuesta:\n"	+ gson.toJson(listaPromocionCompuesta));
//        Log.e("", "---------------------------------");
//        Log.d(TAG, "lista CantidadesUsadas:\n"	+ gson.toJson(listaCantidadesUsadas));
//        Log.e("", "---------------------------------");
//        Log.d(TAG, "lista MontosUsados:\n"	+ gson.toJson(listaMontosUsados));
//        Log.e("", "---------------------------------");*/
//            //
//            /*Elminar repetidos con HashSet (todos los atributos del objeto son tomados en cuenta, es decir que todos los atributos del objeto deben ser necesariamente diferentes)*/
//        /*HashSet hashSet = new HashSet();//Creamos un objeto HashSet
//        hashSet.addAll(listaCantidadesBonificadas);//Lo cargamos con los valores de un array, esto hace que se quiten los repetidos
//        listaCantidadesBonificadas.clear();//Limpiamos la lista
//        listaCantidadesBonificadas.addAll(hashSet);//Agregamos los elementos sin repetir
//        Log.d(TAG, "lista CantidadesBonificadas Sin Repetir:\n"		+ gson.toJson(listaCantidadesBonificadas));*/
//
//            /*Elminar repetidos con For, no se puede usar el HashSet porque no queremos tomar en cuenta el atributo itemPedido al momento de eliminar repetidos*/
//            ArrayList<HashMap<String,Object>> newListaCantidadesBonificadas = new ArrayList<>();
//            for (HashMap<String,Object> item : listaFinalCantidadesBonificadas){
//                boolean isInList = false;
//                for (HashMap<String,Object> itemUnico : newListaCantidadesBonificadas){
//                    if (    (int) item.get("idPromocion") == (int) itemUnico.get("idPromocion") &&
//                            (int) item.get("itemPromocion") == (int) itemUnico.get("itemPromocion") &&
//                            (int) item.get("cantidad") == (int) itemUnico.get("cantidad") &&
//                            item.get("idProducto").toString().equals(itemUnico.get("idProducto").toString()) ){
//                        //Si el item es totalmente igual(por los campos igualados) a alguno de los items dentro de este for, entonces se cambia el flag
//                        isInList = true;
//                    }
//                }
//                if (!isInList)
//                    newListaCantidadesBonificadas.add(item);
//            }
//            listaFinalCantidadesBonificadas.clear();
//            listaFinalCantidadesBonificadas.clear();//Limpiamos la lista
//            listaFinalCantidadesBonificadas.addAll(newListaCantidadesBonificadas);//Agregamos los elementos sin repetir
//            Log.d(TAG, "lista CantidadesBonificadas Sin Repetir:\n"		+ gson.toJson(listaFinalCantidadesBonificadas));
//
//
//        /*Con la lista sin repetidos, recien se puede validar cada uno de los elementos
//         La lista podría contener varias promociones con el mismo ID y con cantidades bonificadas distintas,
//         esto solo pasa con los acumulados ya que se puede cumplir su condicion varias veces.
//         Para solucionar esto analizamos todos los las bonificaciones a fin de determinar solo el mayor del cantidad
//         bonificada por cada promocion, es decir que si hay varias cantidades por acumulado, solo se tomará y será agregado
//         a la lista de productos a bonificar en el pedido finalmente
//        */
//
//            ArrayList<PedidoDetalleModel> listaProductosBonificados = new ArrayList<>();
//            String productosNulos = "";
//
//            //Primer for es sólo para actualizar las cantidades luego de ser multiplicadas por la compra (en caso se requiera)
//            for (int i = 0; i < listaFinalCantidadesBonificadas.size(); i++) {
//                int idPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("idPromocion");
//                int itemPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("itemPromocion");
//                int cantidadx = (int) listaFinalCantidadesBonificadas.get(i).get("cantidad");
//                int itemPedidox = (int) listaFinalCantidadesBonificadas.get(i).get("itemPedido");
//                String idProductox = listaFinalCantidadesBonificadas.get(i).get("idProducto").toString();
//                boolean esElMayor = true;
//
//                //Antes de agregar la cantidad al pedido, verificar si la promociones debe ser multiplicado por la cantidad de compra total del pedido (UnidadMayor)
//                //Esto se debe hacer antes de obtener el mayor también, ya que aquí es donde habrá diferencias entre las promociones escalables, al final el mayor de ellos deberá registrarse en el pedido
//                if (daoPromocion.isPromocionMultiplicadoPorCompra(idPromocionx, itemPromocionx)) {
//                    int cantidadProductosPromo = daoPedido.getCantidadProductosDePromocion(numeroPedido, idPromocionx, itemPromocionx, itemPedidox);
//                    Log.w(TAG, "multiplicando " + cantidadx + " x " + cantidadProductosPromo + " productos en el pedido...");
//                    cantidadx = cantidadx * cantidadProductosPromo;
//                    listaFinalCantidadesBonificadas.get(i).put("cantidad", cantidadx);
//                }
//            }
//            Log.d(TAG, "lista CantidadesBonificadas Sin Repetir luego de update cantidadx:\n"		+ gson.toJson(listaFinalCantidadesBonificadas));
//
//            //Segundo for ya es para obtener la bonificación mayor (con las cantidades ya multiplicadas), para este momento no deben existir bonificaciones repetidas(mismas cantidades, esto pasa con acumulados), de lo contrario no se bonificará nada
//            for (int i = 0; i < listaFinalCantidadesBonificadas.size(); i++){
//                int idPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("idPromocion");
//                int itemPromocionx = (int) listaFinalCantidadesBonificadas.get(i).get("itemPromocion");
//                int cantidadx =  (int) listaFinalCantidadesBonificadas.get(i).get("cantidad");
//                int itemPedidox =  (int) listaFinalCantidadesBonificadas.get(i).get("itemPedido");
//                String idProductox = listaFinalCantidadesBonificadas.get(i).get("idProducto").toString();
//                boolean esElMayor = true;
//
//            /*Por promocion solo debe haber una condicion resultante, asi hayan muchos items dentro de la promoción, los items deben ser usados para promocinoes escalables,
//            donde al final se obtenga solo uno o tambien sirve para hacer multilpes bonificaciones.
//            La comprobación de la bonificación mayor se hace sin tomar en cuenta el item, para asi descargar los items con bonificaciones menores, siempre y cuando se trate del mismo producto bonificado
//            Si los productos que se van a boinficar son distintos, entonces no es una promocion escalable, sino una promocion con bonificacion multiple*/
//                for (int j = 0; j < listaFinalCantidadesBonificadas.size(); j++){
//                    if (j != i){
//                        /*todo Si son de la misma promocion, y con la misma salida quiere decir que son escalables y se debe elegir el mayor.*/
//                        if (idPromocionx == (int)listaFinalCantidadesBonificadas.get(j).get("idPromocion") && idProductox.equals(listaFinalCantidadesBonificadas.get(j).get("idProducto").toString())) {
//                            Log.w(TAG, "if " + cantidadx + " > " + (int) listaFinalCantidadesBonificadas.get(j).get("cantidad"));
//                            if (cantidadx > (int) listaFinalCantidadesBonificadas.get(j).get("cantidad")) {
//                                Log.w(TAG, "true");
//                                esElMayor = true;
//                            } else {
//                                Log.w(TAG, "false");
//                                esElMayor = false;
//                                //Basta que no sea mayor que alguno para descartarlo como el Mayor
//                                break;//El break rompe el for
//                            }
//                        }
//                        /*todo Si no son de la misma salida, quiere decir que son varias bonificaciones, será tomado como mayor y será agregado a lista de productos bonificados.*/
//                    }
//                }
//
//                if (esElMayor){
//
//                    Log.e(TAG,"El producto "+idProductox+" es el mayor en la posicion "+i+" con cantidad "+cantidadx);
//                    ProductoModel productoModel = daoProducto.getProducto(idProductox);
//                    if (productoModel != null){
//                        PedidoDetalleModel pedidoDetalleBonificacion = new PedidoDetalleModel();
//                        pedidoDetalleBonificacion.setNumeroPedido(numeroPedido);
//                        pedidoDetalleBonificacion.setIdProducto(idProductox);
//                        pedidoDetalleBonificacion.setPrecioBruto(0.0);
//                        pedidoDetalleBonificacion.setPrecioNeto(0.0);
//                        pedidoDetalleBonificacion.setCantidad(cantidadx);
//                        pedidoDetalleBonificacion.setPesoNeto(productoModel.getPeso() * cantidadx);
//                        //pedidoDetalleBonificacion.setIdUnidadMedida(productoModel.getIdUnidadMenor());
//                        pedidoDetalleBonificacion.setIdPoliticaPrecio("0");
//                        pedidoDetalleBonificacion.setPercepcion(0.0);
//                        pedidoDetalleBonificacion.setISC(0.0);
//                        pedidoDetalleBonificacion.setTipoProducto(ProductoModel.TIPO_BONIFICACION);//Producto tipo [V]venta [B]bonificacion
//                        listaProductosBonificados.add(pedidoDetalleBonificacion);
//                    }else{
//                        //Se concatenan los codigos de los productos bonificados que no se encuentren. Para mostrar un mensaje después
//                        productosNulos += idProductox+",";
//                    }
//                }
//
//            }
//
//            //Si la cadena no está vacía quiere decir quer si hubo productos nulos y se debe mostrar el mensaje
//            if (!productosNulos.isEmpty()){
//                productosNulos = productosNulos.substring(0,productosNulos.length()-1);//Quitamos el ultimo coma ","
//                String mensaje = "No se encontraron los productos ("+productosNulos+") para bonificar";
//                Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_LONG).show();
//            }
//
//            //Registramos todos los productos bonificados al pedido
//            for (PedidoDetalleModel productoB : listaProductosBonificados){
//                int cantidadRegistrada = productoEstaRegistrado(productoB.getIdProducto(),productoB.getTipoProducto());
//                if (cantidadRegistrada != -1){
//                    //Si retorna distinto a -1 quiere decir que si está registrado, se debe tomar su cantidad y sumarle a la nueva cantidad
//                    productoB.setCantidad(productoB.getCantidad() + cantidadRegistrada);
//                    daoPedido.modificarItemDetallePedido(productoB);
//                }else{
//                    daoPedido.agregarItemPedidoDetalle(productoB);
//                }
//            }
//
//            //Actualizar la lista detalle del pedido
//            pedidoDetalleFragment.mostrarListaProductos();

    }



}
