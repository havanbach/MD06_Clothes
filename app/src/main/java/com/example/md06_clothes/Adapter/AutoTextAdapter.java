package com.example.md06_clothes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.md06_clothes.Models.Product;
import com.example.md06_clothes.R;
import java.util.ArrayList;
import java.util.List;

public class AutoTextAdapter extends ArrayAdapter<Product> {
    private List<Product> mlistProduct;//Khởi tạo với bản sao của danh sách gốc => để không làm thay đổi dữ liệu nguồn khi tìm kiếm sp.
    public AutoTextAdapter(@NonNull Context context, int resource, @NonNull List<Product> objects) {
        super(context, resource, objects);
        mlistProduct = new ArrayList<>(objects);
    }
    //Hiển thị dữ liệu trong danh sách
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_dong_auto_text, parent, false);
        }

        TextView tvcountryname = convertView.findViewById(R.id.textViewCountryName);//tvcountryname:Hiển thị tên sản phẩm
        Product product = getItem(position);
        tvcountryname.setText(product.getTensp());
        return convertView;
    }
    //Lọc danh sách = cách Filter
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) { //performFiltering: Lọc d/s dựa trên từ khóa (charSequence).
                List<Product> mListSuggest = new ArrayList<>();

                if (charSequence == null || charSequence.length() == 0){
                    mListSuggest.addAll(mlistProduct);// thêm tất cả các sp có ở d/s khi tìm kiếm vào thanh tìm kiếm
                } else {
                    String filter = charSequence.toString().toLowerCase().trim();
                    for (Product product: mlistProduct){
                        if (product.getTensp().toLowerCase().contains(filter)){
                            mListSuggest.add(product);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mListSuggest;
                filterResults.count = mListSuggest.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {//publishResults: Cập nhật d/s hiển thị dựa trên kết quả lọc (filterResults).
                clear();
                addAll((List<Product>) filterResults.values);
                notifyDataSetInvalidated();
            }

            //Chuyển s/p đã chọn thành chuỗi (hiển thị trong AutoCompleteTextView)
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((Product) resultValue).getTensp();
            }
        };
    }
}
