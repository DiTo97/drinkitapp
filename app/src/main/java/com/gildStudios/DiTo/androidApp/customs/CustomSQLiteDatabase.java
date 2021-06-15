package com.gildStudios.DiTo.androidApp.customs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.DrinkItApplication;
import com.gildStudios.DiTo.androidApp.Glass;
import com.gildStudios.DiTo.androidApp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class CustomSQLiteDatabase extends SQLiteOpenHelper {
    private static final int SQLite_dbVersion = 1;

    private static final String SQLite_dbName = "app_dbInit.db";
    private static final String SQLite_dbPath = "/data/user/0/com.gildStudios.DiTo.androidApp/databases/";

    private static final String TAG = "CustomSQLiteDatabase";

    private SQLiteDatabase mDatabase;

    private Context mContext;

    public CustomSQLiteDatabase(Context appContext) {
        super(appContext, SQLite_dbName, null, SQLite_dbVersion);

        mContext = appContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public boolean checkDatabase() {
        File dbFile = mContext.getDatabasePath(SQLite_dbName);

        return dbFile.exists();
    }

    private boolean openReadableDB() {
        return openDatabase(true);
    }

    private boolean openWritableDB() {
        return openDatabase(false);
    }

    private synchronized void closeDatabase() {
        if(mDatabase != null)
            mDatabase.close();
        SQLiteDatabase.releaseMemory();
    }

    private void closeCursor(Cursor queryCursor) {
        if(queryCursor != null)
            queryCursor.close();
    }

    public void createDatabase() throws IOException {
        boolean databaseExists = checkDatabase();

        if(!databaseExists) {
            updateDatabase();
        }
    }

    public void updateDatabase() throws IOException {
        this.getReadableDatabase();

        try {
            copyDatabase();
        } catch(IOException mIOException) {

            String dbCopyFailed = DrinkItApplication.getContext()
                    .getString(R.string.error_copyDatabaseFail, SQLite_dbPath);
            throw new Error(dbCopyFailed);
        } finally {
            this.closeDatabase();
        }
    }

    private void copyDatabase() throws IOException {
        try {
            InputStream mInputStream = mContext.getAssets().open("databases/" + SQLite_dbName);
            String outFileName = SQLite_dbPath + SQLite_dbName;

            OutputStream mOutputStream = new FileOutputStream(outFileName, false);
            byte[] streamBuffer = new byte[1024];
            int byteRead;
            while((byteRead = mInputStream.read(streamBuffer)) > 0) {
                mOutputStream.write(streamBuffer, 0, byteRead);
            }
            mOutputStream.flush();
            mOutputStream.close();

            mInputStream.close();
        } catch(Exception copyDbException) {
            copyDbException.printStackTrace();
        }
    }

    private boolean openDatabase(boolean readOnly) throws SQLiteException {
        String mPath = SQLite_dbPath + SQLite_dbName;

        mDatabase = SQLiteDatabase.openDatabase(mPath, null, readOnly
                ? SQLiteDatabase.OPEN_READONLY
                : SQLiteDatabase.OPEN_READWRITE);

        return mDatabase.isOpen();
    }

    private String getStringResourceByTag(String resourceTag) {
        Context appContext = DrinkItApplication.getContext();
        String packageName = appContext.getPackageName();

        int resourceId = appContext.getResources()
                .getIdentifier(resourceTag, "string", packageName);

        return appContext.getString(resourceId);
    }

    // Table Drinks constants
    private final String DRINK_tableName = "Drinks";

    private final String DRINK_idCol = "id";
    private final int DRINK_idColNumber = 0;

    private final String DRINK_nameCol = "name";
    private final int DRINK_nameColNumber = 1;

    private final String DRINK_ingredientsCol = "ingredients";
    private final int DRINK_ingredientsColNumber = 2;

    private final String DRINK_taxCol = "alcoholemicTax";
    private final int DRINK_taxColNumber = 3;

    // Table Drinks functions
    public Drink getDrink(int drinkId) {
        String where = DRINK_idCol + "= ?";
        String[] whereArgs = { Long.toString(drinkId) };

        this.openReadableDB();
        Cursor cursor = mDatabase.query(DRINK_tableName,
                null, where, whereArgs, null, null, null);
        cursor.moveToFirst();
        Drink drink = getDrinkFromCursor(cursor);
        this.closeCursor(cursor);
        this.closeDatabase();

        return drink;
    }

    // TODO_ Add a remoteTag field
    public ArrayList<Drink> getAllDrinks() {
        ArrayList<Drink> drinkList = new ArrayList<>();
        String selectQuery = "SELECT * FROM Drinks";

        if(!this.openReadableDB()) {
            return null;
        }

        Cursor drinksCursor = mDatabase.rawQuery(selectQuery, null);
        if(drinksCursor.moveToFirst()) {
            do {
                drinkList.add(getDrinkFromCursor(drinksCursor));
            } while(drinksCursor.moveToNext());
        }
        this.closeCursor(drinksCursor);
        this.closeDatabase();

        return drinkList;
    }

    private Drink getDrinkFromCursor(Cursor drinkCursor) {
        if(drinkCursor == null || drinkCursor.getCount() == 0){
            return null;
        } else {
            try {
                String drinkTag  = drinkCursor.getString(DRINK_nameColNumber);
                String drinkName = getStringResourceByTag(drinkTag);
                return new Drink(drinkTag, drinkName,
                        drinkCursor.getDouble(DRINK_taxColNumber));
            } catch(Exception readDbException) {
                readDbException.printStackTrace();
                return null;
            }
        }
    }

    // Table DrinkLimits functions
    public float getAlcoholLimit(String countryCode) {
        if(!this.openReadableDB()) {
            return -2;
        }

        String where = "countryCode = ?";
        String[] whereArgs = { countryCode };

        float alcoholLimit = -1;

        Cursor limitCursor = mDatabase.query("DrinkLimits",
                null, where, whereArgs, null, null, null);
        if(limitCursor.moveToFirst()) {
            alcoholLimit = limitCursor.getFloat(limitCursor
                    .getColumnIndex("alcoholLimit"));
        }
        this.closeCursor(limitCursor);
        this.closeDatabase();

        return alcoholLimit;
    }

    // Table Glasses constants
    private final String GLASS_tableName = "Glasses";

    private final String GLASS_idCol          = "id";
    private final int GLASS_idColNumber       = 0;

    private final String GLASS_labelCol       = "label";
    private final int GLASS_labelColNumber    = 1;

    private final String GLASS_capacityCol    = "capacity";
    private final int GLASS_capacityColNumber = 2;

    // Table Glasses methods
    public ArrayList<Glass> getAllGlasses() {
        ArrayList<Glass> glassesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + GLASS_tableName;

        if(!this.openReadableDB()) {
            return null;
        }

        Cursor glassesCursor = mDatabase.rawQuery(selectQuery, null);
        if(glassesCursor.moveToFirst()) {
            do {
                glassesList.add(getGlassFromCursor(glassesCursor));
            } while(glassesCursor.moveToNext());
        }
        this.closeCursor(glassesCursor);
        this.closeDatabase();

        return glassesList;
    }

    private Glass getGlassFromCursor(Cursor glassCursor) {
        if(glassCursor == null || glassCursor.getCount() == 0){
            return null;
        } else {
            try {
                return new Glass(
                        getStringResourceByTag(glassCursor.getString(GLASS_labelColNumber)),
                        glassCursor.getDouble(GLASS_capacityColNumber));
            } catch(Exception readDbException) {
                readDbException.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
        super.onOpen(db);
    }
}
