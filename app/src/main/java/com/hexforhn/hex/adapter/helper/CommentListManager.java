package com.hexforhn.hex.adapter.helper;

import com.hexforhn.hex.viewmodel.CommentViewModel;

import java.util.ArrayList;
import java.util.List;

public class CommentListManager {
    private final List<CommentViewModel> mComments;

    public CommentListManager(List<CommentViewModel> comments) {
        mComments = comments;
    }

    public List<CommentViewModel> getVisibleComments() {
        List<CommentViewModel> comments = new ArrayList<>();
        int commentCount = mComments.size();

        for (int i = 0; i < commentCount; i++) {
            CommentViewModel comment = mComments.get(i);

            if (comment.isVisible()) {
                comments.add(comment);
            }
        }

        return comments;
    }

    public void toggleCommentAtPosition(int position) {
        CommentViewModel commentToToggle = getVisibleComments().get(position);
        int commentCount = mComments.size();

        commentToToggle.toggleCollapsed();

        int toggledCommentDepth = commentToToggle.getDepth();
        boolean collapsedState = commentToToggle.isCollapsed();

        position = mComments.indexOf(commentToToggle);

        for (int i = position + 1; i < commentCount; i++) {
            CommentViewModel comment = mComments.get(i);

            if (comment.getDepth() > toggledCommentDepth) {
                comment.setVisible(!collapsedState);
                comment.setCollapsed(collapsedState);
            } else {
                break;
            }
        }
    }

}
