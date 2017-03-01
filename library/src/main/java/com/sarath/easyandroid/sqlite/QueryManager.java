package com.sarath.easyandroid.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sarath.sqlitelibrary.annotation.Column;
import com.sarath.sqlitelibrary.annotation.Table;
import com.sarath.sqlitelibrary.exception.NoColumnAnnotationException;
import com.sarath.sqlitelibrary.exception.NoTableAnnotationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sarath on 17/11/16.
 */

public class QueryManager {

    private final SQLiteDatabase mSqLiteDatabase;

    public QueryManager(SQLiteDatabase database){
        mSqLiteDatabase = database;
    }

    public <T> long insert(T t) throws NoTableAnnotationException {
        Table classAnnotation = t.getClass().getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",t.getClass().getName()));
        }
        return mSqLiteDatabase.insert(classAnnotation.name(),null,getContentValues(t));
    }

    public <T> long update(Class<T> tClass,T t,ContentValues values,String whereClause, String[] whereArgs) throws NoTableAnnotationException {
        Table classAnnotation = tClass.getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",t.getClass().getName()));
        }
        return mSqLiteDatabase.update(classAnnotation.name(),values,whereClause,whereArgs);
    }

    public <T> long update(Class<T> tClass,T t,String fieldsToUpdate[],String[] fieldsToSkip, String whereClause, String[] whereArgs) throws NoTableAnnotationException {
        Table classAnnotation = tClass.getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",t.getClass().getName()));
        }
        ContentValues values = getContentValuesForUpdate(t,fieldsToUpdate,fieldsToSkip);
        Log.d(getClass().getName(),values.toString());
        return mSqLiteDatabase.update(classAnnotation.name(),values,whereClause,whereArgs);
    }

    public <T> List<T> query(Class<T> tClass, String selection, String[] selectionArgs,
                            String groupBy, String having, String orderBy,
                            String limit) throws NoTableAnnotationException {
        List<T> list = new ArrayList<>();
        Table classAnnotation = tClass.getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",tClass.getName()));
        }
        Cursor  cursor= mSqLiteDatabase.query(classAnnotation.name(),getColumns(tClass),selection,selectionArgs,groupBy,having,orderBy,limit);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            try {
                list.add(cursorToObject(tClass,cursor));
            } catch (Exception  e) {
                e.printStackTrace();
            }
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    private  <T> T cursorToObject(Class<T> tClass, Cursor cursor) throws IllegalAccessException,
            InstantiationException, NoTableAnnotationException {
        T t = tClass.newInstance();
        Table classAnnotation = tClass.getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",tClass.getName()));
        }
        for(Field field:tClass.getFields()) {
            if (field.getAnnotation(Column.class)!=null) {
                field.set(t,getCorrectObjectForField(field,cursor));
            }
        }
        return t;
    }

    private Object getCorrectObjectForField(Field field, Cursor cursor){
        try {
            int coulmnIndex = cursor.getColumnIndex(getColumnName(field));
            if (field.getType()==(String.class)) {
                return cursor.getString(coulmnIndex);
            } else if (field.getType()==(Integer.class)) {
                return cursor.getInt(coulmnIndex);
            } else if (field.getType()==(Boolean.class)) {
                return cursor.getInt(coulmnIndex)==1;
            } else if (field.getType()==(Float.class)) {
                return cursor.getFloat(coulmnIndex);
            } else if (field.getType()==(Long.class)) {
                return cursor.getLong(coulmnIndex);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public <T> long count(Class<T> tClass) throws NoTableAnnotationException {
        Table classAnnotation = tClass.getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",tClass.getName()));
        }
        Cursor cursor= mSqLiteDatabase.rawQuery(String.format("SELECT Count(*) FROM %s",classAnnotation.name()),null);
        cursor.moveToFirst();
        long count = 0;
        if(!cursor.isAfterLast()){
            count = cursor.getLong(0);
        }
        cursor.close();
        return count;
    }




    @NonNull
    private static <T> String[] getColumns(Class<T> tClass) throws NoTableAnnotationException {
        ArrayList<String> strings = new ArrayList<>();
        Table classAnnotation = tClass.getAnnotation(Table.class);
        if(classAnnotation == null){
            throw new NoTableAnnotationException(String.format("%s is not annotated with @Table",tClass.getName()));
        }
        for(Field field:tClass.getFields()) {
            if (field.getAnnotation(Column.class)!=null) {
                strings.add(getColumnName(field));
            }
        }
        return strings.toArray(new String[strings.size()]);
    }

    @NonNull
    private static ContentValues getContentValues(Object object){
        ContentValues contentValues = new ContentValues();
        for(Field field:object.getClass().getFields()){
            putIntoContentValues(contentValues,field,object);
        }
        return contentValues;
    }

    @NonNull
    private static ContentValues getContentValuesForUpdate(Object object, String[] fieldsToUpdate, String[] fieldsToSkip){
        ContentValues contentValues = new ContentValues();
        if(fieldsToUpdate!=null) Arrays.sort(fieldsToUpdate);
        if(fieldsToSkip!=null) Arrays.sort(fieldsToSkip);
        for(Field field:object.getClass().getFields()){
            if(field.getAnnotation(Column.class)!=null) {
                if (fieldsToSkip != null) {
                    int searchResult = Arrays.binarySearch(fieldsToSkip, getColumnName(field));
                    if (searchResult >= 0 && searchResult < fieldsToSkip.length) {
                        continue;
                    }
                }
                if (fieldsToUpdate != null) {
                    int searchResult = Arrays.binarySearch(fieldsToUpdate, getColumnName(field));
                    if (searchResult >= 0 && searchResult < fieldsToUpdate.length) {
                        putIntoContentValues(contentValues, field, object);
                    }
                } else {
                    putIntoContentValues(contentValues, field, object);
                }
            }
        }
        return contentValues;
    }

    private static void putIntoContentValues(ContentValues values, Field field, Object object){
        if(field.getAnnotation(Column.class)!=null){
            try {
                if (field.getType()==(String.class)) {
                    values.put(getColumnName(field), (String) field.get(object));
                } else if (field.getType()==(Integer.class)) {
                    values.put(getColumnName(field), (Integer) field.get(object));
                } else if (field.getType()==(Boolean.class)) {
                    values.put(getColumnName(field), (Boolean) field.get(object));
                } else if (field.getType()==(Float.class)) {
                    values.put(getColumnName(field), (Float) field.get(object));
                } else if (field.getType()==(Long.class)) {
                    values.put(getColumnName(field), (Long) field.get(object));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String getColumnName(Field field){
        Column column =field.getAnnotation(Column.class);
        return column.name();
    }

}
