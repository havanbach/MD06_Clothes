package com.example.md06_clothes.my_interface;

public interface UserView {
    void OnSucess();

    void OnFail();

    void getDataUser(String id, String email,String name, String address, String phone, String date, String gender, String avatar);
}