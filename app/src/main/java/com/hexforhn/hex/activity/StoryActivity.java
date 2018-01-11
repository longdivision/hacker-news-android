package com.hexforhn.hex.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.hexforhn.hex.HexApplication;
import com.hexforhn.hex.R;
import com.hexforhn.hex.adapter.StorySlidePagerAdapter;
import com.hexforhn.hex.fragment.ArticleFragment;
import com.hexforhn.hex.model.Story;
import com.hexforhn.hex.net.hexapi.StoryService;
import com.hexforhn.hex.util.ThemeHelper;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Callable;

public class StoryActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, TabLayout.OnTabSelectedListener {
    private final static String STORY_TITLE_INTENT_EXTRA_NAME = "storyTitle";
    private final static String STORY_ID_INTENT_EXTRA_NAME = "storyId";
    private TabLayout mTabLayout;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private Single mGetStory;

    private enum Page {WEBVIEW, COMMENTS}

    private Page mPage = Page.WEBVIEW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_story);

        mGetStory = getStory();
        mTabLayout = setupTabLayout();
        mPagerAdapter = createPageAdapter(getSupportFragmentManager(), getStoryId());
        mPager = setupPaging(mPagerAdapter);

        setupToolbar();
        loadArticleTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeHelper.updateTheme(this);
    }

    private void setupToolbar() {
        String storyTitle = getAnyProvidedStoryTitle();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(storyTitle);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    private TabLayout setupTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.articleTabTitle));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.commentTabTitle));
        tabLayout.setOnTabSelectedListener(this);

        return tabLayout;
    }

    private StorySlidePagerAdapter createPageAdapter(FragmentManager fragmentManager, String storyId) {
        return new StorySlidePagerAdapter(fragmentManager, storyId);
    }

    private ViewPager setupPaging(PagerAdapter pagerAdapter) {
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(this);

        return pager;
    }

    private String getStoryId() {
        Intent intent = this.getIntent();
        String storyId = intent.getStringExtra(STORY_ID_INTENT_EXTRA_NAME);

        if (storyId == null) {
            final Uri data = intent.getData();
            storyId = data.getQueryParameter("id");
        }

        return storyId;
    }

    private String getAnyProvidedStoryTitle() {
        Intent intent = this.getIntent();
        String storyTitle = intent.getStringExtra(STORY_TITLE_INTENT_EXTRA_NAME);

        if (storyTitle == null) {
            storyTitle = "";
        }

        return storyTitle;
    }

    private void loadArticleTitle() {
        SingleObserver observer = new SingleObserver<Story>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Story story) {
                getSupportActionBar().setTitle(story.getTitle());
            }

            @Override
            public void onError(Throwable e) {
            }
        };

        mGetStory.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private Single getStory() {
        return Single.fromCallable(new Callable<Story>() {
            @Override
            public Story call() {
                HexApplication application = (HexApplication) getApplication();
                StoryService service = new StoryService(application.getRequestQueue(), application.apiBaseUrl);
                return service.getStory(getStoryId());
            }
        });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mTabLayout.getTabAt(position).select();
        mPage = Page.values()[position];
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_story_icons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        } else if (item.getItemId() == R.id.action_share) {
            handleShareRequest();
        }

        return true;
    }

    private void handleShareRequest() {
        SingleObserver observer = new SingleObserver<Story>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Story story) {
                String intentMessage = getString(R.string.shareArticle);
                String url = story.getUrl();

                if (mPage.equals(Page.COMMENTS)) {
                    intentMessage = getString(R.string.shareComments);
                    url = story.getCommentsUrl();
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(Intent.createChooser(shareIntent, intentMessage));
            }

            @Override
            public void onError(Throwable e) {
            }
        };

        mGetStory.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void onBackPressed() {
        boolean backHandled = false;

        if (mPage.equals(Page.WEBVIEW)) {
            ArticleFragment storyListFragment = (ArticleFragment) (((StorySlidePagerAdapter)
                    mPagerAdapter).getItem(0));

            backHandled = storyListFragment.handleBack();
        }


        if (!backHandled) {
            super.onBackPressed();
        }
    }

}
