package com.example.md06_clothes.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.R;
import com.example.md06_clothes.View.DetailSPActivity;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class HoaDonDaGiaoAdapter extends RecyclerView.Adapter<HoaDonDaGiaoAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Product> list;
    private IClickCTHD iClickCTHD;
    private int type;

    public void setDataHoaDonDaGiao(Context context, ArrayList<Product> list, int type, IClickCTHD iClickCTHD) {
        this.context = context;
        this.list = list;
        this.type = type;
        this.iClickCTHD = iClickCTHD;
        notifyDataSetChanged();

    }
////
    @NotNull
    @Override
    public HoaDonDaGiaoAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        if (type == 1){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dong_hoadon_dagiao, parent, false);
        } else view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dong_hoadon_khac, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Log.d("abc", "onBind");
        Product product = list.get(position);
        holder.tvTen.setText(product.getTensp());
        holder.tvDongia.setText(NumberFormat.getInstance().format(product.getGiatien()));
        // Thiết lập RecyclerView hiển thị danh sách size
        SizeAdapter sizeAdapter = new SizeAdapter(product.getSizes(),null);
        holder.rcvSizes.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        holder.rcvSizes.setAdapter(sizeAdapter);
        Picasso.get().load(product.getHinhanh()).into(holder.img);
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailSPActivity.class);
                intent.putExtra("search", product);
                intent.putExtra("from_cart", true);
                // Truyền thông tin sản phẩm vào Intent
                context.startActivity(intent);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("BinhLuan")
                .whereEqualTo("iduser", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereEqualTo("idproduct", product.getIdsp())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            holder.btnDongDanhGia.setEnabled(false);
                            holder.btnDongDanhGia.setText("Đã đánh giá");
                        } else {
                            holder.btnDongDanhGia.setEnabled(true);
                            holder.btnDongDanhGia.setText("Đánh giá");
                        }

                        notifyDataSetChanged();
                    }
                });



        holder.btnDongDanhGia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickCTHD.onClickCTHD(position);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iClickCTHD.onClickCTHD(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout constraintDongDanhGia;
        private TextView tvTen, tvDongia, tvTotal;
        private CircleImageView img;
        private Button btnDongDanhGia;
        private RecyclerView rcvSizes;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            tvTen = itemView.findViewById(R.id.tv_name_dong_danhgia);
            tvDongia = itemView.findViewById(R.id.tv_giatien_dong_danhgia);
            tvTotal = itemView.findViewById(R.id.tv_total_dong_danhgia);
            img = itemView.findViewById(R.id.img_dong_danhgia);
            constraintDongDanhGia = itemView.findViewById(R.id.constraint_dong_danhgia);
            btnDongDanhGia = itemView.findViewById(R.id.btn_dong_danhgia);
            rcvSizes = itemView.findViewById(R.id.rcv_sizes);
        }
    }
}