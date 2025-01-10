package com.example.md06_clothes.Presenter;


import static com.itextpdf.kernel.pdf.PdfName.List;

import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.my_interface.IProduct;
import com.example.md06_clothes.my_interface.ProductView;

import java.util.List;

public class ProductPresenter implements IProduct {

    private Product product;
    private ProductView callback;

    public ProductPresenter(ProductView callback){
        product = new Product(this);
        this.callback = callback;
    }

    public void HandleGetDataProduct(){
        product.HandleGetDataProduct();
    }

    public void HandleGetWithIDProduct(String idproduct){
        product.HandleGetWithIDProduct(idproduct);
    }
    @Override
    public void OnSucess() {

    }

    @Override
    public void OnFail() {

    }

    @Override
    public void getDataProduct(String id, String tensp, long giatien, String hinhanh, String loaisp, String mota, java.util.List<SizeQuantity> sizes, long type, String chatlieu) {
        callback.getDataProduct(id,  tensp,  giatien, hinhanh,  loaisp,  mota, sizes,  type,  chatlieu);
    }

}
