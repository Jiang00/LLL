// Generated code from Butter Knife. Do not modify!
package com.security.securitylock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class ViewPho$$ViewInjector<T extends com.security.securitylock.ViewPho> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findOptionalView(source, 2131624076, null);
    target.icon = finder.castView(view, 2131624076, "field 'icon'");
    view = finder.findOptionalView(source, 2131624077, null);
    target.appName = finder.castView(view, 2131624077, "field 'appName'");
    view = finder.findOptionalView(source, 2131624078, null);
    target.encrypted = view;
    view = finder.findOptionalView(source, 2131624082, null);
    target.blockIcon = finder.castView(view, 2131624082, "field 'blockIcon'");
    view = finder.findOptionalView(source, 2131624080, null);
    target.simName = finder.castView(view, 2131624080, "field 'simName'");
    view = finder.findOptionalView(source, 2131624079, null);
    target.title_date = finder.castView(view, 2131624079, "field 'title_date'");
    view = finder.findOptionalView(source, 2131624114, null);
    target.box = finder.castView(view, 2131624114, "field 'box'");
  }

  @Override public void reset(T target) {
    target.icon = null;
    target.appName = null;
    target.encrypted = null;
    target.blockIcon = null;
    target.simName = null;
    target.title_date = null;
    target.box = null;
  }
}
