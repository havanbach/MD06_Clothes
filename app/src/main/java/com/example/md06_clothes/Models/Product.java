package com.example.md06_clothes.Models;

import androidx.annotation.NonNull;

import com.example.md06_clothes.my_interface.IProduct;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {

    private String id;
    private String idsp;
    private String tensp;
    private long giatien;
    private String hinhanh;
    private String loaisp;
    private String mota;
    private List<SizeQuantity> sizes; // Danh sách kích thước và số lượng
    private long type;
    private String chatlieu;

    private IProduct callback;
    private FirebaseFirestore db;

    public Product() {
        this.sizes = new ArrayList<>();
    }

    public Product(IProduct callback) {
        this.callback = callback;
        db = FirebaseFirestore.getInstance();
        this.sizes = new ArrayList<>();
    }

    public Product(String id, String idsp, String tensp, long giatien, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, long type, String chatlieu) {
        this.id = id;
        this.idsp = idsp;
        this.tensp = tensp;
        this.giatien = giatien;
        this.hinhanh = hinhanh;
        this.loaisp = loaisp;
        this.mota = mota;
        this.sizes = sizes;
        this.type = type;
        this.chatlieu = chatlieu;
    }

    public Product(String id, String ten, Long gia, String hinhanh, String loaisp, String mota, List<SizeQuantity> sizes, Long type, String chatlieu) {
        this.id = id;
        this.tensp = ten;
        this.giatien = gia;
        this.hinhanh = hinhanh;
        this.loaisp = loaisp;
        this.mota = mota;
        this.sizes = sizes;
        this.type = type;
        this.chatlieu = chatlieu;
    }

    public Product(String ten) {
        this.tensp = ten;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdsp() {
        return idsp;
    }

    public void setIdsp(String idsp) {
        this.idsp = idsp;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public long getGiatien() {
        return giatien;
    }

    public void setGiatien(long giatien) {
        this.giatien = giatien;
    }

    public String getHinhanh() {
        return hinhanh;
    }

    public void setHinhanh(String hinhanh) {
        this.hinhanh = hinhanh;
    }

    public String getLoaisp() {
        return loaisp;
    }

    public void setLoaisp(String loaisp) {
        this.loaisp = loaisp;
    }

    public String getMota() {
        return mota;
    }

    public void setMota(String mota) {
        this.mota = mota;
    }

    public List<SizeQuantity> getSizes() {
        return sizes;
    }

    public void setSizes(List<SizeQuantity> sizes) {
        this.sizes = sizes;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public String getChatlieu() {
        return chatlieu;
    }

    public void setChatlieu(String chatlieu) {
        this.chatlieu = chatlieu;
    }

    public void HandleGetDataProduct() {
        db.collection("SanPham")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0) {
                            for (QueryDocumentSnapshot d : queryDocumentSnapshots) {
                                List<SizeQuantity> sizes = new ArrayList<>();
                                List<Map<String, Object>> sizesData = (List<Map<String, Object>>) d.get("sizes");
                                if (sizesData != null) {
                                    for (Map<String, Object> size : sizesData) {
                                        sizes.add(new SizeQuantity((String) size.get("size"), ((Long) size.get("soluong")).intValue()));
                                    }
                                }

                                callback.getDataProduct(
                                        d.getId(),
                                        d.getString("tensp"),
                                        d.getLong("giatien"),
                                        d.getString("hinhanh"),
                                        d.getString("loaisp"),
                                        d.getString("mota"),
                                        sizes,
                                        d.getLong("type"),
                                        d.getString("chatlieu")
                                );
                            }
                        }
                    }
                });
    }

    public void HandleGetWithIDProduct(String idproduct) {
        db.collection("SanPham").document(idproduct).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot d) {
                List<SizeQuantity> sizes = new ArrayList<>();
                List<Map<String, Object>> sizesData = (List<Map<String, Object>>) d.get("sizes");
                if (sizesData != null) {
                    for (Map<String, Object> size : sizesData) {
                        sizes.add(new SizeQuantity((String) size.get("size"), ((Long) size.get("soluong")).intValue()));
                    }
                }

                callback.getDataProduct(
                        idproduct,
                        d.getString("tensp"),
                        d.getLong("giatien"),
                        d.getString("hinhanh"),
                        d.getString("loaisp"),
                        d.getString("mota"),
                        sizes,
                        d.getLong("type"),
                        d.getString("chatlieu")
                );
            }
        });
    }
}
