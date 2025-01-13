package com.example.md06_clothes.Presenter;


import com.example.md06_clothes.Models.Giohang;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.my_interface.GioHangView;
import com.example.md06_clothes.my_interface.IGioHang;

import java.util.ArrayList;
import java.util.List;

public class GioHangPresenter implements IGioHang {
    private Giohang giohang;
    private GioHangView callback;

    public GioHangPresenter(GioHangView callback) {
        this.callback = callback;
        giohang = new Giohang(this);
    }


    public  void AddCart(String idsp,String size, Long soluong){
        giohang.AddCart(idsp, size,soluong);
    }

    public  void  HandlegetDataGioHang(){
        giohang.HandlegetDataGioHang();
    }
    public  void  HandleDeleteDataGioHang(String id){
        giohang.HandleDeleteDataGioHang(id);
    }

    public void HandleAddHoaDon(String ghichu, String ngaydat, String diachi, String hoten, String sdt, String phuongthuc, String tongtien, ArrayList<Product> arrayList) {
        giohang.HandleThanhToan(ghichu,ngaydat,diachi,hoten,sdt,phuongthuc,tongtien,arrayList);
    }
    @Override
    public void OnSucess() {
        callback.OnSucess();

    }

    @Override
    public void OnFail() {
        callback.OnFail();
    }

    @Override
    public void getDataSanPham(String id, String id_product, String tensp, Long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu) {
        callback.getDataSanPham(id,id_product,tensp,giatien,hinhanh,loaisp,mota,sizes,type,chatlieu);
    }

    public void HandlegetDataCTHD(String id,String uid) {
        giohang.HandleGetDataCTHD(id,uid);
    }
}
