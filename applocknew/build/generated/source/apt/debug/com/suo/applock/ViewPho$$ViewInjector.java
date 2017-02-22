// Generated code from Butter Knife. Do not modify!
package com.suo.applock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class ViewPho$$ViewInjector<T extends com.suo.applock.ViewPho> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findOptionalView(source, 2131624109, null);
    target.icon = finder.castView(view, 2131624109, "field 'icon'");
    view = finder.findOptionalView(source, 2131624110, null);
    target.appName = finder.castView(view, 2131624110, "field 'appName'");
    view = finder.findOptionalView(source, 2131624111, null);
    target.encrypted = view;
    view = finder.findOptionalView(source, 2131624115, null);
    target.blockIcon = finder.castView(view, 2131624115, "field 'blockIcon'");
    view = finder.findOptionalView(source, 2131624113, null);
    target.simName = finder.castView(view, 2131624113, "field 'simName'");
    view = finder.findOptionalView(source, 2131624112, null);
    target.title_date = finder.castView(view, 2131624112, "field 'title_date'");
    view = finder.findOptionalView(source, 2131624147, null);
    target.box = finder.castView(view, 2131624147, "field 'box'");
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
