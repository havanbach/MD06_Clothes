package com.example.md06_clothes.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.R;
import com.example.md06_clothes.View.CartActivity;
import com.example.md06_clothes.View.DetailSPActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GiohangAdapter extends RecyclerView.Adapter<GiohangAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Product> mListGiohang;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CartActivity cartActivity;
    ;

    public GiohangAdapter(Context context, ArrayList<Product> mListGiohang, CartActivity cartActivity) {
        this.context = context;
        this.mListGiohang = mListGiohang;
        this.cartActivity = cartActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dong_giohang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = mListGiohang.get(position);
        holder.tvTenGiohang.setText(product.getTensp());
        holder.tvGiatienGiohang.setText(NumberFormat.getInstance().format(product.getGiatien()) + " VNĐ");
        holder.tvchatlieuGiohang.setText("Chất liệu: "+product.getChatlieu());
        Picasso.get().load(product.getHinhanh()).into(holder.imgGiohang);

        // Xóa tất cả các view cũ của sizes trong container (tránh trùng lặp khi tái sử dụng ViewHolder)
        holder.sizeContainer.removeAllViews();

        // Duyệt qua danh sách size và số lượng của sản phẩm
        for (SizeQuantity size : product.getSizes()) {
            View sizeView = LayoutInflater.from(context).inflate(R.layout.item_size_quantity, holder.sizeContainer, false);

            TextView tvSize = sizeView.findViewById(R.id.tv_size);
            TextView tvQuantity = sizeView.findViewById(R.id.tv_quantity);
            ImageView btnPlus = sizeView.findViewById(R.id.btn_plus);
            ImageView btnMinus = sizeView.findViewById(R.id.btn_minus);

            // Gán dữ liệu size và số lượng
            tvSize.setText("Size: " + size.getSize());
            tvQuantity.setText(String.valueOf(size.getSoluong()));

            // Nút tăng số lượng
            btnPlus.setOnClickListener(v -> updateQuantity(product, size, tvQuantity, 1));

            // Nút giảm số lượng
            btnMinus.setOnClickListener(v -> updateQuantity(product, size, tvQuantity, -1));

            // Ẩn nút giảm nếu số lượng <= 1
            btnMinus.setVisibility(size.getSoluong() <= 1 ? View.GONE : View.VISIBLE);

            // Thêm sizeView vào container
            holder.sizeContainer.addView(sizeView);
        }

        // Cập nhật tổng giá tiền cho sản phẩm
        updateTotal(holder, product);

        // click image_product hiển thị chi tiết sp đó
        holder.imgGiohang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailSPActivity.class);
                intent.putExtra("search", product); // Truyền thông tin sản phẩm vào Intent
                context.startActivity(intent);
            }
        });
    }

    private void updateQuantity(Product product, SizeQuantity size, TextView tvQuantity, int delta) {
        int newQuantity = Math.max(1, size.getSoluong() + delta); // Cập nhật số lượng size sản phẩm, đảm bảo >= 1
        size.setSoluong(newQuantity);

        // Update Firestore
        db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("ALL")
                .whereEqualTo("id_product", product.getIdsp())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        List<HashMap<String, Object>> existingSizes = (List<HashMap<String, Object>>) doc.get("sizes");
                        if (existingSizes != null) {
                            for (HashMap<String, Object> sizeData : existingSizes) {
                                if (sizeData.get("size").equals(size.getSize())) {
                                    sizeData.put("soluong", newQuantity);// Cập nhật số lượng
                                    break;
                                }
                            }

                            // Update Firestore document
                            db.collection("GioHang").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("ALL").document(doc.getId())
                                    .update("sizes", existingSizes)
                                    .addOnSuccessListener(aVoid -> {
                                        // Cập nhật giao diện sau khi lưu thành công
                                        tvQuantity.setText(String.valueOf(newQuantity));
                                        cartActivity.UpdateCartSummary();// Cập nhật tổng giá giỏ hàng
                                        notifyDataSetChanged();// Làm mới danh sách
                                    });
                        }
                    }
                });
    }

    private void updateTotal(ViewHolder holder, Product product) {
        // Tính tổng giá tiền cho sản phẩm (dựa trên giá và số lượng)
        int total = 0;
        for (SizeQuantity size : product.getSizes()) {
            total += product.getGiatien() * size.getSoluong();
        }
        holder.tvTotalGiohang.setText("Giá tiền: "+NumberFormat.getInstance().format(total) + " VNĐ");
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng sản phẩm trong giỏ hàng
        return mListGiohang.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTenGiohang, tvchatlieuGiohang, tvGiatienGiohang, tvTotalGiohang;
        private ImageView imgGiohang;
        private LinearLayout sizeContainer; // Container for sizes

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenGiohang = itemView.findViewById(R.id.tv_ten_giohang);
            tvchatlieuGiohang = itemView.findViewById(R.id.tv_chatlieu_giohang);
            tvGiatienGiohang = itemView.findViewById(R.id.tv_giatien_giohang);
            tvTotalGiohang = itemView.findViewById(R.id.tv_total_giohang);
            imgGiohang = itemView.findViewById(R.id.img_giohang);
            sizeContainer = itemView.findViewById(R.id.size_container);

        }
    }
}
