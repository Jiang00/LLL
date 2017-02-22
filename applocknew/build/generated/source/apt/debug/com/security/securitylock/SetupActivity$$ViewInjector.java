// Generated code from Butter Knife. Do not modify!
package com.security.securitylock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class SetupActivity$$ViewInjector<T extends com.security.securitylock.SetupActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131623969, "field 'cancel'");
    target.cancel = finder.castView(view, 2131623969, "field 'cancel'");
    view = finder.findRequiredView(source, 2131623954, "field 'tip'");
    target.tip = finder.castView(view, 2131623954, "field 'tip'");
  }

  @Override public void reset(T target) {
    target.cancel = null;
    target.tip = null;
  }
}
