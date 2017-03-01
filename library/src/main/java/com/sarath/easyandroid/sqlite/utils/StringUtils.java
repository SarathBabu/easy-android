package com.sarath.easyandroid.sqlite.utils;

import com.google.common.base.CaseFormat;

/**
 * Created by sarath on 17/11/16.
 */

public class StringUtils {
    public static String changeCase(String s){
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,s);
    }
}
