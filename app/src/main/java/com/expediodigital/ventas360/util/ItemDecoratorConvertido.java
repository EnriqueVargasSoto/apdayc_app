package com.expediodigital.ventas360.util;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.IntegerRes;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Kevin Robin Meza Hinostroza on 2/04/2018.
 * Expedio Digital
 * kevin.meza@expediodigital.com
 */

public class ItemDecoratorConvertido extends RecyclerView.ItemDecoration{
    public static final int TODOS_LADOS = 1;
    public static final int SUPERIOR_INFERIOR = 2;
    public static final int LATERALES = 3;
    public static final int LATERALES_Y_SUPERIOR = 4;
    public static final int LATERALES_E_INFERIOR = 5;
    public static final int TODOS_LADOS_MITAD = 6;

    private int mItemOffset;//La medida en pixeles del espaciado
    private int tipo;

    public ItemDecoratorConvertido(Context context, @IntegerRes int integerResId, int tipo){
        int itemOffSetDp = context.getResources().getInteger(integerResId);
        this.mItemOffset = convertToPixel(itemOffSetDp,context.getResources().getDisplayMetrics());
        this.tipo = tipo;
    }

    public int convertToPixel(int dp, DisplayMetrics displayMetrics){
        //Se obtiene la densidad en dp
        return dp * (displayMetrics.densityDpi/160);//Formula para convertir dp en pixels
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        //Se indica el espacio para cada lado de la forma (left,top,right,bottom)
        //En este caso la misma medida en pixeles para todos los lados
        switch (tipo){
            case TODOS_LADOS:
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
                break;
            case SUPERIOR_INFERIOR:
                outRect.set(0, mItemOffset, 0, mItemOffset);
                break;
            case LATERALES:
                outRect.set(mItemOffset, 0, mItemOffset, 0);
                break;
            case LATERALES_Y_SUPERIOR:
                outRect.set(mItemOffset, mItemOffset, mItemOffset, 0);
                break;
            case LATERALES_E_INFERIOR:
                outRect.set(mItemOffset, 0, mItemOffset, mItemOffset);
                break;
            case TODOS_LADOS_MITAD:
                outRect.set(mItemOffset, mItemOffset/2, mItemOffset, mItemOffset/2);
        }
    }
}
