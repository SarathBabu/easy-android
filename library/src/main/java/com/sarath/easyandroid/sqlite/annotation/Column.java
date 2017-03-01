package com.sarath.easyandroid.sqlite.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sarath on 17/11/16.
 */

@Retention(RetentionPolicy.RUNTIME) @Inherited
public @interface Column {
    String name();
}
