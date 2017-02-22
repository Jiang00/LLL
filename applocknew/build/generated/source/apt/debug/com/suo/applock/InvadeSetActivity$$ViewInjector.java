// Generated code from Butter Knife. Do not modify!
package com.suo.applock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class InvadeSetActivity$$ViewInjector<T extends com.suo.applock.InvadeSetActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131623973, "field 'title'");
    target.title = finder.castView(view, 2131623973, "field 'title'");
  }

  @Override public void reset(T target) {
    target.title = null;
  }
}
