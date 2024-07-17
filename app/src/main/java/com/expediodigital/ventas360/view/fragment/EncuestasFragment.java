package com.expediodigital.ventas360.view.fragment;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expediodigital.ventas360.DAO.DAOEncuesta;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.adapter.RecyclerViewEncuestaAdapter;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.util.Util;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EncuestasFragment extends Fragment {
    private RecyclerView recycler_encuestas;
    private RecyclerViewEncuestaAdapter adapter;
    ArrayList<EncuestaDetalleModel> listaEncuestaDetalle = new ArrayList<>();
    DAOEncuesta daoEncuesta;

    public EncuestasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View view = inflater.inflate(R.layout.fragment_encuestas, container, false);
        setHasOptionsMenu(true);
        Util.actualizarToolBar("Encuestas",false,getActivity());
        daoEncuesta = new DAOEncuesta(getActivity());
        recycler_encuestas = view.findViewById(R.id.recycler_encuestas);

        //Adapter y divider

        adapter = new RecyclerViewEncuestaAdapter(listaEncuestaDetalle, getActivity());
        recycler_encuestas.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        recycler_encuestas.setAdapter(adapter);
        refreshLista();

        return view;
    }

    public void refreshLista() {
        listaEncuestaDetalle.clear();
        listaEncuestaDetalle.addAll(daoEncuesta.getListaEncuestaDetalle());
        adapter.notifyDataSetChanged();
    }

}
