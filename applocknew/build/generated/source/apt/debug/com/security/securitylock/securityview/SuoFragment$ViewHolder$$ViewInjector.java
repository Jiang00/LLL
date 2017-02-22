// Generated code from Butter Knife. Do not modify!
package com.security.securitylock.securityview;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class SuoFragment$ViewHolder$$ViewInjector<T extends com.security.securitylock.securityview.SuoFragment.ViewHolder> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624076, "field 'icon'");
    target.icon = finder.castView(view, 2131624076, "field 'icon'");
    view = finder.findRequiredView(source, 2131624077, "field 'name'");
    target.name = finder.castView(view, 2131624077, "field 'name'");
    view = finder.findRequiredView(source, 2131624105, "field 'lock'");
    target.lock = finder.castView(view, 2131624105, "field 'lock'");
  }

  @Override public void reset(T target) {
    target.icon = null;
    target.name = null;
    target.lock = null;
  }
}
