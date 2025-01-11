package com.example.md06_clothes.Models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.md06_clothes.my_interface.IGioHang;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Giohang {
    private String id;
    private String id_product;
    private List<SizeQuantity> sizes; // Danh sách size và số lượng

    private IGioHang callback;
    private FirebaseFirestore db;

    public Giohang() {
    }

    public Giohang(String id_product, List<SizeQuantity> sizes) {
        this.id_product = id_product;
        this.sizes = sizes;
    }

    public Giohang(String id, String id_product, List<SizeQuantity> sizes) {
        this.id = id;
        this.id_product = id_product;
        this.sizes = sizes;
    }

    public Giohang(IGioHang callback) {
        this.callback = callback;
        db = FirebaseFirestore.getInstance();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_product() {
        return id_product;
    }

    public void setId_product(String id_product) {
        this.id_product = id_product;
    }

    public List<SizeQuantity> getSizes() {
        return sizes;
    }

    public void setSizes(List<SizeQuantity> sizes) {
        this.sizes = sizes;
    }

    // Thanh toán
    public void HandleThanhToan(String ghichu, String ngaydat, String diachi, String hoten, String sdt, String phuongthuc, String tongtien, ArrayList<Product> arrayList) {
        HashMap<String, Object> order = new HashMap<>();
        order.put("ghichu", ghichu);
        order.put("ngaydat", ngaydat);
        order.put("diachi", diachi);
        order.put("sdt", sdt);
        order.put("hoten", hoten);
        order.put("phuongthuc", phuongthuc);
        order.put("tongtien", tongtien);
        order.put("trangthai", 1);
        order.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());

        db.collection("HoaDon")
                .add(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (Product product : arrayList) {
                            HashMap<String, Object> orderDetail = new HashMap<>();
                            orderDetail.put("id_hoadon", task.getResult().getId());
                            orderDetail.put("id_product", product.getIdsp());
                            orderDetail.put("sizes", product.getSizes()); // Lưu toàn bộ sizes

                            db.collection("ChitietHoaDon").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("ALL").add(orderDetail)
                                    .addOnCompleteListener(orderTask -> {
                                        if (orderTask.isSuccessful()) {
                                            callback.OnSucess();
                                        } else {
                                            callback.OnFail();
                                        }
                                    });
                        }
                    } else {
                        callback.OnFail();
                    }
                });
    }

    // Thêm sản phẩm vào giỏ hàng
    // Thêm sản phẩm vào giỏ hàng
    public void AddCart(String idsp, String size, long soluong) {
        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL").whereEqualTo("id_product", idsp)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            List<HashMap<String, Object>> existingSizes = (List<HashMap<String, Object>>) doc.get("sizes");
                            boolean sizeFound = false;

                            if (existingSizes != null) {
                                // Check if the size already exists
                                for (HashMap<String, Object> sizeData : existingSizes) {
                                    if (sizeData.get("size").equals(size)) {
                                        long currentQuantity = (long) sizeData.get("soluong");
                                        sizeData.put("soluong", currentQuantity + soluong); // Update quantity
                                        sizeFound = true;
                                        break;
                                    }
                                }
                            } else {
                                existingSizes = new ArrayList<>();
                            }

                            if (!sizeFound) {
                                // Add a new size entry
                                HashMap<String, Object> newSize = new HashMap<>();
                                newSize.put("size", size);
                                newSize.put("soluong", soluong);
                                existingSizes.add(newSize);
                            }

                            // Update Firestore with the modified sizes array
                            db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("ALL").document(doc.getId())
                                    .update("sizes", existingSizes)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            callback.OnSucess();
                                        } else {
                                            callback.OnFail();
                                        }
                                    });
                        }
                    } else {
                        // If no product with the given ID exists in the cart, create a new entry
                        List<SizeQuantity> sizes = new ArrayList<>();
                        sizes.add(new SizeQuantity(size, (int) soluong));

                        Giohang giohang = new Giohang(idsp, sizes);
                        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("ALL").add(giohang)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        callback.OnSucess();
                                    } else {
                                        callback.OnFail();
                                    }
                                });
                    }
                });
    }


    // Lấy dữ liệu giỏ hàng
    public void HandlegetDataGioHang() {
        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            List<HashMap<String, Object>> sizesData = (List<HashMap<String, Object>>) doc.get("sizes");
                            List<SizeQuantity> sizes = new ArrayList<>();

                            if (sizesData != null) {
                                for (HashMap<String, Object> size : sizesData) {
                                    sizes.add(new SizeQuantity((String) size.get("size"), ((Long) size.get("soluong")).intValue()));
                                }
                            }

                            db.collection("SanPham").document(doc.getString("id_product"))
                                    .get()
                                    .addOnSuccessListener(productDoc -> {
                                        callback.getDataSanPham(
                                                doc.getId(),
                                                doc.getString("id_product"),
                                                productDoc.getString("tensp"),
                                                productDoc.getLong("giatien"),
                                                productDoc.getString("hinhanh"),
                                                productDoc.getString("loaisp"),
                                                productDoc.getString("mota"),
                                                sizes,
                                                productDoc.getLong("type"),
                                                productDoc.getString("chatlieu")
                                        );
                                    });
                        }
                    }
                });
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void HandleDeleteDataGioHang(String id) {
        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL").document(id).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.OnSucess();
                    } else {
                        callback.OnFail();
                    }
                });
    }

    public void HandleGetDataCTHD(String id, String uid) {
        db.collection("ChitietHoaDon").document(uid).collection("ALL").whereEqualTo("id_hoadon", id)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            List<HashMap<String, Object>> sizesData = (List<HashMap<String, Object>>) doc.get("sizes");
                            List<SizeQuantity> sizes = new ArrayList<>();

                            if (sizesData != null) {
                                for (HashMap<String, Object> size : sizesData) {
                                    sizes.add(new SizeQuantity((String) size.get("size"), ((Long) size.get("soluong")).intValue()));
                                }
                            }

                            db.collection("SanPham").document(doc.getString("id_product"))
                                    .get()
                                    .addOnSuccessListener(productDoc -> {
                                        callback.getDataSanPham(
                                                doc.getId(),
                                                doc.getString("id_product"),
                                                productDoc.getString("tensp"),
                                                productDoc.getLong("giatien"),
                                                productDoc.getString("hinhanh"),
                                                productDoc.getString("loaisp"),
                                                productDoc.getString("mota"),
                                                sizes,
                                                productDoc.getLong("type"),
                                                productDoc.getString("chatlieu")
                                        );
                                    });
                        }
                    }
                });

    }
}
