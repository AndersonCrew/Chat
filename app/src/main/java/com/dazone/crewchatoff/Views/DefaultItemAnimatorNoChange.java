package com.dazone.crewchatoff.Views;

import android.support.v7.widget.DefaultItemAnimator;

/**
 * Created by maidinh on 1/3/2017.
 */

public class DefaultItemAnimatorNoChange extends DefaultItemAnimator {
    public DefaultItemAnimatorNoChange() {
        setSupportsChangeAnimations(false);
    }
}