package com.sarath.easyandroid.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.RawRes;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by sarath on 22/11/16.
 *
 * Creates or upgrades database
 */

public class DatabaseManager {

    private static final String CREATE = "create_db";
    private static final String UPGRADE = "upgrade_db";

    private static void queryExecutor(Context context, @RawRes int resId,SQLiteDatabase database){
        String sql = IOHelper.readResourceAsString(context, resId);
        executeQueries(sql.split(";"),database);
    }
    private static void executeQueries(String[] queries, SQLiteDatabase database){
        for (String query : queries) {
            String str = query.replace("\n", "").trim().replace("\t", "");
            if (str.length() > 0) {
                database.execSQL(query);
            }
        }
    }

    /**
     * Creates the database by executing the queries in resource R.raw.create_db
     *
     * @param context
     * @param database
     */
    public static void createDB(Context context, SQLiteDatabase database){
        int identifier = context.getResources().getIdentifier(CREATE, "raw",
                context.getPackageName());
        if (identifier > 0) {
            queryExecutor(context,identifier,database);
        } else {
            Log.e(DatabaseManager.class.getClass().getName(),
                    "impossible upgrade db file is not exist " + CREATE);
        }
    }

    /**
     * Updates the database by executing the queries in resources R.raw.upgrade_db_{oldVersion},...
     * R.raw.upgrade_db_{newVersion}
     *
     * @param context
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    public static void upgradeDB(Context context, SQLiteDatabase database, int oldVersion,
                                 int newVersion){

        for (int i = oldVersion; i <= newVersion; i++) {
            String filename = "upgrade_db_" + i;
            int identifier = context.getResources().getIdentifier(filename,
                    "raw", context.getPackageName());
            if (identifier > 0) {
                Log.d(DatabaseManager.class.getClass().getName(),
                        "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
                queryExecutor(context,identifier,database);
            } else {
                Log.e(DatabaseManager.class.getClass().getSimpleName(),
                        "impossible upgrade db file is not exist " + filename);
            }
        }
    }


    private static class IOHelper {

        static final String SEPARATOR = "\n";

        static String readResourceAsString(Context context, int resourceId) {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String str;
            try {
                while ((str = br.readLine()) != null) {
                    sb.append(str).append(SEPARATOR);
                }
            } catch (IOException e) {
                Log.d(IOHelper.class.getSimpleName(), "error loading resource by id = " + resourceId, e);
                throw new RuntimeException(e);
            }
            return sb.toString();
        }
    }


}
