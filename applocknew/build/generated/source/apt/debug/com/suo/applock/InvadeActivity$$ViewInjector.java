// Generated code from Butter Knife. Do not modify!
package com.suo.applock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class InvadeActivity$$ViewInjector<T extends com.suo.applock.InvadeActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624106, "field 'listView'");
    target.listView = finder.castView(view, 2131624106, "field 'listView'");
    view = finder.findRequiredView(source, 2131623956, "field 'tip'");
    target.tip = finder.castView(view, 2131623956, "field 'tip'");
    view = finder.findRequiredView(source, 2131623973, "field 'title'");
    target.title = finder.castView(view, 2131623973, "field 'title'");
    view = finder.findRequiredView(source, 2131624157, "field 'help'");
    target.help = finder.castView(view, 2131624157, "field 'help'");
  }

  @Override public void reset(T target) {
    target.listView = null;
    target.tip = null;
    target.title = null;
    target.help = null;
  }
}
