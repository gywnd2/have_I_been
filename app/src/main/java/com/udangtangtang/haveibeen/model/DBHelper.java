package com.udangtangtang.haveibeen.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private Context mContext;

    public final String RECORD_TABLE_NAME ="myDB";
    public final String ADDRESS_TABLE_NAME = "addressDB";
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
        db.execSQL("create table addressDB(address varchar(255), count int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists myDB;");
        db.execSQL("drop table if exists addressDB;");
        onCreate(db);
    }


    // DB 초기화
    public void initializeDB() {
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        myHelper.onUpgrade(sqlDB, 1, 2);
        sqlDB.close();
    }

    // DB가 존재하는 지 확인하고 없을 때만 DB 초기화
    public void isInitialDB(){
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getReadableDatabase();

        // myDB 존재 확인
        Cursor cursor=sqlDB.rawQuery("select name from sqlite_master where type='table' AND name='myDB'", null);
        if(cursor!=null && cursor.moveToFirst()){
            // DB가 이미 존재 하므로 Pass
        }else{
            // DB 초기화
            initializeDB();
        }

        // addressDB 존재 확인
        cursor=sqlDB.rawQuery("select name from sqlite_master where type='table' AND name='addressDB'", null);
        if(cursor!=null && cursor.moveToFirst()){
            // DB가 이미 존재 하므로 Pass
        }else{
            // DB 초기화
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
        sqlDB.update(RECORD_TABLE_NAME, contentValues, FILE_NAME+"=?", new String[]{fileName});
        sqlDB.close();
    }

    // DB에 사진 파일명 추가
    public void insertImageToDB(String fileName, double latitude, double longtitude, String address, String datetime) {
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();

        // myDB에 이미지 정보 추가
        ContentValues contentValues=new ContentValues();
        contentValues.put(FILE_NAME, fileName);
        contentValues.put(LATITUDE, String.valueOf(latitude));
        contentValues.put(LONGTITUDE, String.valueOf(longtitude));
        contentValues.put(ADDRESS, address);
        contentValues.put(DATE, datetime);
        sqlDB.insert(RECORD_TABLE_NAME, null, contentValues);

        // addressDB에 주소 추가
        contentValues=new ContentValues();
        contentValues.put("address", address);
        contentValues.put("count", 0);
        sqlDB.insert(ADDRESS_TABLE_NAME, null, contentValues);

        sqlDB.close();
      }

    // 좌표가 있는 이미지 리스트 개수 반환
    public int getSizeOfPictureDB(){
        Cursor cursor;
        int result=0;
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        // filename 개수 count 하여 반환
        cursor=sqlDB.rawQuery("select count(filename) from myDB;", null);
        while(cursor.moveToNext()){
            result=cursor.getInt(0);
        }
        sqlDB.close();
        return result;
    }

    public ArrayList<String> getSameLocationPictures(String[] latLng){
        // 같은 위/경도의 이미지 목록을 내보낼 ArrayList 선언
        ArrayList<String> sameLocationPictures=new ArrayList<>();

        // DB에서 입력받은 위/경도를 갖는 사진 파일들 조회
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        Cursor cursor;
        String[] params={latLng[0], latLng[1]};
        cursor = sqlDB.rawQuery("select filename from myDB where latitude=? AND longtitude=?;", params);
        if(cursor!=null && cursor.moveToFirst()){
            // 1회 실행 후 while에서 moveToNext()
            sameLocationPictures.add(cursor.getString(0));
            while(cursor.moveToNext()){
                sameLocationPictures.add(cursor.getString(0));
            }
        }

        return sameLocationPictures;
    }

    // 위/경도로 filename 조회
    public String getFileNameByLatLng(String[] latLng){
        // 받아온 위/경도로 기록 조회
        DBHelper dbHelper=new DBHelper(mContext);
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select filename, locName, rating, comment from myDB where latitude=? AND longtitude=?;", latLng);
        if(cursor!=null && cursor.moveToFirst()){
            return cursor.getString(0);
        }else{
            return null;
        }
    }

    // filename으로 정보창 데이터 받기
    public InfoWindowData getInfoWindowData(String[] latLng){
        InfoWindowData infoWindowData=new InfoWindowData();

        // 받아온 fileName으로 기록 조회
        DBHelper dbHelper=new DBHelper(mContext);
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        String[] columns = {dbHelper.LOCATION_NAME, dbHelper.DATE, dbHelper.COMMENT, dbHelper.RATING};
        String[] params = {latLng[0], latLng[1]};
        Cursor cursor = sqlDB.rawQuery("select locName, address, date, comment, rating from myDB where latitude=? AND longtitude=?;", params);

        // 데이터 내용 표시
        if (cursor != null && cursor.moveToFirst()) {
            infoWindowData.setLocationName(cursor.getString(0));
            infoWindowData.setAddress(cursor.getString(1));
            infoWindowData.setDatetime(cursor.getString(2));
            infoWindowData.setComment(cursor.getString(3));
            infoWindowData.setRating(cursor.getFloat(4));
        }

        return infoWindowData;
    }

    // filename으로 locName, rating, comment 찾기
    public RecordData getRecordData(String filename){
        RecordData recordData=new RecordData();

        // 받아온 fileName으로 기록 조회
        DBHelper dbHelper=new DBHelper(mContext);
        SQLiteDatabase sqlDB = dbHelper.getReadableDatabase();
        String[] columns = {dbHelper.FILE_NAME,dbHelper.LOCATION_NAME, dbHelper.RATING, dbHelper.COMMENT};
        String[] params = {filename};
        Cursor cursor = sqlDB.query(dbHelper.RECORD_TABLE_NAME, columns, dbHelper.FILE_NAME + "=?", params, null, null, null);

        // 데이터 내용 표시
        if (cursor != null && cursor.moveToFirst()) {
            recordData.setFileName(cursor.getString(0));
            recordData.setLocationName(cursor.getString(1));
            recordData.setRating(cursor.getFloat(2));
            recordData.setComment(cursor.getString(3));
        }

        // DB연결 종료
        sqlDB.close();
        return recordData;
    }

    // 모든 주소 정보 리턴
    public ArrayList<String> getAllAddressInDB() {
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        ArrayList<String> addressList=new ArrayList<>();
        Cursor cursor;
        // 모든 주소 조회
        cursor = sqlDB.rawQuery("select * from addressDB;", null);
        while (cursor.moveToNext()) {
            addressList.add(cursor.getString(0));
        }
        return addressList;
    }

    // 모든 데이터 출력
    public void showAllData(){
        DBHelper myHelper = new DBHelper(mContext);
        SQLiteDatabase sqlDB = myHelper.getWritableDatabase();
        Cursor cursor;
        cursor = sqlDB.rawQuery("select * from myDB;", null);

        if(cursor.getCount() == 0) {
            //show message
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