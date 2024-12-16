package com.example.md06_clothes.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.md06_clothes.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class NotifyFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "NotifyFragment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView txtDiaChi, txtSdt, txtNoiDung, txtGioiThieu;
    private LinearLayout layoutContainer;

    // Tọa độ mặc định của cửa hàng
    private static final LatLng STORE_LOCATION = new LatLng(21.015789, 105.723599);
    private static final float DEFAULT_ZOOM = 18f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notify, container, false);

        txtDiaChi = v.findViewById(R.id.txtdiachi);
        txtSdt = v.findViewById(R.id.txtsdt);
        txtNoiDung = v.findViewById(R.id.txtnoidung);
        txtGioiThieu = v.findViewById(R.id.txtgioithieu);
        layoutContainer = v.findViewById(R.id.layoutContainer);

        // Lấy thông tin từ Firestore và cập nhật vào TextView
        db.collection("ThongTinCuaHang").document("4It8RfW5U1FoFOWiZH7W")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        txtDiaChi.setText("Email: " + documentSnapshot.getString("email"));
                        txtSdt.setText("Số điện thoại: " + documentSnapshot.getString("sdt"));
                        txtNoiDung.setText("Nội dung: " + documentSnapshot.getString("noidung"));
                        txtGioiThieu.setText("Giới thiệu: " + documentSnapshot.getString("gioithieu"));
                    } else {
                        Log.w(TAG, "Document does not exist!");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching document", e));

        // Khởi tạo Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return v;
    }

    @Override
    public void onMapReady(@NonNull @NotNull GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        googleMap.addMarker(new MarkerOptions().position(STORE_LOCATION).title("Cửa hàng Clothes"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(STORE_LOCATION, DEFAULT_ZOOM));
    }
}
