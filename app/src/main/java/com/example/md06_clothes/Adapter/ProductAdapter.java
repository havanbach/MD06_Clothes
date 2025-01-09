package com.example.md06_clothes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.IClickOpenBottomSheet;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Product> arrayList;
    private ProductType type;
    private IClickOpenBottomSheet iClickOpenBottomSheet;

    // Enum cho các loại sản phẩm
    public enum ProductType {
        DEFAULT, DS_PRODUCT, NOIBAT, DO_UONG, THOITRANG1, MICAY, YEUTHICH, LAU, GOIY, GIOHANG
    }

    public ProductAdapter(Context context, ArrayList<Product> arrayList, ProductType type, IClickOpenBottomSheet iClickOpenBottomSheet) {
        this.context = context;
        this.arrayList = arrayList;
        this.type = type;
        this.iClickOpenBottomSheet = iClickOpenBottomSheet;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        int layoutId;
        // Chọn layout dựa trên loại sản phẩm
        switch (type) {
            case DS_PRODUCT:
                layoutId = R.layout.dong_ds_product;
                break;
            case NOIBAT:
                layoutId = R.layout.dong_product_noibat;
                break;
            case DO_UONG:
                layoutId = R.layout.dong_do_uong;
                break;
            case THOITRANG1:
                layoutId = R.layout.dong_sp_thoitrang1;
                break;
            case MICAY:
                layoutId = R.layout.dong_sp_micay;
                break;
            case YEUTHICH:
                layoutId = R.layout.dong_sp_yeuthich;
                break;
            case LAU:
                layoutId = R.layout.dong_sp_lau;
                break;
            case GOIY:
                layoutId = R.layout.dong_sp_goiy;
                break;
            case GIOHANG:
                layoutId = R.layout.dong_giohang;
                break;
            default:
                layoutId = R.layout.dong_ds_product;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Product product = arrayList.get(position);

        // Hiển thị thông tin sản phẩm
        holder.tvTenProduct.setText(product.getTensp());
        holder.tvGiaProduct.setText(NumberFormat.getInstance().format(product.getGiatien()));

        // Hiển thị hình ảnh với Picasso
        Picasso.get().load(product.getHinhanh()).into(holder.imgProduct);

        // Chỉ hiển thị thông tin đặc biệt nếu là giỏ hàng
        if (type == ProductType.GIOHANG) {
            holder.tvchatlieuProduct.setText(product.getchatlieu());
            holder.tvSoluongProduct.setText(String.valueOf(product.getSoluong()));
        }

        // Xử lý sự kiện click
        holder.layoutProduct.setOnClickListener(view -> {
            if (iClickOpenBottomSheet != null) {
                iClickOpenBottomSheet.onClickOpenBottomSheet(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList != null ? arrayList.size() : 0;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTenProduct, tvGiaProduct, tvchatlieuProduct, tvSoluongProduct;
        private ImageView imgProduct;
        private LinearLayout layoutProduct;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvTenProduct = itemView.findViewById(R.id.tv_ten_product);
            tvGiaProduct = itemView.findViewById(R.id.tv_giatien_product);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvchatlieuProduct = itemView.findViewById(R.id.tv_chatlieu_giohang);
            tvSoluongProduct = itemView.findViewById(R.id.tv_number_giohang);
            layoutProduct = itemView.findViewById(R.id.layout_product);
        }
    }
}
