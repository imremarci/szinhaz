package com.example.szinhaz.model;

public class CartItem {
    private Show show;
    private String seatInfo;
    private int quantity;
    private int price;

    public CartItem(Show show, String seatInfo, int quantity, int price) {
        this.show = show;
        this.seatInfo = seatInfo;
        this.quantity = quantity;
        this.price = price;
    }
    // Getterek kellenek a Thymeleafhez!
    public Show getShow() { return show; }
    public String getSeatInfo() { return seatInfo; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }
}