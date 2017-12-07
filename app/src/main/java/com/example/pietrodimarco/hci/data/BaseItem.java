package com.example.pietrodimarco.hci.data;

/**
 * Created by awidiyadew on 12/09/16.
 */
public class BaseItem {
    private String mName;
    private boolean isFavourite = false;
    public BaseItem(String name) {
        mName = name;
    }

    public BaseItem(String name, boolean isFavourite) {
        mName = name;
        this.isFavourite = isFavourite;
    }

    public String getName() {
        return mName;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean isFavourite() {
        return isFavourite;
    }
}
