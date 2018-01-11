package com.hexforhn.hex.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.hexforhn.hex.R;
import com.hexforhn.hex.drawer.HexDrawer;
import com.hexforhn.hex.fragment.StoryListFragment;
import com.hexforhn.hex.util.ThemeHelper;

import java.util.HashMap;
import java.util.Map;

import static com.hexforhn.hex.drawer.HexDrawer.Item.*;

public class MainActivity extends AppCompatActivity implements HexDrawer.ItemSelectionHandler, FragmentManager.OnBackStackChangedListener {

    private static final String ITEM_KEY = "Item";
    private HexDrawer.Item mCurrentItem;

    private static final Map<HexDrawer.Item, Integer> itemToToolbarTitle = new HashMap<HexDrawer.Item, Integer>(){{
        put(HexDrawer.Item.FrontPage, R.string.frontPageTitle);
        put(HexDrawer.Item.New, R.string.newTitle);
        put(HexDrawer.Item.Ask, R.string.askTitle);
        put(HexDrawer.Item.Show, R.string.showTitle);
        put(HexDrawer.Item.Jobs, R.string.jobsTitle);
        put(HexDrawer.Item.Settings, R.string.settingsTitle);
        put(HexDrawer.Item.About, R.string.aboutTitle);
    }};

    private static final Map<HexDrawer.Item, StoryListFragment.Collection> itemToCollection = new HashMap<HexDrawer.Item, StoryListFragment.Collection>(){{
        put(HexDrawer.Item.FrontPage, StoryListFragment.Collection.Top);
        put(HexDrawer.Item.New, StoryListFragment.Collection.New);
        put(HexDrawer.Item.Ask, StoryListFragment.Collection.Ask);
        put(HexDrawer.Item.Show, StoryListFragment.Collection.Show);
        put(HexDrawer.Item.Jobs, StoryListFragment.Collection.Jobs);
    }};

    private HexDrawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = setupToolbar();

        getFragmentManager().addOnBackStackChangedListener(this);

        mCurrentItem = FrontPage;

        if (savedInstanceState != null) {
            mCurrentItem = HexDrawer.Item.valueOf(savedInstanceState.getString(ITEM_KEY));
        }

        mDrawer = new HexDrawer(this, toolbar, mCurrentItem, this);
        mDrawer.build();

        Bundle bundle = new Bundle();
        bundle.putString(StoryListFragment.CollectionKey, itemToCollection.get(mCurrentItem).toString());
        StoryListFragment storyListFragment = new StoryListFragment();
        storyListFragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_wrapper, storyListFragment);

        transaction.commit();

        updateDrawer();
        updateToolbarTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeHelper.updateTheme(this);
        mDrawer.selectItem(mCurrentItem);
    }

    @Override
    public void onItemSelectedHandler(HexDrawer.Item item) {
        String collection;

        switch (item) {
            case Settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case About:
                Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutIntent);
                break;
            default:
                mCurrentItem = item;

                collection = itemToCollection.get(item).toString();
                Bundle bundle = new Bundle();
                bundle.putString(StoryListFragment.CollectionKey, collection);
                StoryListFragment storyListFragment = new StoryListFragment();
                storyListFragment.setArguments(bundle);

                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStackImmediate();
                }

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.content_wrapper, storyListFragment);
                transaction.addToBackStack(item.toString());

                transaction.commit();

                break;
        }
    }

    private Toolbar setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.frontPageTitle);
        }

        return toolbar;
    }

    private void updateToolbarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(itemToToolbarTitle.get(mCurrentItem));
        }
    }
    
    private void updateDrawer() {
        mDrawer.selectItem(mCurrentItem);
    }

    @Override
    public void onBackStackChanged() {

        if (getFragmentManager().getBackStackEntryCount() < 1) {
            mCurrentItem = HexDrawer.Item.FrontPage;
        } else {
            FragmentManager.BackStackEntry bse = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
            mCurrentItem = HexDrawer.Item.valueOf(bse.getName());
        }

        updateToolbarTitle();
        updateDrawer();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(ITEM_KEY, mCurrentItem.toString());
    }
}
