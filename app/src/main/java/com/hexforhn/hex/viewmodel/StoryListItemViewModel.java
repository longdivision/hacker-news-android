package com.hexforhn.hex.viewmodel;

import android.text.format.DateUtils;

import java.util.Date;


public class StoryListItemViewModel {

    private final String mTitle;
    private final String mDomain;
    private final int mScore;
    private final int mCommentCount;
    private final Date mDate;

    public StoryListItemViewModel(String title, String domain, int score, int commentCount, Date date) {
        this.mTitle = title;
        this.mDomain = domain;
        this.mScore = score;
        this.mCommentCount = commentCount;
        this.mDate = date;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getDomain() {
        return this.mDomain;
    }

    public String getScore() {
        return String.valueOf(this.mScore);
    }

    public String getCommentCount() {
        return String.valueOf(this.mCommentCount);
    }

    public String getRelativeTime() {
        return DateUtils.getRelativeTimeSpanString(mDate.getTime(), System.currentTimeMillis(), 0)
                .toString();

    }
}
