package com.hexforhn.hex.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hexforhn.hex.R;
import com.hexforhn.hex.adapter.helper.CommentListManager;
import com.hexforhn.hex.adapter.helper.TextHelper;
import com.hexforhn.hex.listener.ClickListener;
import com.hexforhn.hex.viewmodel.CommentViewModel;

import java.util.List;


public class CommentListAdapter extends RecyclerView.Adapter<ViewHolder> implements ClickListener {

    private final static int INDENT_SIZE = 5;
    private final Context mContext;
    private final CommentListManager mCommentListManager;

    public CommentListAdapter(Context context, List<CommentViewModel> comments) {
        mContext = context;
        mCommentListManager = new CommentListManager(comments);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.comment, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setClickListener(this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CommentViewModel comment = mCommentListManager.getVisibleComments().get(position);
        renderCommentIntoView(comment, holder.mView);
    }

    @Override
    public int getItemCount() {
        return mCommentListManager.getVisibleComments().size();
    }

    @Override
    public void onClick(View view, int position, boolean isLongClick) {
        mCommentListManager.toggleCommentAtPosition(position);
        notifyDataSetChanged();
    }

    private void renderCommentIntoView(CommentViewModel comment, View commentView) {
        TextView usernameView = (TextView) commentView.findViewById(R.id.username);
        TextView relativeTimeView = (TextView) commentView.findViewById(R.id.relativeTime);
        final TextView textView = (TextView) commentView.findViewById(R.id.text);
        LinearLayout indentView = (LinearLayout) commentView.findViewById(R.id.indent);
        View childCommentsHidden = commentView.findViewById(R.id.hiddenChildCommentsMarker);

        usernameView.setText(comment.getUser());
        relativeTimeView.setText(comment.getRelativeTime());
        textView.setText(TextHelper.removeTrailingNewlinesFromText(Html.fromHtml(comment.getText())));
        setupIndent(indentView, comment.getDepth());
        setupClickListenerForTextView(textView);

        if (comment.isCollapsed() && comment.getCommentCount() > 0) {
            TextView commentCount = (TextView) commentView.findViewById(R.id.childCommentCount);
            commentCount.setText(String.valueOf(comment.getCommentCount()));
            childCommentsHidden.setVisibility(View.VISIBLE);
        } else {
            childCommentsHidden.setVisibility(View.GONE);
        }
    }

    /**
     * The comment body TextView is setup for autolinking. A special handler is used to ensure
     * non-link clicks are captured and bubbled to the parent to simulate a click.
     */
    private void setupClickListenerForTextView(final TextView textView) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Non-autolinked text will have a value of -1 for selectionStart and selectionEnd
                if (textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                    View commentWrapper = (View) textView.getParent().getParent();
                    commentWrapper.performClick();
                }
            }
        });
    }

    private void setupIndent(View indentView, int depth) {
        LinearLayout indentBarView = (LinearLayout) indentView.findViewById(R.id.indent_bar);

        if (depth <= 0) {
            hideIndent(indentBarView, indentView);
        } else {
            positionAndColorIndentForDepth(indentBarView, indentView, depth);
        }
    }

    private void hideIndent(View indentBarView, View indentView) {
        indentView.getLayoutParams().width = 0;

        indentView.setVisibility(View.INVISIBLE);
        indentBarView.setVisibility(View.INVISIBLE);
    }

    private void positionAndColorIndentForDepth(View indentBarView, View indentView, int depth) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        int indentSize = depth * INDENT_SIZE;
        indentView.getLayoutParams().width = Math.round(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, indentSize, metrics));

        int[] indentColors = mContext.getResources().getIntArray(R.array.comment);
        int indentColor = indentColors[depth % indentColors.length];
        indentBarView.setBackgroundColor(indentColor);

        indentView.setVisibility(View.VISIBLE);
        indentBarView.setVisibility(View.VISIBLE);
    }

}
