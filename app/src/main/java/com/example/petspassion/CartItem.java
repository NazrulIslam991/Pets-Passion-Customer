package com.example.petspassion;

public class CartItem {
    private String cart_id;
    private String product_image;
    private String name;
    private String product_id;
    private String quantity;
    private String totle_price;
    private String imageUrl;

    public CartItem() {
    }

    public CartItem(String cart_id, String product_image, String name, String product_id, String quantity, String totle_price) {
        this.cart_id = cart_id;
        this.product_image = product_image;
        this.name = name;
        this.product_id = product_id;
        this.quantity = quantity;
        this.totle_price = totle_price;
    }

    public String getCart_id() {
        return cart_id;
    }

    public void setCart_id(String cart_id) {
        this.cart_id = cart_id;
    }

    public String getProduct_image() {
        return product_image;
    }

    public void setProduct_image(String product_image) {
        this.product_image = product_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTotle_price() {
        return totle_price;
    }

    public void setTotle_price(String totle_price) {
        this.totle_price = totle_price;
    }
}
