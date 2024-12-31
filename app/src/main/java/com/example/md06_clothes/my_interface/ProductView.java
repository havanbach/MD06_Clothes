package com.example.md06_clothes.my_interface;

import com.example.md06_clothes.Models.SizeQuantity;

public interface ProductView {
    void OnSucess();

    void OnFail();

    void getDataProduct(String id, String ten, Long gia, String hinhanh, String loaisp, String mota, java.util.List<SizeQuantity> sizes, Long type, String chatlieu);
}
