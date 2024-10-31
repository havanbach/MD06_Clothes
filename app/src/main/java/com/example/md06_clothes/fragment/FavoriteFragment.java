package com.example.md06_clothes.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.md06_clothes.Adapter.FavoriteAdapter;
import com.example.md06_clothes.Models.Favorite;
import com.example.md06_clothes.Presenter.FavoritePresenter;
import com.example.md06_clothes.Presenter.ProductPresenter;
import com.example.md06_clothes.R;

import com.example.md06_clothes.my_interface.FavoriteView;
import com.example.md06_clothes.my_interface.IClickCTHD;
import com.example.md06_clothes.my_interface.ProductView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FavoriteFragment  {

}