package com.expediodigital.ventas360.view.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.expediodigital.ventas360.DAO.DAOCliente;
import com.expediodigital.ventas360.DAO.DAOConfiguracion;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.Ventas360App;
import com.expediodigital.ventas360.model.ClienteModel;
import com.expediodigital.ventas360.view.DetalleClienteActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class DatosClienteFragment extends Fragment {
    private TextView tv_razonSocial,/*tv_establecimiento,*/tv_codigo,tv_dniruc,tv_subGiro,tv_numeroOrden,tv_correo,tv_direccion,tv_direccionFiscal,tv_ruta,tv_modulo,tv_segmento,tv_cluster,tv_limiteCredito,tv_nroExhibidores,tv_nroPuertasFrio;
    Ventas360App ventas360App;

    public DatosClienteFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_datos_cliente, container, false);

        tv_razonSocial      = view.findViewById(R.id.tv_razonSocial);
//        tv_establecimiento      = view.findViewById(R.id.tv_razonSocial);
        tv_codigo           = view.findViewById(R.id.tv_codigo);
        tv_dniruc           = view.findViewById(R.id.tv_dniruc);
        tv_subGiro          = view.findViewById(R.id.tv_subGiro);
        tv_numeroOrden      = view.findViewById(R.id.tv_numeroOrden);
        tv_correo           = view.findViewById(R.id.tv_correo);
        tv_direccion        = view.findViewById(R.id.tv_direccion);
        tv_direccionFiscal  = view.findViewById(R.id.tv_direccionFiscal);
        tv_ruta             = view.findViewById(R.id.tv_ruta);
        tv_modulo           = view.findViewById(R.id.tv_modulo);
        tv_segmento         = view.findViewById(R.id.tv_segmento);
        tv_cluster          = view.findViewById(R.id.tv_cluster);
        tv_limiteCredito    = view.findViewById(R.id.tv_limiteCredito);
        tv_nroExhibidores   = view.findViewById(R.id.tv_nroExhibidores);
        tv_nroPuertasFrio   = view.findViewById(R.id.tv_nroPuertasFrio);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ventas360App    = (Ventas360App) getActivity().getApplicationContext();
        DAOCliente daoCliente = new DAOCliente(getActivity());
        DAOConfiguracion daoConfiguracion = new DAOConfiguracion(getActivity());

        try{
            String idCliente = ((DetalleClienteActivity)getActivity()).getIdCliente();
            ClienteModel clienteModel = daoCliente.getDetalleCliente(idCliente);

            tv_razonSocial.setText(clienteModel.getRazonSocial());
//            tv_establecimiento.setText(clienteModel.getRazonSocial());
            tv_codigo.setText(clienteModel.getIdCliente());
            tv_dniruc.setText(clienteModel.getRucDni());
            tv_subGiro.setText(clienteModel.getSubGiro());
            tv_numeroOrden.setText(String.valueOf(clienteModel.getOrden()));
            tv_correo.setText(clienteModel.getCorreo());
            tv_direccion.setText(clienteModel.getDireccion());
            tv_direccionFiscal.setText(clienteModel.getDireccionFiscal());
            tv_ruta.setText(daoConfiguracion.getRutaVendedor(ventas360App.getIdEmpresa(),ventas360App.getIdSucursal(),ventas360App.getIdVendedor()));
            tv_modulo.setText(clienteModel.getIdModulo());
            tv_segmento.setText(clienteModel.getIdSegmento());
            tv_cluster.setText(clienteModel.getIdCluster());
            tv_nroExhibidores.setText(String.valueOf(clienteModel.getNroExhibidores()));
            tv_nroPuertasFrio.setText(String.valueOf(clienteModel.getNroPuertasFrio()));

            if (clienteModel.getLimiteCredito()>0)
                tv_limiteCredito.setText(String.valueOf(clienteModel.getLimiteCredito()));
            else
                tv_limiteCredito.setText("Sin crédito");
        }catch (Exception e){
            Toast.makeText(getActivity(),"Error:"+e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
