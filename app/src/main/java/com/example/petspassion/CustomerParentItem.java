package com.example.petspassion;

import java.util.List;

public class CustomerParentItem {

    private String title;
    private List<DataClass> childItemList;

    public CustomerParentItem(String title, List<DataClass> childItemList) {
        this.title = title;
        this.childItemList = childItemList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<DataClass> getChildItemList() {
        return childItemList;
    }

    public void setChildItemList(List<DataClass> childItemList) {
        this.childItemList = childItemList;
    }
}
