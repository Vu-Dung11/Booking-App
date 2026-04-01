
package com.example.bookingapp.data.model.views;


public class Category {
    private String icon;
    private String name;
    boolean isSelected;

    public Category(String icon, String name, boolean isSelected) {
        this.icon = icon;
        this.name = name;
        this.isSelected = isSelected;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
