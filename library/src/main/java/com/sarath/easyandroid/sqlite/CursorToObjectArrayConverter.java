package com.sarath.easyandroid.sqlite;

import android.database.Cursor;


import com.sarath.easyandroid.sqlite.annotation.Column;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by sarath on 17/11/16.
 */

public class CursorToObjectArrayConverter {

    public static<T> T[] convertToArray(Cursor cursor, Class<T> aClass) throws IllegalAccessException, InstantiationException {
        T[] ts = (T[]) Array.newInstance(aClass, cursor.getCount());
        cursor.moveToFirst();
        int i=0;
        while (!cursor.isAfterLast()) {
            T t = aClass.newInstance();
            for(Field field:aClass.getFields()){
                putIntoObject(t,cursor,field);
            }
            ts[i++] = t;
            cursor.moveToNext();
        }
        return ts;
    }

    private static void putIntoObject(Object object, Cursor cursor, Field field){
        if(field.getAnnotation(Column.class).getClass() !=null){
            try {
                if (field.getType() ==(String.class)) {
                    field.set(object, cursor.getString(cursor.getColumnIndex(QueryManager.getColumnName(field))));
                } else if (field.getType() ==(Integer.class)) {
                    field.set(object, cursor.getInt(cursor.getColumnIndex(QueryManager.getColumnName(field))));
                } else if (field.getType() ==(Boolean.class)) {
                    field.set(object,cursor.getInt(cursor.getColumnIndex(QueryManager.getColumnName(field))) == 1);
                }else if (field.getType() ==(Long.class)) {
                    field.set(object,cursor.getLong(cursor.getColumnIndex(QueryManager.getColumnName(field))));
                } else if (field.getType() ==(Float.class)) {
                    field.set(object, cursor.getFloat(cursor.getColumnIndex(QueryManager.getColumnName(field))));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
