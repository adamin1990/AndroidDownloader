package com.adamin.androiddownloader.multithread;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Adam on 2016/7/30.
 */
public class DbHelper extends SQLiteOpenHelper {

    public DbHelper (Context context){
        super(context,"androiddownload.db",null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE downloadInfo(_id integer primary key autocrement,downPaht varchar(100),threadId integer,downloadLength integer)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
