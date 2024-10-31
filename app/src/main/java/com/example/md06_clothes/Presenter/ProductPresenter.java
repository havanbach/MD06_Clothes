package com.example.md06_clothes.Presenter;


import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.my_interface.IProduct;
import com.example.md06_clothes.my_interface.ProductView;

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
    public void getDataProduct(String id, String ten, Long gia, String hinhanh, String loaisp, String mota, Long soluong, String size, Long type, String chatlieu) {
        callback.getDataProduct(id, ten, gia, hinhanh, loaisp, mota, soluong, size, type, chatlieu);
    }
}
