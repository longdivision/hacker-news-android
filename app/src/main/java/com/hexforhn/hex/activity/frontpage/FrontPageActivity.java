package com.hexforhn.hex.activity.frontpage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hexforhn.hex.HexApplication;
import com.hexforhn.hex.R;
import com.hexforhn.hex.activity.story.StoryActivity;
import com.hexforhn.hex.adapter.FrontPageListAdapter;
import com.hexforhn.hex.asynctask.GetTopStories;
import com.hexforhn.hex.asynctask.StoriesHandler;
import com.hexforhn.hex.decoration.DividerItemDecoration;
import com.hexforhn.hex.listener.ClickListener;
import com.hexforhn.hex.model.Story;
import com.hexforhn.hex.util.view.RefreshHandler;
import com.hexforhn.hex.util.view.SwipeRefreshManager;
import com.hexforhn.hex.viewmodel.StoryListItemViewModel;
import com.hexforhn.hex.viewmodel.factory.StoryListItemFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FrontPageActivity extends AppCompatActivity implements StoriesHandler,
        FrontPageStateHandler, ClickListener, RefreshHandler {

    private RecyclerView mRecyclerView;
    private List<Story> mStories = new ArrayList<>();
    private SwipeRefreshManager mSwipeRefreshManager;
    private final static String STORY_TITLE_INTENT_EXTRA_NAME = "storyTitle";
    private final static String STORY_ID_INTENT_EXTRA_NAME = "storyId";
    private final static String STORIES_KEY = "stories";
    private GetTopStories mGetTopStories;
    private FrontPageState mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        setupToolbar();
        setupRecyclerView();
        setupStoriesUnavailableView();
        setupRefreshLayout();
        setupState(savedInstanceState);
    }

    @Override
    public void onEnterLoading() {
        fetchFrontPageStories();
        mSwipeRefreshManager.start();
    }

    @Override
    public void onEnterLoaded() {
        displayStories(mStories);
        hideContentUnavailable();
        showStories();
        mSwipeRefreshManager.stop();
        mSwipeRefreshManager.enable();
    }

    @Override
    public void onEnterRefresh() {
        mSwipeRefreshManager.start();
        mSwipeRefreshManager.disable();
        fetchFrontPageStories();
    }

    @Override
    public void onEnterUnavailable() {
        mSwipeRefreshManager.stop();
        if (mStories.isEmpty()) {
            hideStories();
            mSwipeRefreshManager.disable();
            showContentUnavailable();
        } else {
            mSwipeRefreshManager.enable();
        }
        showRefreshFailedSnackbar();
    }

    @Override
    public void onStoriesReady(List<Story> stories) {
        setStories(stories);
        mState.sendEvent(FrontPageState.Event.LOAD_SUCCEEDED);
    }

    @Override
    public void onStoriesUnavailable() {
        mState.sendEvent(FrontPageState.Event.LOAD_FAILED);
    }

    @Override
    public void onClick(View v, int position, boolean isLongClick) {
        openStoryAtPosition(position);
    }

    @Override
    public void onRefresh() {
        mState.sendEvent(FrontPageState.Event.LOAD_REQUESTED);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STORIES_KEY, new Gson().toJson(mStories));
    }

    private List<Story> extractStories(Bundle savedInstanceState) {
        if (savedInstanceState == null) { return null; }

        List<Story> stories = null;
        String serializedStories = savedInstanceState.getString(STORIES_KEY);

        if (serializedStories != null) {
            stories = new Gson().fromJson(serializedStories,
                    new TypeToken<ArrayList<Story>>() {}.getType());
        }

        return stories;
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.setTitle(R.string.frontPage); }
    }

    private void setupRefreshLayout() {
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshManager = new SwipeRefreshManager(refreshLayout, this);
    }

    private void setupRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) findViewById(R.id.stories);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this));
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.Adapter mAdapter = new FrontPageListAdapter(Collections.EMPTY_LIST, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupStoriesUnavailableView() {
        TextView loadingFailed = (TextView) findViewById(R.id.loading_failed_text);
        loadingFailed.setText(R.string.error_unableToLoadFrontPage);

        Button tryAgain = (Button) findViewById(R.id.try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mState.sendEvent(FrontPageState.Event.LOAD_REQUESTED);
            }
        });
    }

    private void setupState(Bundle savedInstanceState) {
        List<Story> stories = extractStories(savedInstanceState);
        mState = new FrontPageState(this);

        if (stories != null) {
            mStories = stories;
            mState.sendEvent(FrontPageState.Event.RESTORED);
        } else {
            mState.sendEvent(FrontPageState.Event.LOAD_REQUESTED);
        }
    }

    private void displayStories(List<Story> stories) {
        List<StoryListItemViewModel> storyListItems = StoryListItemFactory.createItemListItems(stories);
        RecyclerView.Adapter mAdapter = new FrontPageListAdapter(storyListItems, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showStories() {
        findViewById(R.id.stories).setVisibility(View.VISIBLE);
    }

    private void hideStories() {
        findViewById(R.id.stories).setVisibility(View.GONE);
    }

    private void showContentUnavailable() {
        findViewById(R.id.content_unavailable).setVisibility(View.VISIBLE);
    }

    private void hideContentUnavailable() {
        findViewById(R.id.content_unavailable).setVisibility(View.GONE);
    }

    private void fetchFrontPageStories() {
        if (mGetTopStories != null) {
            mGetTopStories.removeHandler();
        }

        mGetTopStories = new GetTopStories(this, (HexApplication) this.getApplication());
        mGetTopStories.execute();
    }

    private void setStories(List<Story> stories) {
        mStories = stories;
    }

    private void openStoryAtPosition(int position) {
        Intent storyIntent = new Intent(this, StoryActivity.class);
        Story story =  (Story) mStories.get(position);
        storyIntent.putExtra(STORY_TITLE_INTENT_EXTRA_NAME, story.getTitle());
        storyIntent.putExtra(STORY_ID_INTENT_EXTRA_NAME, story.getId());

        startActivity(storyIntent);
    }

    private void showRefreshFailedSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.front_page_container),
                R.string.error_unableToLoadFrontPage, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(Color.WHITE);

        snackbar.show();
    }
}
