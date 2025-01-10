package com.example.md06_clothes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class LichSuSearchAdapter extends RecyclerView.Adapter<LichSuSearchAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> mlist;
    private IClickCTHD iClickCTHD;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    // Cập nhật phương thức setdata() để nhận Context, List và IClickCTHD
    public void setData(Context context, ArrayList<String> mlist, IClickCTHD iClickCTHD) {
        this.context = context;
        this.mlist = mlist;
        this.iClickCTHD = iClickCTHD;
        notifyDataSetChanged();  // Đảm bảo RecyclerView được cập nhật khi dữ liệu thay đổi
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dong_lichsu_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String keyword = mlist.get(position);
        holder.tvLichSuSearch.setText(keyword);

        // Xử lý khi nhấn vào item (Tự động điền từ khóa vào thanh tìm kiếm)
        holder.relativeItemStory.setOnClickListener(view -> {
            iClickCTHD.onClickCTHD(position);
        });

        // Xử lý khi nhấn nút xóa
        holder.imgDelete.setOnClickListener(view -> {
            // Xóa từ khóa khỏi danh sách mlist
            mlist.remove(position);
            notifyItemRemoved(position);  // Cập nhật RecyclerView

            // Xóa từ khóa khỏi Firestore
            deleteKeywordFromFirestore(keyword);
        });
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    private void deleteKeywordFromFirestore(String keyword) {
        // Xóa từ khóa khỏi Firestore
        firestore.collection("LichSuTimKiem")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Story")
                .whereEqualTo("noidungtimkiem", keyword)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots) {
                        // Xóa tài liệu chứa từ khóa khỏi Firestore
                        firestore.collection("LichSuTimKiem")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .collection("Story")
                                .document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Xóa thành công từ Firestore
                                })
                                .addOnFailureListener(e -> {
                                    // Xử lý lỗi khi xóa
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi khi lấy dữ liệu từ Firestore
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvLichSuSearch;
        private RelativeLayout relativeItemStory;
        private ImageView imgDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLichSuSearch = itemView.findViewById(R.id.tv_lichsu_search);
            relativeItemStory = itemView.findViewById(R.id.relative_item_story);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }
}
