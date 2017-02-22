// Generated code from Butter Knife. Do not modify!
package com.security.securitylock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class InvadeSetActivity$$ViewInjector<T extends com.security.securitylock.InvadeSetActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131623971, "field 'title'");
    target.title = finder.castView(view, 2131623971, "field 'title'");
  }

  @Override public void reset(T target) {
    target.title = null;
  }
}
