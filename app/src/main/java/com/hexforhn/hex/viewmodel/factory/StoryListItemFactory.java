package com.hexforhn.hex.viewmodel.factory;

import com.hexforhn.hex.model.Item;
import com.hexforhn.hex.model.Story;
import com.hexforhn.hex.viewmodel.StoryListItemViewModel;

import java.util.ArrayList;
import java.util.List;

public class StoryListItemFactory {

    public static ArrayList<StoryListItemViewModel> createItemListItems(List<? extends Item> items) {
        ArrayList<StoryListItemViewModel> itemListItems = new ArrayList<>();

        if (items == null) {
            return itemListItems;
        }

        for (Object item : items) {
            if (item instanceof Story) {
                itemListItems.add(createItemListItemForStory((Story) item));
            }
        }

        return itemListItems;
    }

    private static StoryListItemViewModel createItemListItemForStory(Story story) {
        return new StoryListItemViewModel(story.getTitle(), story.getDomain(),
                story.getScore(), story.getCommentCount(), story.getDate());
    }

}
