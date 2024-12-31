package com.example.md06_clothes.my_interface;

import com.example.md06_clothes.Models.SizeQuantity;

import java.util.List;

public interface GioHangView {
    void OnSucess();

    void OnFail();

    void getDataSanPham(String id, String id_product, String tensp, Long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu);
}
