package com.suo.applock;

/**
 * Created by superjoy on 2014/11/5.
 */
public class Unlockactivity extends MainActivity {

    @Override
    public void setupView() {
            super.setupView();
        MyTrack.sendEvent(MyTrack.CATE_DEFAULT, MyTrack.ACT_UNLOCK, MyTrack.ACT_UNLOCK, 1L);
    }
}