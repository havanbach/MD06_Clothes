package com.example.md06_clothes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Models.SizeQuantity;
import com.example.md06_clothes.R;

import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.SizeViewHolder> {

    private final List<SizeQuantity> sizes;
    private final OnSizeClickListener listener;
    private int selectedPosition = -1;

    // Constructor nhận listener, nếu không có listener thì truyền vào null
    public SizeAdapter(List<SizeQuantity> sizes, OnSizeClickListener listener) {
        this.sizes = sizes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_size, parent, false);
        return new SizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeViewHolder holder, int position) {
        SizeQuantity size = sizes.get(position);
        holder.tvSize.setText("Size: "+size.getSize());
        holder.tvQuantity.setText("Số lượng: " + size.getSoluong());

        // Kiểm tra nếu listener không phải là null thì mới thực hiện click
        holder.radioButton.setChecked(position == selectedPosition);

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                selectedPosition = position;
                listener.onSizeClick(position);  // Gọi phương thức listener khi có click
                notifyDataSetChanged();
            });
        } else {
            // Nếu không có listener thì ẩn RadioButton
            holder.radioButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sizes.size();
    }

    public interface OnSizeClickListener {
        void onSizeClick(int position);  // Phương thức listener cho sự kiện click
    }

    public static class SizeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSize, tvQuantity;
        RadioButton radioButton;

        public SizeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            radioButton = itemView.findViewById(R.id.radio_button);
        }
    }
}
