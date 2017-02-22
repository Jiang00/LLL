// Generated code from Butter Knife. Do not modify!
package com.security.securitylock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class InvadeActivity$$ViewInjector<T extends com.security.securitylock.InvadeActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624073, "field 'listView'");
    target.listView = finder.castView(view, 2131624073, "field 'listView'");
    view = finder.findRequiredView(source, 2131623954, "field 'tip'");
    target.tip = finder.castView(view, 2131623954, "field 'tip'");
    view = finder.findRequiredView(source, 2131623971, "field 'title'");
    target.title = finder.castView(view, 2131623971, "field 'title'");
    view = finder.findRequiredView(source, 2131624125, "field 'help'");
    target.help = finder.castView(view, 2131624125, "field 'help'");
  }

  @Override public void reset(T target) {
    target.listView = null;
    target.tip = null;
    target.title = null;
    target.help = null;
  }
}
