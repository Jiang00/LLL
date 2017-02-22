package com.lockscreen.api.liberal.clis;

import android.widget.AbsListView;

import com.lockscreen.api.liberal.Utils;

/**
 * Created by SongHualin on 6/12/2015.
 */
public class CCViewScroller {
    boolean scrolling = false;

    public CCViewScroller(final AbsListView listView) {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE){
                    scrolling = false;
                    Utils.notifyDataSetChanged(view);
                } else {
                    scrolling = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    public boolean isScrolling(){
        return scrolling;
    }
}
