package com.hexforhn.hex.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.hexforhn.hex.HexApplication;
import com.hexforhn.hex.R;
import com.hexforhn.hex.adapter.CommentListAdapter;
import com.hexforhn.hex.model.Comment;
import com.hexforhn.hex.model.Story;
import com.hexforhn.hex.net.hexapi.StoryService;
import com.hexforhn.hex.viewmodel.CommentViewModel;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class CommentsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Single mGetStory;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_comments, container,
                false);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.commentList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mGetStory = getStory();

        setupStoriesUnavailableView(rootView);
        setupRefreshLayout(rootView);

        loadComments();

        return rootView;
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

    private void loadComments() {
        mGetStory.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(createCommentsLoadedHandler());
    }

    private SingleObserver createCommentsLoadedHandler() {
        return new SingleObserver<Story>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Story story) {
                List<CommentViewModel> viewComments = new ArrayList<>();

                for (Comment comment : story.getComments()) {
                    addCommentToList(comment, viewComments, 0);
                }

                CommentListAdapter cla = new CommentListAdapter(getActivity(), viewComments);
                mLayoutManager = new LinearLayoutManager(getActivity());
                mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(cla);

                showCommentList();
                hideCommentsUnavailable();

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                mSwipeRefreshLayout.setRefreshing(false);
                hideCommentList();
                showCommentsUnavailable();
            }
        };
    }

    private String getStoryId() {
        return this.getArguments().getString("STORY_ID");
    }

    private void addCommentToList(Comment comment, List<CommentViewModel> list, int depth) {
        list.add(new CommentViewModel(comment.getUser(), comment.getText(), depth,
                comment.getCommentCount(), comment.getDate()));

        for (com.hexforhn.hex.model.Comment childComment : comment.getChildComments()) {
            addCommentToList(childComment, list, depth + 1);
        }
    }

    private void setupStoriesUnavailableView(View rootView) {
        ((TextView) rootView.findViewById((R.id.loading_failed_text)))
                .setText(R.string.error_unableToLoadComments);
        Button tryAgain = (Button) rootView.findViewById(R.id.try_again);
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mGetStory.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(createCommentsLoadedHandler());
            }
        });
    }

    private void setupRefreshLayout(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mGetStory.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(createCommentsLoadedHandler());
                    }
                }
        );
    }

    private void showCommentList() {
        getView().findViewById(R.id.commentList).setVisibility(View.VISIBLE);
    }

    private void hideCommentList() {
        getView().findViewById(R.id.commentList).setVisibility(View.GONE);
    }

    private void showCommentsUnavailable() {
        getView().findViewById(R.id.content_unavailable).setVisibility(View.VISIBLE);
    }

    private void hideCommentsUnavailable() {
        getView().findViewById(R.id.content_unavailable).setVisibility(View.GONE);
    }
}
