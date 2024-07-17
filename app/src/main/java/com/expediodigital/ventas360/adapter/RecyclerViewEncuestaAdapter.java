package com.expediodigital.ventas360.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.expediodigital.ventas360.R;
import com.expediodigital.ventas360.model.EncuestaDetalleModel;
import com.expediodigital.ventas360.view.EncuestasClientesActivity;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Kevin Robinson Meza Hinostroza on febrero 2018.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

public class RecyclerViewEncuestaAdapter extends RecyclerView.Adapter<RecyclerViewEncuestaAdapter.ClienteViewHolder> {
    public static final long TRANSITION_TIME = 500;
    public static final String TAG = "RecyclerViewEncuestaAdapter";
    private ArrayList<EncuestaDetalleModel> listaEncuestaDetalle;
    private Activity activity;

    public RecyclerViewEncuestaAdapter(ArrayList<EncuestaDetalleModel> listaEncuestaDetalle, Activity activity) {
        this.listaEncuestaDetalle = listaEncuestaDetalle;
        this.activity = activity;
    }

    @Override
    public RecyclerViewEncuestaAdapter.ClienteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_encuesta, parent, false);
        return new RecyclerViewEncuestaAdapter.ClienteViewHolder(view);
    }
    //Suprimir el warning, ayuda tambien para el proguard
    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerViewEncuestaAdapter.ClienteViewHolder holder, final int position) {
        final EncuestaDetalleModel model = listaEncuestaDetalle.get(position);

        holder.tv_descripcionEncuesta.setText(model.getDescripcionEncuesta());
        holder.tv_tipoEncuesta.setText(model.getTipoEncuesta());
        holder.tv_fechaEncuesta.setText("Del " + model.getFechaInicio() + " al " + model.getFechaFin());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity.getApplicationContext(), EncuestasClientesActivity.class);
                intent.putExtra("idEncuesta",model.getIdEncuesta());
                intent.putExtra("idEncuestaDetalle",model.getIdEncuestaDetalle());
                intent.putExtra("descripcionEncuesta",model.getDescripcionEncuesta());
                intent.putExtra("tipoEncuesta",model.getTipoEncuesta());


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Transition transition = new Slide(Gravity.LEFT);
                    //transition.setDuration(TRANSITION_TIME);//Cuanto se demoran en llegar a este activity
                    transition.setInterpolator(new DecelerateInterpolator());//a veces las animaciones empiezan lento y luego se aceleran, para que el movimiento sea uniforme se agrega este interpolator
                    activity.getWindow().setExitTransition(transition);
                    activity.startActivity(
                            intent,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle()
                    );
                }else{
                    activity.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() { return listaEncuestaDetalle.size();  }

    public void setFilter(List<EncuestaDetalleModel> listaEncuestaDetalleFiltrada) {
        this.listaEncuestaDetalle = new ArrayList<>();
        this.listaEncuestaDetalle.addAll(listaEncuestaDetalleFiltrada);
        notifyDataSetChanged();
    }

    public class ClienteViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_descripcionEncuesta;
        private TextView tv_tipoEncuesta;
        private TextView tv_fechaEncuesta;

        public ClienteViewHolder(View itemView) {
            super(itemView);
            tv_descripcionEncuesta = itemView.findViewById(R.id.tv_descripcionEncuesta);
            tv_tipoEncuesta = itemView.findViewById(R.id.tv_tipoEncuesta);
            tv_fechaEncuesta = itemView.findViewById(R.id.tv_fechaEncuesta);
        }
    }
}
