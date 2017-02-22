// Generated code from Butter Knife. Do not modify!
package com.security.securitylock;

import android.view.View;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.Injector;

public class InvadeImageActivity$$ViewInjector<T extends com.security.securitylock.InvadeImageActivity> implements Injector<T> {
  @Override public void inject(final Finder finder, final T target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131624087, "field 'blockIcon'");
    target.blockIcon = finder.castView(view, 2131624087, "field 'blockIcon'");
    view = finder.findRequiredView(source, 2131624089, "field 'blockImage'");
    target.blockImage = finder.castView(view, 2131624089, "field 'blockImage'");
    view = finder.findRequiredView(source, 2131624090, "field 'dateIcon'");
    target.dateIcon = finder.castView(view, 2131624090, "field 'dateIcon'");
    view = finder.findRequiredView(source, 2131624080, "field 'dateView'");
    target.dateView = finder.castView(view, 2131624080, "field 'dateView'");
    view = finder.findRequiredView(source, 2131624088, "field 'messageView'");
    target.messageView = finder.castView(view, 2131624088, "field 'messageView'");
    view = finder.findRequiredView(source, 2131623971, "field 'title'");
    target.title = finder.castView(view, 2131623971, "field 'title'");
    view = finder.findRequiredView(source, 2131624124, "field 'edit_mode'");
    target.edit_mode = finder.castView(view, 2131624124, "field 'edit_mode'");
    view = finder.findRequiredView(source, 2131624125, "field 'delete'");
    target.delete = finder.castView(view, 2131624125, "field 'delete'");
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
