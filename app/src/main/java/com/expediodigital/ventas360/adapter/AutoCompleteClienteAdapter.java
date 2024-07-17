package com.expediodigital.ventas360.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.ClienteModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Meza.Hinostroza.Robin@gmail.com
 */

public class AutoCompleteClienteAdapter extends ArrayAdapter<ClienteModel> {

    private final List<ClienteModel> clientes;
    private List<ClienteModel> clientesFiltrados = new ArrayList<>();

    public AutoCompleteClienteAdapter(@NonNull Context context, @NonNull List<ClienteModel> clientes) {
        super(context, 0, clientes);
        this.clientes=clientes;
    }

    @Override
    public int getCount() {
        return clientesFiltrados.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        //Creamos un nuevo filtro que contiene la lógica de búsqueda
        return new ClientesFilter(this, clientes);
    }

    //Método importante para que el listener del adaptador devuelva el item desde la lista filtrada y no desde la lista base
    @Nullable
    @Override
    public ClienteModel getItem(int position) {
        return clientesFiltrados.get(position);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        //Inflar la vista customizada para la fila
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //Se puede usar getContext() directamente, pero utilizo parent.getContext() para tomar el contexto del padre y también su tema
        //De lo contrario el inflater se crearía basandose en su propio tema, puede que sea Dark o Light pero no será el mismo de quien lo invocó


        if(convertView==null){
            //convertView = inflater.inflate(R.layout.item_busqueda_cliente, null);
            convertView = inflater.inflate(R.layout.item_busqueda_cliente, parent, false);
            holder = new ViewHolder();

            holder.tv_cliente = convertView.findViewById(R.id.tv_cliente);
            holder.tv_dniruc = convertView.findViewById(R.id.tv_rucDni);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Obtener la data del item de la lista filtrada
        ClienteModel cliente = clientesFiltrados.get(position);

        //Seteamos los valores a las vistas
        holder.tv_cliente.setText(cliente.getRazonSocial());
        holder.tv_dniruc.setText(cliente.getRucDni());
        return convertView;
    }


    //La clase viewHolder ayuda a agilizar la vista, ya que guarda o mantiene los componentes y los va seteando en lugar de crear cada componente para cada fila
    class ViewHolder{
        TextView tv_cliente;
        TextView tv_dniruc;
    }

    //Creamos una clase que extiende de la clase Filter para implementar la lógica de busqueda (sensitiva)
    class ClientesFilter extends Filter{
        AutoCompleteClienteAdapter autoCompleteClienteAdapter;
        List<ClienteModel> origialList;
        List<ClienteModel> filteredList;

        private ClientesFilter(AutoCompleteClienteAdapter autoCompleteClienteAdapter, List<ClienteModel> origialList){
            super();
            this.autoCompleteClienteAdapter = autoCompleteClienteAdapter; //Obtenemos el adapter principal (desde donde se llama al Filtro)
            this.origialList = origialList;
            this.filteredList = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence textoBusqueda) {
            filteredList.clear();
            FilterResults results = new FilterResults();

            if (textoBusqueda == null || textoBusqueda.length() == 0){
                //Si el texto es nulo o vacio la lista Filtrada toma todos los valores de la lista original
                filteredList.addAll(origialList);
            }else{
                //El texto a buscar tiene que ser tratado convitiendolo a String y pasándolo a minúsculas y quitando espacios
                String textoTratado = textoBusqueda.toString().toLowerCase().trim();

                //Logica de filtrado (Sensitivo)
                for (ClienteModel clienteModel : origialList){ //Por cada cliente de la lista original
                    if (TextUtils.isDigitsOnly(textoTratado)){
                        //De lo contrario se filtra por la razon social
                        if (clienteModel.getIdCliente().contains(textoTratado)){
                            //Si el ID del cliente (tratado) contiene el texto a buscar (tratado)
                            filteredList.add(clienteModel);
                            //Se agrega a la lista filtrada
                        }
                    }else{
                        //De lo contrario se filtra por la razon social
                        if (clienteModel.getRazonSocial().toLowerCase().contains(textoTratado)){
                            //Si el nombre del cliente (tratado) contiene el texto a buscar (tratado)
                            filteredList.add(clienteModel);
                            //Se agrega a la lista filtrada
                        }
                    }


                }
            }
            //Por último llenamos la lista filtrada al resultado (FilterResults)
            results.values = filteredList;
            results.count = filteredList.size();
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //Ahora limpipamos la lista de clientes filtrados del adapter principal
            autoCompleteClienteAdapter.clientesFiltrados.clear();
            //Agregamos todos los resultados obtenidos tras aplicar la lógica de búsqueda
            autoCompleteClienteAdapter.clientesFiltrados.addAll( (List) results.values);
            //Notificamos al adapter que la data ha tenido cambios (Para que la vista se actualice y el usuario pueda ver los cambios)
            autoCompleteClienteAdapter.notifyDataSetChanged();
        }
    }

}
