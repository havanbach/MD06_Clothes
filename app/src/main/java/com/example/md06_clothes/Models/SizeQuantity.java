package com.example.md06_clothes.Models;

import java.io.Serializable;

public class SizeQuantity implements Serializable {
    private String size;
    private int soluong;

    public SizeQuantity() {
    }

    public SizeQuantity(String size, int soluong) {
        this.size = size;
        this.soluong = soluong;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }
}
