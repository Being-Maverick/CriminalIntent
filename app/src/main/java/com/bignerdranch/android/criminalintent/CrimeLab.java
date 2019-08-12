package com.bignerdranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import database.CrimeBaseHelper;
import database.CrimeCursorWrapper;
import database.CrimeDbSchema;

public class CrimeLab {

    private static CrimeLab sCrimeLab;
    // private List<Crime> mCrimes;
    // private HashMap<UUID,Crime> map;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context){
        if(sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context){

        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
        // mCrimes = new ArrayList<>();
        // map = new HashMap<>();
        // for (int i = 0; i < 100; i++) {
        //    Crime crime = new Crime();
        //    crime.setTitle("Crime #" + i);
        //    crime.setSolved(i % 2 == 0);
        //    map.put(crime.getId(),crime);
            //crime.setRequiresPolice(i%2 == 0);
        //    mCrimes.add(crime);
        //}
    }

    public List<Crime> getCrimes(){
        // return mCrimes;
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return crimes;
    }

    public Crime getCrime(UUID id){
        // for(Crime crime: mCrimes){
        //    if(crime.getId().equals(id)){
        //        return crime;
        //    }
        //}
        // return map.get(id);
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeDbSchema.CrimeTable.COLS.UUID + " = ?",
                new String[] { id.toString() }
        );
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void addCrime(Crime c){
        // mCrimes.add(c);
        // map.put(c.getId(),c);
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeDbSchema.CrimeTable.NAME,null,values);
    }

    public void deleteCrime(Crime c){
        if(c == null){
            return;
        }
        ContentValues values = getContentValues(c);
        mDatabase.delete(CrimeDbSchema.CrimeTable.NAME, CrimeDbSchema.CrimeTable.COLS.UUID + "= ?",new String[]{ values.get(CrimeDbSchema.CrimeTable.COLS.UUID).toString()});
        // mCrimes.remove(c);
        // map.remove(c.getId());
    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeDbSchema.CrimeTable.NAME,values, CrimeDbSchema.CrimeTable.COLS.UUID + "= ?",new String[]{uuidString});
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();
        values.put(CrimeDbSchema.CrimeTable.COLS.UUID,crime.getId().toString());
        values.put(CrimeDbSchema.CrimeTable.COLS.TITLE,crime.getTitle());
        values.put(CrimeDbSchema.CrimeTable.COLS.DATE,crime.getDate().getTime());
        values.put(CrimeDbSchema.CrimeTable.COLS.SOLVED,crime.isSolved()? 1:0);
        values.put(CrimeDbSchema.CrimeTable.COLS.SUSPECT, crime.getSuspect());

        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(CrimeDbSchema.CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null,
                null);

        return new CrimeCursorWrapper(cursor);
    }

    public File getPhotoFile(Crime crime){
        File filesDir = mContext.getFilesDir();
        return new File(filesDir,crime.getPhotoFilename());
    }
}
