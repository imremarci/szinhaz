package com.example.szinhaz.model;

public class CartItem {
    private Show show;
    private String seatInfo;
    private String rawSeats; // pl. "1-4,1-5"
    private int quantity;
    private int price;

    public CartItem(Show show, String seatInfo, String rawSeats, int quantity, int price) {
        this.show = show;
        this.seatInfo = seatInfo;
        this.rawSeats = rawSeats;
        this.quantity = quantity;
        this.price = price;
    }

    public Show getShow() { return show; }
    public String getSeatInfo() { return seatInfo; }
    public String getRawSeats() { return rawSeats; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }
}