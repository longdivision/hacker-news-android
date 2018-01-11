package com.hexforhn.hex.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import com.hexforhn.hex.HexApplication;
import com.hexforhn.hex.R;
import com.hexforhn.hex.model.Story;
import com.hexforhn.hex.net.hexapi.StoryService;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.Callable;


public class ArticleFragment extends Fragment {

    private WebView mWebView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Single mGetStory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_article, container, false);

        mGetStory = getStory();
        mSwipeRefreshLayout = setupSwipeRefreshLayout(rootView);
        mWebView = setupWebView(rootView, mSwipeRefreshLayout);

        setupArticleUnavailableView(rootView);
        loadArticle();

        return rootView;
    }

    private WebView setupWebView(View rootView, final SwipeRefreshLayout swipeRefreshLayout) {
        WebView webView = (WebView) rootView.findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                hideArticleUnavailable();
                mSwipeRefreshLayout.setRefreshing(true);
            }

            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (!request.isForMainFrame()) {
                    return;
                }

                swipeRefreshLayout.setRefreshing(false);
                showArticleUnavailable();
            }
        });

        return webView;
    }

    private SwipeRefreshLayout setupSwipeRefreshLayout(View rootView) {
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);

        refreshLayout.setEnabled(false);

        return refreshLayout;
    }

    private void loadArticle() {
        SingleObserver observer = new SingleObserver<Story>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Story story) {
                mWebView.loadUrl(story.getUrl());
                hideArticleUnavailable();
            }

            @Override
            public void onError(Throwable e) {
                showArticleUnavailable();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        };

        mGetStory.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void setupArticleUnavailableView(View rootView) {
        ((TextView) rootView.findViewById((R.id.loading_failed_text)))
                .setText(R.string.error_unableToLoadArticle);
        Button tryAgain = (Button) rootView.findViewById(R.id.try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadArticle();
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private String getStoryId() {
        return this.getArguments().getString("STORY_ID");
    }

    private void showArticleUnavailable() {
        getView().findViewById(R.id.webView).setVisibility(View.GONE);
        getView().findViewById(R.id.content_unavailable).setVisibility(View.VISIBLE);
    }

    private void hideArticleUnavailable() {
        getView().findViewById(R.id.webView).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.content_unavailable).setVisibility(View.GONE);
    }

    private Single getStory() {
        return Single.fromCallable(new Callable<Story>() {
            @Override
            public Story call() {
                HexApplication application = (HexApplication) getActivity().getApplication();
                StoryService service = new StoryService(application.getRequestQueue(), application.apiBaseUrl);
                return service.getStory(getStoryId());
            }
        });
    }

    public boolean handleBack() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return false;
    }
}
