package com.udangtangtang.haveibeen.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private final static String TAG="imageDB";
    private Context mContext;

    /* DB Structure
    / filename -> varchar(255)
    / locName -> varchar(255)
    / latitude -> double
    / longtitude -> double
    / address -> varchar(255)
    / date -> datetime
    / rating -> float
    / comment -> varchar(255)
    */

    public final String TABLE_NAME="myDB";
    public final String FILE_NAME="filename";
    public final String LOCATION_NAME="locName";
    public final String LATITUDE="latitude";
    public final String LONGTITUDE="longtitude";
    public final String ADDRESS="address";
    public final String DATE="date";
    public final String RATING="rating";
    public final String COMMENT="comment";


    public DBHelper(Context context) {
        super(context, "commentDB", null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table myDB (filename varchar(255), locName varchar(255), latitude varchar(255), longtitude varchar(255), address varchar(255), date datetime, rating float, comment varchar(255));");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists myDB;");
        onCreate(db);
    }


    // DB 초기화
    public void initializeDB() {
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        myHelper.onUpgrade(sqlDB, 1, 2);
        sqlDB.close();
        Toast.makeText(mContext, "DB 초기화 됨", Toast.LENGTH_LONG).show();
    }

    // DB가 존재하는 지 확인하고 없을 때만 DB 초기화
    public void isInitialDB(){
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getReadableDatabase();
        Cursor cursor=sqlDB.rawQuery("select name from sqlite_master where type='table' AND name='myDB'", null);
        if(cursor!=null && cursor.moveToFirst()){
            // DB가 이미 존재 하므로 Pass
        }else{
            Toast.makeText(mContext, "DB가 존재합니다.", Toast.LENGTH_SHORT).show();
            initializeDB();
        }
        sqlDB.close();
    }

    // 기록 수정 시 업데이트
    public void updateDB(String fileName, String locationName, float rating, String comment){
        DBHelper myHelper=new DBHelper(mContext);
        SQLiteDatabase sqlDB=myHelper.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(LOCATION_NAME, locationName);
        contentValues.put(RATING, rating);
        contentValues.put(COMMENT, comment);
        sqlDB.update(TABLE_NAME, contentValues, FILE_NAME+"=?", new String[]{fileName});
        sqlDB.close();
    }

    // DB에 사진 파일명 추가
    public void insertImageToDB(String fileName, double latitude, double longtitude, String address, String datetime) {
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();

        ContentValues contentValues=new ContentValues();
        contentValues.put(FILE_NAME, fileName);
        contentValues.put(LATITUDE, String.valueOf(latitude));
        contentValues.put(LONGTITUDE, String.valueOf(longtitude));
        contentValues.put(ADDRESS, address);
        sqlDB.insert(TABLE_NAME, null, contentValues);
        sqlDB.close();
        Log.i(TAG, "Image file inserted to DB : "+fileName+" latitude : "+String.valueOf(latitude)+" longtitude : "+String.valueOf(longtitude)+" address : "+address+" datetime : "+datetime);
    }

    // 좌표가 있는 이미지 리스트 개수 반환
    public int getSizeOfPictureDB(){
        Cursor cursor;
        int result=0;
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        cursor=sqlDB.rawQuery("select count(filename) from myDB;", null);
        while(cursor.moveToNext()){
            Log.d("dbCount", String.valueOf(cursor.getInt(0)));
            result=cursor.getInt(0);
        }
        sqlDB.close();
        return result;
    }

    public ArrayList<String> getSameLocationPictures(String latitude, String longtitude){
        // 같은 위/경도의 이미지 목록을 내보낼 ArrayList 선언
        ArrayList<String> sameLocationPictures=new ArrayList<>();

        // DB에서 입력받은 위/경도를 갖는 사진 파일들 조회
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        Cursor cursor;
        String[] params={latitude, longtitude};
        cursor = sqlDB.rawQuery("select filename from myDB where latitude=? AND longtitude=?;", params);
        if(cursor!=null && cursor.moveToFirst()){
            // 1회 실행 후 while에서 moveToNext()
            Log.i("test", "Adding same location pictures : "+cursor.getString(0));
            sameLocationPictures.add(cursor.getString(0));
            while(cursor.moveToNext()){
                Log.i("test", "Adding same location pictures : "+cursor.getString(0));
                sameLocationPictures.add(cursor.getString(0));
            }
        }

        return sameLocationPictures;
    }

    public void searchDB() {
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("select * from myDB;", null);

        String string1 = "Movie Title" + System.lineSeparator();
        String string2 = "Director" + System.lineSeparator();
        String string3 = "Released Year" + System.lineSeparator();

        string1 += "-----------" + System.lineSeparator();
        string2 += "-----------" + System.lineSeparator();
        string3 += "-----------" + System.lineSeparator();

        while (cursor.moveToNext()) {
            string1 += cursor.getString(0) + System.lineSeparator();
            string2 += cursor.getString(1) + System.lineSeparator();
            string3 += cursor.getString(2) + System.lineSeparator();
        }

        //TODO : 여기 수정
//        binding.textTitleColumnOne.setText(string1);
//        binding.textTitleColumnTwo.setText(string2);
//        binding.textTitleColumnThree.setText(string3);

        cursor.close();
        sqlDB.close();
    }


    // 모든 데이터 출력
    public void showAllData(){
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("select * from myDB;", null);

        if(cursor.getCount() == 0) {
            //show message
            Log.i("test", "there is no data.");
            return;

        }
        StringBuffer buffer = new StringBuffer();
        while(cursor.moveToNext()){
            buffer.append(FILE_NAME+ cursor.getString(0) + "\n");
            buffer.append(LOCATION_NAME+ cursor.getString(1) + "\n");
            buffer.append(LATITUDE+ cursor.getString(2) + "\n");
            buffer.append(LONGTITUDE+ cursor.getString(3) + "\n");
            buffer.append(ADDRESS+ cursor.getString(4) + "\n");
            buffer.append(DATE+ cursor.getString(5) + "\n");
            buffer.append(RATING + cursor.getString(6) + "\n");
            buffer.append(COMMENT+ cursor.getString(7) + "\n\n");

        }
        // Show all data
        Log.i("test",buffer.toString());

    }

}