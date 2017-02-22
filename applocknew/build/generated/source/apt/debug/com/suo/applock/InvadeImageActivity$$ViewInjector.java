// Generated code from Butter Knife. Do not modify!
package com.suo.applock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class InvadeImageActivity$$ViewInjector<T extends com.suo.applock.InvadeImageActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624120, "field 'blockIcon'");
    target.blockIcon = finder.castView(view, 2131624120, "field 'blockIcon'");
    view = finder.findRequiredView(source, 2131624122, "field 'blockImage'");
    target.blockImage = finder.castView(view, 2131624122, "field 'blockImage'");
    view = finder.findRequiredView(source, 2131624123, "field 'dateIcon'");
    target.dateIcon = finder.castView(view, 2131624123, "field 'dateIcon'");
    view = finder.findRequiredView(source, 2131624113, "field 'dateView'");
    target.dateView = finder.castView(view, 2131624113, "field 'dateView'");
    view = finder.findRequiredView(source, 2131624121, "field 'messageView'");
    target.messageView = finder.castView(view, 2131624121, "field 'messageView'");
    view = finder.findRequiredView(source, 2131623973, "field 'title'");
    target.title = finder.castView(view, 2131623973, "field 'title'");
    view = finder.findRequiredView(source, 2131624156, "field 'edit_mode'");
    target.edit_mode = finder.castView(view, 2131624156, "field 'edit_mode'");
    view = finder.findRequiredView(source, 2131624157, "field 'delete'");
    target.delete = finder.castView(view, 2131624157, "field 'delete'");
  }

  @Override public void reset(T target) {
    target.blockIcon = null;
    target.blockImage = null;
    target.dateIcon = null;
    target.dateView = null;
    target.messageView = null;
    target.title = null;
    target.edit_mode = null;
    target.delete = null;
  }
}
