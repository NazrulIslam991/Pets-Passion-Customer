package com.example.petspassion;

public class DataClass {
    private String product_name;
    private String product_categories;
    private String product_description;
    private String product_price;
    private String product_discount;
    private String product_quantity;
    private String product_image;
    private String product_id;

    private String category_name;
    private String category_image;

    private String name;
    private String description;
    private String imageUrl;
    private String price;
    private String t_price;
    private String discountPrice;
    private String discount;
    private String quantity;

    public String getTotle_price() {
        return totle_price;
    }

    public void setTotle_price(String totle_price) {
        this.totle_price = totle_price;
    }

    private String totle_price;


    public DataClass() {
    }


    public DataClass(String name, String description, String imageUrl, String price, String discountPrice, String discount, String quantity) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.discountPrice = discountPrice;
        this.discount = discount;
        this.quantity = quantity;
    }

    public DataClass(String name, String description, String price, String discountPrice, String discount, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountPrice = discountPrice;
        this.discount = discount;
        this.imageUrl = imageUrl;
    }

    public DataClass(String name, String discountPrice, String imageUrl) {
        this.name = name;
        this.discountPrice = discountPrice;
        this.imageUrl = imageUrl;
    }





    public String getCategory_name() {
        return category_name;
    }

    public String getCategory_image() {
        return category_image;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public void setCategory_image(String category_image) {
        this.category_image = category_image;
    }


    public String getProduct_name() {
        return product_name;
    }

    public String getProduct_categories() {
        return product_categories;
    }

    public String getProduct_description() {
        return product_description;
    }

    public String getProduct_price() {
        return product_price;
    }
    public String getProduct_discount() {
        return product_discount;
    }

    public String getProduct_quantity() {
        return product_quantity;
    }

    public String getProduct_image() {
        return product_image;
    }
    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getTotalPrice() { return t_price; }
    public void setPrice(String price) { this.price = price; }

    public String getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(String discountPrice) { this.discountPrice = discountPrice; }

    public String getDiscount() { return discount; }
    public void setDiscount(String discount) { this.discount = discount; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }







}
