package com.hexforhn.hex.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hexforhn.hex.HexApplication;
import com.hexforhn.hex.R;
import com.hexforhn.hex.activity.StoryActivity;
import com.hexforhn.hex.adapter.FrontPageListAdapter;
import com.hexforhn.hex.decoration.DividerItemDecoration;
import com.hexforhn.hex.listener.ClickListener;
import com.hexforhn.hex.model.Story;
import com.hexforhn.hex.net.hexapi.StoryCollectionService;
import com.hexforhn.hex.viewmodel.StoryListItemViewModel;
import com.hexforhn.hex.viewmodel.factory.StoryListItemFactory;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;


public class StoryListFragment extends Fragment implements ClickListener {

    private final static String STORY_TITLE_INTENT_EXTRA_NAME = "storyTitle";
    private final static String STORY_ID_INTENT_EXTRA_NAME = "storyId";
    private final static String STORIES_KEY = "stories";

    private Single mGetStories;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Story> mStories;
    private View mRootView;

    public final static String CollectionKey = "collection";

    public enum Collection {
        Top, New, Ask, Show, Jobs
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_story_list, container, false);
        mRootView = rootView;

        setupRecyclerView(rootView);
        setupStoriesUnavailableView(rootView);
        setupRefreshLayout(rootView);

        mGetStories = getStories();

        processBundle(savedInstanceState);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupState();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STORIES_KEY, new Gson().toJson(mStories));
    }

    private void processBundle(Bundle savedInstanceState) {
        List<Story> stories = extractStories(savedInstanceState);

        if (stories != null) {
            mStories = stories;
        }
    }

    private void setupState() {
        if (mStories != null) {
            displayStories(mStories);
        } else {
            loadStories();
        }
    }

    @Override
    public void onClick(View v, int position, boolean isLongClick) {
        openStoryAtPosition(position);
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

    private void setupRecyclerView(View rootView) {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mRootView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.stories);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRootView.getContext()));
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.Adapter mAdapter = new FrontPageListAdapter(Collections.EMPTY_LIST, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupStoriesUnavailableView(View rootView) {
        TextView loadingFailed = (TextView) rootView.findViewById(R.id.loading_failed_text);
        loadingFailed.setText(R.string.error_unableToLoadFrontPage);

        Button tryAgain = (Button) rootView.findViewById(R.id.try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO load stories
            }
        });
    }

    private void setupRefreshLayout(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadStories();
            }
        });
    }

    private Single getStories() {
        return Single.fromCallable(new Callable<List<Story>>() {
                @Override
                public List<Story> call() {
                    HexApplication application = (HexApplication) getActivity().getApplication();
                    StoryCollectionService service = new StoryCollectionService(
                            application.getRequestQueue(),
                            application.apiBaseUrl);

                    Collection collectionParam = Collection.valueOf(getArguments().getString(CollectionKey));

                    return service.getStories(getCollection(collectionParam));

            }
        });
    }

    private void loadStories() {
        SingleObserver observer = new SingleObserver<List<Story>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onSuccess(List<Story> stories) {
                mStories = stories;
                displayStories(stories);
                hideContentUnavailable();

                mRootView.findViewById(R.id.stories).setVisibility(View.VISIBLE);

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                if (mStories.isEmpty()) {
                    showRefreshFailedSnackbar();
                } else {
                    mRootView.findViewById(R.id.stories).setVisibility(View.GONE);
                    showContentUnavailable();
                }

                mSwipeRefreshLayout.setRefreshing(false);
            }
        };

        mGetStories.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void showContentUnavailable() {
        mRootView.findViewById(R.id.content_unavailable).setVisibility(View.VISIBLE);
    }

    private void hideContentUnavailable() {
        mRootView.findViewById(R.id.content_unavailable).setVisibility(View.GONE);
    }

    private void displayStories(List<Story> stories) {
        List<StoryListItemViewModel> storyListItems = StoryListItemFactory.createItemListItems(stories);
        RecyclerView.Adapter mAdapter = new FrontPageListAdapter(storyListItems, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showRefreshFailedSnackbar() {
        Snackbar snackbar = Snackbar.make(mRootView.findViewById(R.id.front_page_container),
                R.string.error_unableToLoadFrontPage, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = (TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextColor(Color.WHITE);

        snackbar.show();
    }

    private void openStoryAtPosition(int position) {
        Intent storyIntent = new Intent(this.getActivity().getApplicationContext(), StoryActivity.class);
        Story story =  mStories.get(position);
        storyIntent.putExtra(STORY_TITLE_INTENT_EXTRA_NAME, story.getTitle());
        storyIntent.putExtra(STORY_ID_INTENT_EXTRA_NAME, story.getId());

        startActivity(storyIntent);
    }

    private StoryCollectionService.Collection getCollection(Collection collection) {
        switch(collection) {
            case Top:
                return StoryCollectionService.Collection.Top;
            case New:
                return StoryCollectionService.Collection.New;
            case Ask:
                return StoryCollectionService.Collection.Ask;
            case Show:
                return StoryCollectionService.Collection.Show;
            case Jobs:
                return StoryCollectionService.Collection.Jobs;
            default:
                return StoryCollectionService.Collection.Top;
        }
    }
}
