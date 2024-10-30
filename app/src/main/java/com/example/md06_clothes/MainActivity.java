package com.example.md06_clothes;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.md06_clothes.fragment.BillFragment;
import com.example.md06_clothes.fragment.FavoriteFragment;
import com.example.md06_clothes.fragment.HomeFragment;
import com.example.md06_clothes.fragment.NotifyFragment;
import com.example.md06_clothes.fragment.ProfileFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    public  static CountDownTimer countDownTimer;
    private static final int TIME_DELAY = 2000;
    private static long back_pressed;
    private HomeFragment homeFragment = new HomeFragment();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Check Internet
    private BroadcastReceiver MyReceiver = null;


    private int mCountProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    // Check Internet
        broadcastIntent();                  // Check Internet


        setProFile();
        // Trong onCreate của MainActivity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (bottomNavigationView != null) {  // Kiểm tra nếu không bị null
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

//                    int itemId = item.getItemId();
//                    if (itemId == R.id.hometrangchu) {
//                        selectedFragment = new HomeFragment();
//                    } else if (itemId == R.id.canhan) {
//                        selectedFragment = new ProfileFragment();
//                    }else if (itemId == R.id.yeuthich) {
//                        selectedFragment = new FavoriteFragment();
//                    }else if (itemId == R.id.donhang) {
//                        selectedFragment = new BillFragment();
//                    }else if (itemId == R.id.khac) {
//                        selectedFragment = new NotifyFragment();
//                    }
                    switch (item.getItemId()) {
                        case R.id.hometrangchu:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.canhan:
                            selectedFragment = new ProfileFragment();
                            break;
                        case R.id.yeuthich:
                            selectedFragment = new FavoriteFragment();
                            break;
                        case R.id.donhang:
                            selectedFragment = new BillFragment();
                            break;
                        case R.id.khac:

                            break;
                        default:
                            // Xử lý trường hợp không xác định
                            return false;
                    }
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame, selectedFragment)
                                .commit();
                    }
                    return true;
                }

            });
        } else {
            Log.e("MainActivity", "BottomNavigationView is null!");
        }


    }

    public void setProFile() {
        db = FirebaseFirestore.getInstance();
        if (homeFragment.getTvNameHome()==null){
            return;
        }
        if (homeFragment.getTvEmailHome()==null){
            return;
        }
        if (homeFragment.getCirAvatarHome() == null){
            return;
        }
        homeFragment.getTvEmailHome().setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        db.collection("User").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Profile")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()>0){
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            if(documentSnapshot!=null){
                                try{
                                    homeFragment.getTvNameHome().setText(documentSnapshot.getString("hoten").length()>0 ?
                                            documentSnapshot.getString("hoten") : "");

                                    if(documentSnapshot.getString("avatar").length()>0){
                                        Picasso.get().load(documentSnapshot.getString("avatar").trim()).into(homeFragment.getCirAvatarHome());
                                    }
                                }catch (Exception e){
                                    Log.d("ERROR",e.getMessage());
                                }
                            }
                        }
                    }
                });
    }

    @SuppressLint("WrongViewCast")




    public int getmCountProduct() {
        return mCountProduct;
    }

    private void replace(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, fragment);
        transaction.commit();
    }



    @Override
    public void onBackPressed() {
        // Sự kiện nhấn back 2 lần để out app
        if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Nhấn lần nữa để thoát!",
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        editsearch.setText("");
        if(countDownTimer!=null){
            countDownTimer.start();
        }
    }

    // Check Internet
    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }




}