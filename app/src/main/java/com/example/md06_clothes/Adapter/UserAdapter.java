package com.example.md06_clothes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Models.User;
import com.example.md06_clothes.R;
import com.example.md06_clothes.my_interface.IClickCTHD;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> mListUser;
    private boolean ischat;
    private IClickCTHD iClickCTHD;

    String theLastMessage;
    public UserAdapter(Context context, List<User> mListUser, boolean ischat, IClickCTHD iClickCTHD) {
        this.context = context;
        this.mListUser = mListUser;
        this.ischat = ischat;
        this.iClickCTHD = iClickCTHD;
    }


    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvItemUsername;
        private final ImageView imgItemUser;
        private ImageView imgOn, imgOff;
        private TextView tvLastMessage;
        public ViewHolder(View itemView) {
            super(itemView);

            tvItemUsername = itemView.findViewById(R.id.tv_item_username);
            imgItemUser = itemView.findViewById(R.id.img_item_user);
            imgOn = itemView.findViewById(R.id.img_on);
            imgOff = itemView.findViewById(R.id.img_off);
            tvLastMessage = itemView.findViewById(R.id.tv_lass_message);
        }
    }
}
