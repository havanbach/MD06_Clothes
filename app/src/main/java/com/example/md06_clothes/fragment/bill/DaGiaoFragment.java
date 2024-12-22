package com.example.md06_clothes.fragment.bill;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.md06_clothes.Adapter.HoaDonAdapter;
import com.example.md06_clothes.Models.HoaDon;
import com.example.md06_clothes.Presenter.HoaDonPreSenter;
import com.example.md06_clothes.R;
import com.example.md06_clothes.View.CTHDActivity;
import com.example.md06_clothes.my_interface.HoaDonView;
import com.example.md06_clothes.my_interface.IClickCTHD;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class DaGiaoFragment extends Fragment implements HoaDonView {

    private TextView tvNullDaGiao;
    private View view;
    private RecyclerView rcvBill;
    private HoaDonPreSenter hoaDonPreSenter;
    private HoaDonAdapter hoaDonAdapter;
    private ArrayList<HoaDon> listHoadon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_da_giao, container, false);

        // Ánh xạ các thành phần giao diện
        tvNullDaGiao = view.findViewById(R.id.tv_null_dagiao);
        rcvBill = view.findViewById(R.id.rcv_bill_dagiao);

        // Khởi tạo presenter và danh sách hóa đơn
        hoaDonPreSenter = new HoaDonPreSenter(this);
        listHoadon = new ArrayList<>();

        // Lấy dữ liệu hóa đơn với trạng thái "Đã giao"
        hoaDonPreSenter.HandleReadDataHDStatus(3);

        return view;
    }

    @Override
    public void getDataHD(String id, String uid, String ghichu, String diachi, String hoten, String ngaydat, String phuongthuc, String sdt, String tongtien, Long type) {
        // Thêm hóa đơn vào danh sách
        listHoadon.add(new HoaDon(id, uid, ghichu, diachi, hoten, ngaydat, phuongthuc, sdt, tongtien, type));

        // Kiểm tra danh sách có trống hay không
        if (!listHoadon.isEmpty()) {
            tvNullDaGiao.setVisibility(View.GONE);
        } else {
            tvNullDaGiao.setVisibility(View.VISIBLE);
        }

        // Sắp xếp danh sách theo ngày đặt hàng giảm dần (mới nhất lên đầu)
        Collections.sort(listHoadon, new Comparator<HoaDon>() {
            @Override
            public int compare(HoaDon o1, HoaDon o2) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // Định dạng ngày
                try {
                    Date date1 = dateFormat.parse(o1.getNgaydat());
                    Date date2 = dateFormat.parse(o2.getNgaydat());
                    return date2.compareTo(date1); // Sắp xếp giảm dần
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // Không thay đổi nếu xảy ra lỗi
                }
            }
        });

        // Khởi tạo và cập nhật adapter
        if (hoaDonAdapter == null) {
            hoaDonAdapter = new HoaDonAdapter();
            rcvBill.setLayoutManager(new LinearLayoutManager(getContext()));
            rcvBill.setAdapter(hoaDonAdapter);
        }

        hoaDonAdapter.setDataHoadon(listHoadon, new IClickCTHD() {
            @Override
            public void onClickCTHD(int pos) {
                HoaDon hoaDon = listHoadon.get(pos);
                Intent intent = new Intent(getContext(), CTHDActivity.class);
                intent.putExtra("HD", hoaDon);
                intent.putExtra("CM", true); // "CM" flag cho biết đây là đơn hàng đã giao
                startActivity(intent);
            }
        });

        // Thông báo dữ liệu đã thay đổi
        hoaDonAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnFail() {
        // Xử lý khi tải dữ liệu thất bại
        tvNullDaGiao.setVisibility(View.VISIBLE);
    }

    @Override
    public void OnSucess() {
        // Xử lý khi tải dữ liệu thành công
        tvNullDaGiao.setVisibility(View.GONE);
    }
}
