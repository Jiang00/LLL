// Generated code from Butter Knife. Do not modify!
package com.suo.applock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class SuoMain$$ViewInjector<T extends com.suo.applock.SuoMain> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624136, "field 'float_action_menu'");
    target.float_action_menu = finder.castView(view, 2131624136, "field 'float_action_menu'");
    view = finder.findRequiredView(source, 2131624135, "field 'black_bg'");
    target.black_bg = view;
  }

  @Override public void reset(T target) {
    target.float_action_menu = null;
    target.black_bg = null;
  }
}
