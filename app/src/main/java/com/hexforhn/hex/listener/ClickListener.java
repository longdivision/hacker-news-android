package com.hexforhn.hex.listener;

import android.view.View;

public interface ClickListener {

    /**
     * Called when the view is clicked.
     *
     * @param v view that is clicked
     * @param position of the clicked item
     * @param isLongClick true if long click, false otherwise
     */
    void onClick(View v, int position, boolean isLongClick);
}
