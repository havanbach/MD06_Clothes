package com.example.md06_clothes.my_interface;

import com.example.md06_clothes.Models.SizeQuantity;

import java.util.List;

public interface IProduct {
    void OnSucess();

    void OnFail();

    void getDataProduct(String id, String tensp, long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, long type, String chatlieu);
}
