package com.hexforhn.hex.drawer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import com.hexforhn.hex.R;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.*;

public class HexDrawer implements Drawer.OnDrawerItemClickListener{

    public enum Item {
        FrontPage, New, Ask, Show, Jobs, About, Settings
    }

    private final List<Item> primaryItemsOrder  = new ArrayList<>(Arrays.asList(Item.FrontPage, Item.New, Item.Ask, Item.Show, Item.Jobs));

    private final List<Item> secondaryItemsOrder = new ArrayList<>(Arrays.asList(Item.Settings, Item.About));

    private static class ItemDetails {
        final int index;
        final String title;
        final int mIconResource;

        public ItemDetails(int index, String title, int mIconResource) {
            this.index = index;
            this.mIconResource = mIconResource;
            this.title = title;
        }
    }

    private static final Map<Item, ItemDetails> primaryItems = new HashMap<Item, ItemDetails>(){{
        put(Item.FrontPage, (new ItemDetails(0, "Front Page", R.drawable.upward_arrow)));
        put(Item.New, (new ItemDetails(1, "New", R.drawable.new_releases)));
        put(Item.Ask, (new ItemDetails(2, "Ask",  R.drawable.question_answer)));
        put(Item.Show, (new ItemDetails(3, "Show",  R.drawable.group)));
        put(Item.Jobs, (new ItemDetails(4, "Jobs",  R.drawable.work)));
    }};

    private final Activity mActivity;
    private final Toolbar mToolbar;
    private final Item mCurrentItem;
    private Drawer mDrawer;
    private final ItemSelectionHandler mHandler;

    public interface ItemSelectionHandler {
        void onItemSelectedHandler(Item item);
    }

    public HexDrawer(Activity activity, Toolbar toolbar, Item currentItem, ItemSelectionHandler handler) {
        this.mActivity = activity;
        this.mCurrentItem = currentItem;
        this.mToolbar = toolbar;
        this.mHandler = handler;
    }

    public void build() {
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(mActivity)
                .withToolbar(mToolbar)
                .withHeader(R.layout.drawer_header)
                .withTranslucentStatusBar(true)
                .withFullscreen(true)
                .withSelectedItemByPosition(Arrays.asList(Item.values()).indexOf(mCurrentItem) + 1);

        int i = 0;

        for (Item item : primaryItemsOrder) {
            final ItemDetails itemDetails = primaryItems.get(item);

            PrimaryDrawerItem drawerItem = new PrimaryDrawerItem()
                .withIdentifier((long) item.ordinal())
                .withName(itemDetails.title)
                .withIcon(itemDetails.mIconResource)
                .withIconTintingEnabled(true)
                .withOnDrawerItemClickListener(this);

            builder.addDrawerItems(drawerItem);
        }

        builder.addDrawerItems(new DividerDrawerItem());

        for (Item item : secondaryItemsOrder) {
            builder.addDrawerItems(
                    new SecondaryDrawerItem()
                            .withIdentifier((long) item.ordinal())
                            .withName(item.toString())
                            .withOnDrawerItemClickListener(this)
            );
        }

        mDrawer = builder.build();

        resizeDrawerHeader(mDrawer.getHeader());
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        position -= 1;

        this.mHandler.onItemSelectedHandler(
                position <= primaryItems.size() ?
                        primaryItemsOrder.get(position) :
                        secondaryItemsOrder.get(position - (primaryItems.size() + 1)));

        mDrawer.closeDrawer();

        return true;
    }

    public void selectItem(Item item) {
        mDrawer.setSelection((long) item.ordinal(), false);
    }

    /**
     * The drawer height is resized so it is the same height as the status bar + the app bar.
     * The top-level View was not responding to resize so it serves as a wrapper and its only child is instead
     * resized.
     *
     * @param header The top-level header View.
     */
    private void resizeDrawerHeader(View header) {
        View itemWrapper = header.findViewById(R.id.item_wrapper);

        ViewGroup.LayoutParams existingParams = itemWrapper.getLayoutParams();
        existingParams.height = getHeaderHeight();

        int paddingDp = convertDpToPixel(13, header.getContext());
        itemWrapper.setPadding(paddingDp, (paddingDp + getStatusBarHeight()), paddingDp, paddingDp);

        itemWrapper.setLayoutParams(existingParams);
    }

    private int convertDpToPixel(int dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private int getHeaderHeight() {
        return getAppBarHeight() + (2 * getStatusBarHeight());
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = mActivity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mActivity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getAppBarHeight() {
        TypedValue tv = new TypedValue();
        mActivity.getApplicationContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
        return mActivity.getResources().getDimensionPixelSize(tv.resourceId);
    }
}
