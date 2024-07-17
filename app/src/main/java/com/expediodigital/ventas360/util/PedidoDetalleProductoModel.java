package com.expediodigital.ventas360.util;

import com.expediodigital.ventas360.model.PedidoDetalleModel;
import com.expediodigital.ventas360.model.ProductoModel;

public class PedidoDetalleProductoModel {
    ProductoModel productoModel;
    PedidoDetalleModel detalleOriginal;

    public PedidoDetalleProductoModel(ProductoModel productoModel, PedidoDetalleModel detalleOriginal) {
        this.productoModel = productoModel;
        this.detalleOriginal = detalleOriginal;
    }

    public ProductoModel getProductoModel() {
        return productoModel;
    }

    public void setProductoModel(ProductoModel productoModel) {
        this.productoModel = productoModel;
    }

    public PedidoDetalleModel getDetalleOriginal() {
        return detalleOriginal;
    }

    public void setDetalleOriginal(PedidoDetalleModel detalleOriginal) {
        this.detalleOriginal = detalleOriginal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PedidoDetalleProductoModel other = (PedidoDetalleProductoModel) o;
        if(getProductoModel().getIdProducto().equals(other.getProductoModel().getIdProducto()) &&
                getDetalleOriginal().getIdPromocion() == other.getDetalleOriginal().getIdPromocion()){
            return true;
        }
        else{
            return false;
        }
    }
    @Override
    public int hashCode() {
        int result = Integer.valueOf(productoModel.getIdProducto()) + detalleOriginal.getIdPromocion()*100000;
        return result;
    }
}

