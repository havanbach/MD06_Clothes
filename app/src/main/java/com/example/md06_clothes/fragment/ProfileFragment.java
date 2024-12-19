package com.example.md06_clothes.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.md06_clothes.MainActivity;
import com.example.md06_clothes.R;
import com.example.md06_clothes.SignInActivity;
import com.example.md06_clothes.ultil.NetworkUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private Uri mUri;
    private ProgressDialog progressDialog;
    private MainActivity mMainActivity;

    private View view;
    private CircleImageView imgAvatar;
    private EditText edtFullName, edtAddress, edtPhoneNumber, edtDate;
    private TextView tvEmail;
    private RadioButton rdoNam, rdoNu;
    private RadioGroup rdoGroup;
    private Button btnUpdateprofile;
    private LinearLayout layoutLogout;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private StorageReference storageReference;
    private String key = "";

    private boolean reloadData = false;

    DatabaseReference reference;
    FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        InitWidget();
        Init();

        if (NetworkUtil.isNetworkConnected(getContext())) {
            reloadData = true;
            LoadInfo();
            setUserInformation();
            Event();
        } else {
            reloadData = false;
        }
        return view;
    }

    private void LoadInfo() {
        firestore.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Profile")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0) {
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if (documentSnapshot != null) {
                                key = documentSnapshot.getId();
                                try {
                                    edtAddress.setText(documentSnapshot.getString("diachi"));
                                    edtFullName.setText(documentSnapshot.getString("hoten"));
                                    edtPhoneNumber.setText(documentSnapshot.getString("sdt"));
                                    edtDate.setText(documentSnapshot.getString("ngaysinh"));
                                    String sex = documentSnapshot.getString("gioitinh");
                                    if (sex != null && !sex.isEmpty()) {
                                        if (sex.equals("Nam")) {
                                            rdoNam.setChecked(true);
                                        } else {
                                            rdoNu.setChecked(true);
                                        }
                                    } else {
                                        rdoGroup.clearCheck();
                                    }
                                    if (documentSnapshot.getString("avatar") != null) {
                                        Picasso.get().load(documentSnapshot.getString("avatar").trim()).into(imgAvatar);
                                    }
                                } catch (Exception e) {
                                    Log.e("ProfileFragment", "Error loading profile: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
    }

    private void setUserInformation() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            edtFullName.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
            Glide.with(getActivity()).load(user.getPhotoUrl()).error(R.drawable.ic_avatar_default).into(imgAvatar);
        }
    }

    private void Init() {
        progressDialog = new ProgressDialog(getActivity());
        mMainActivity = (MainActivity) getActivity();
    }

    private void Event() {

        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int ngay = calendar.get(Calendar.DATE);
                int thang = calendar.get(Calendar.MONTH);
                int nam = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);

                        // Kiểm tra nếu ngày được chọn nằm sau ngày hiện tại
                        if (selectedDate.after(Calendar.getInstance())) {
                            Toast.makeText(getContext(), "Ngày sinh phải nhỏ hơn ngày hiện tại!", Toast.LENGTH_SHORT).show();
                        } else {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            edtDate.setText(simpleDateFormat.format(selectedDate.getTime()));
                        }
                    }
                }, nam, thang, ngay);

                datePickerDialog.show();
            }
        });

        btnUpdateprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strFullName = edtFullName.getText().toString().trim();
                String strAddress = edtAddress.getText().toString().trim();
                String strSDT = edtPhoneNumber.getText().toString().trim();
                String strDate = edtDate.getText().toString().trim();
                String strSex;

                if (rdoNam.isChecked()) {
                    strSex = "Nam";
                } else if (rdoNu.isChecked()) {
                    strSex = "Nữ";
                } else {
                    strSex = "";
                }

                // Validate dữ liệu đầu vào
                if (strFullName.isEmpty()) {
                    edtFullName.setError("Vui lòng nhập tên đầy đủ!");
                    return;
                }
                if (strAddress.isEmpty()) {
                    edtAddress.setError("Vui lòng nhập địa chỉ!");
                    return;
                }
                if (!strSDT.matches("^[0-9]{10,11}$")) {
                    edtPhoneNumber.setError("Số điện thoại không hợp lệ!");
                    return;
                }
                if (strDate.isEmpty()) {
                    edtDate.setError("Vui lòng chọn ngày sinh!");
                    return;
                }
                if (strSex.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng chọn giới tính!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(strFullName)
                            .setPhotoUri(mUri)
                            .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Cập nhật thông tin tài khoản thành công!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                Map<String, Object> chinh = new HashMap<>();
                chinh.put("hoten", strFullName);
                chinh.put("diachi", strAddress);
                chinh.put("sdt", strSDT);
                chinh.put("ngaysinh", strDate);
                chinh.put("gioitinh", strSex);

                firestore.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("Profile").document(key)
                        .update(chinh)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                    LoadInfo();
                                } else {
                                    Toast.makeText(getContext(), "Đã xảy ra lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void InitWidget() {
        imgAvatar = view.findViewById(R.id.img_avatar);
        edtFullName = view.findViewById(R.id.edt_full_name);
        edtAddress = view.findViewById(R.id.edt_address);
        edtPhoneNumber = view.findViewById(R.id.edt_phone);
        edtDate = view.findViewById(R.id.edt_date);
        rdoNam = view.findViewById(R.id.rdo_nam);
        rdoNu = view.findViewById(R.id.rdo_nu);
        rdoGroup = view.findViewById(R.id.rdo_group);
        tvEmail = view.findViewById(R.id.tv_email_profile);
        btnUpdateprofile = view.findViewById(R.id.btn_update_profile);
        layoutLogout = view.findViewById(R.id.layout_logout);
    }
}
