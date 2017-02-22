// Generated code from Butter Knife. Do not modify!
package com.security.securitylock.securityview;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class SuoFragment$$ViewInjector<T extends com.security.securitylock.securityview.SuoFragment> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624091, "field 'refreshLayout'");
    target.refreshLayout = finder.castView(view, 2131624091, "field 'refreshLayout'");
    view = finder.findRequiredView(source, 2131624073, "field 'listView' and method 'onItemClick'");
    target.listView = finder.castView(view, 2131624073, "field 'listView'");
    ((android.widget.AdapterView<?>) view).setOnItemClickListener(
      new android.widget.AdapterView.OnItemClickListener() {
        @Override public void onItemClick(
          android.widget.AdapterView<?> p0,
          android.view.View p1,
          int p2,
          long p3
        ) {
          target.onItemClick(p1, p2);
        }
      });
  }

  @Override public void reset(T target) {
    target.refreshLayout = null;
    target.listView = null;
  }
}
