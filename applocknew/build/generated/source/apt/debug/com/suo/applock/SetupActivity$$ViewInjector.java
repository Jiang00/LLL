// Generated code from Butter Knife. Do not modify!
package com.suo.applock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class SetupActivity$$ViewInjector<T extends com.suo.applock.SetupActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131623971, "field 'cancel'");
    target.cancel = finder.castView(view, 2131623971, "field 'cancel'");
    view = finder.findRequiredView(source, 2131623956, "field 'tip'");
    target.tip = finder.castView(view, 2131623956, "field 'tip'");
  }

  @Override public void reset(T target) {
    target.cancel = null;
    target.tip = null;
  }
}
