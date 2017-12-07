package com.example.pietrodimarco.hci.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by awidiyadew on 15/09/16.
 */
public class CustomDataProvider {

    private static final int MAX_LEVELS = 2;

    private static final int LEVEL_1 = 1;
    private static final int LEVEL_2 = 2;

    private static List<BaseItem> mMenu = new ArrayList<>();

    public static List<BaseItem> getInitialItems() {
        //return getSubItems(new GroupItem("root"));

        List<BaseItem> rootMenu = new ArrayList<>();





        rootMenu.add(new GroupItem("Favourites"));
        rootMenu.add(new GroupItem("Recents"));
        rootMenu.add(new Item("Restrooms",false));

        return rootMenu;
    }

    public static List<BaseItem> getSubItems(BaseItem baseItem) {

        List<BaseItem> result = new ArrayList<>();
        int level = ((GroupItem) baseItem).getLevel() + 1;
        String menuItem = baseItem.getName();

        if (!(baseItem instanceof GroupItem)) {
            throw new IllegalArgumentException("GroupItem required");
        }

        GroupItem groupItem = (GroupItem)baseItem;
        if(groupItem.getLevel() >= MAX_LEVELS){
            return null;
        }


        switch (level){
            case LEVEL_1 :
                switch (menuItem.toUpperCase()){
                    case "FAVOURITES" :
                        result = getListFavourites();
                        break;
                    case "RECENTS" :
                        result = getListRecents();
                        break;
                }
                break;

        }

        return result;
    }

    public static boolean isExpandable(BaseItem baseItem) {
        return baseItem instanceof GroupItem;
    }

    private static List<BaseItem> getListFavourites(){

        List<BaseItem> list = new ArrayList<>();



        // Setiap membuat groupItem harus di set levelnya
        /*GroupItem groupItem = new GroupItem("GROUP 1");
        groupItem.setLevel(groupItem.getLevel() + 1);*/

        list.add(new Item("ROOM 2048"));
        list.add(new Item("ROOM 2036"));
        //list.add(groupItem);

        return list;
    }

    private static List<BaseItem> getListRecents(){

        List<BaseItem> list = new ArrayList<>();
       /* GroupItem groupItem = new GroupItem("GROUP X");
        groupItem.setLevel(groupItem.getLevel() + 1);*/

        list.add(new Item("ROOM 1024"));
        list.add(new Item("ROOM 2038"));
       // list.add(groupItem);

        return list;
    }

   /* private static List<BaseItem> getListGroup1(){
        List<BaseItem> list = new ArrayList<>();
        list.add(new Item("CHILD OF G1-A"));
        list.add(new Item("CHILD OF G1-B"));

        return list;
    }

    private static List<BaseItem> getListGroupX(){
        List<BaseItem> list = new ArrayList<>();
        list.add(new Item("CHILD OF GX-A"));
        list.add(new Item("CHILD OF GX-B"));

        return list;
    }*/

}
