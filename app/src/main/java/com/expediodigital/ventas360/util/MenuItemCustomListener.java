package com.expediodigital.ventas360.util;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Kevin Robinson Meza Hinostroza on julio 2017.
 * Expedio Digital
 * Kevin.Meza@expediodigital.com
 */

/** La clase abstracta permite declarar métodos pero no implementarlos, esto para que la clase que la herede o la cree se encargue de hacerlo
 * a su manera, es decir que el método abstracto de esta clase podrá ser utilizado de la forma que se necesite segun quien lo cree.
 * Esta clase se comporta como una clase convencional, con sus propios atributos y métodos, solo que podrá declarar metodos abstractos **/
/*A la vez se está implementando dos métodos onClick y onLongClick en esta clase para que también tenga esa funcionalidad*/
public abstract class MenuItemCustomListener implements View.OnClickListener, View.OnLongClickListener {
    private String hint;
    private View view;

    /** En el método constructor obtenemos un String y la vista con la que se va a trabar, asignandole a la vez
     * el OnClickListener y OnLongClickListener, es decir le estamos otorgando esas funcionalidades **/
    public MenuItemCustomListener(View view, String hint) {
        this.view = view;
        this.hint = hint;
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public void actualizarTextView(TextView textView, final int numero) {
        if (textView == null) return;
        if (numero == 0)
            textView.setVisibility(View.INVISIBLE);
        else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(numero));
        }
    }

    /** onClick se ejecutará de distinta manera dependiendo ya de las necesidades de donde creen esta clase
     * es por eso que es declarado como abstract, obligando a su vez que las clases hijas sean las encargadas
     * de darle funcionalidad, es decir que necesariamente tendrán que implementar este método porque les marcará error **/
    @Override
    public abstract void onClick(View v);

    /** onLongClick se ejecutará de la misma manera siempre es por eso que es está declarado convencionalmente y no como un abstract**/
    @Override
    public boolean onLongClick(View v) {
        /** Al mentener presionado la vista se debe mostrar un toast debajo de la misma vista. *
         *Similar a cuando mantienes presionado un item del menu del actionbar **/
        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        view.getLocationOnScreen(screenPos);
        view.getWindowVisibleDisplayFrame(displayFrame);
        final Context context = view.getContext();
        final int width = view.getWidth();
        final int height = view.getHeight();
        final int midy = screenPos[1] + height / 2;
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        Toast cheatSheet = Toast.makeText(context, hint, Toast.LENGTH_SHORT);
        if (midy < displayFrame.height()) {
            cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT,
                    screenWidth - screenPos[0] - width / 2, height);
        } else {
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
        }
        cheatSheet.show();
        return true;
    }
}
