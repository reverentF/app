package jp.ac.titech.itpro.sdl.tsuyoso2.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mitsuaki on 16/11/03.
 * 使い方
 * Context を引数に初期化
 */
public class LocalDatabaseService {
    private DatabaseHelper mDbHelper;

    public LocalDatabaseService(Context ctx){
        this.mDbHelper = new DatabaseHelper(ctx);
    }


    /**
     * 下記のパラメータを指定すると新規に登録される
     * @param recipeId
     * @param recipeName
     * @param cookDate
     * @param evaluation
     */
    public void saveSingleData(int recipeId,
                               String recipeName,
                               String cookDate,
                               int evaluation
    ){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.RECIPE_ID, recipeId);
        values.put(DatabaseHelper.RECIPE_NAME, recipeName);
        String cookdate = cookDate.replaceAll("-","");
        values.put(DatabaseHelper.COOK_DATE, cookdate);
        values.put(DatabaseHelper.EVALUATION, evaluation);

        try {
            db.insert(DatabaseHelper.TABLE_NAME, null, values);
        } catch (Exception e) {

        } finally {
            db.close();
        }
    }

    public void updateData(int recipeId,
                           String recipeName,
                           String cookDate,
                           int evaluation
    ){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.RECIPE_ID, recipeId);
        values.put(DatabaseHelper.RECIPE_NAME, recipeName);
        String cookdate = cookDate.replaceAll("-","");
        values.put(DatabaseHelper.COOK_DATE, cookdate);
        values.put(DatabaseHelper.EVALUATION, evaluation);

        try {
            db.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COOK_DATE + " == " + cookdate, null);
        } catch (Exception e) {

        } finally {
            db.close();
        }
    }

    /**
     * _id (primary key)を指定してレコードを削除する
     */
    public void deleteColumn(String id) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_NAME, "_id = " + id, null);
    }

    /**
     * ローカルのデータベースの情報をすべて削除
     */
    public void resetDatabase() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String select_all = "SELECT * FROM " + DatabaseHelper.TABLE_NAME + ";";
        Cursor c = db.rawQuery(select_all, null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            deleteColumn(c.getString(c.getColumnIndex(DatabaseHelper.ID)));
            c.moveToNext();
        }
    }

    /**
     * date(yyyy-mm-dd)を指定するとその日のメニューのid(int)が帰ってくる
     * @param date
     * @return if exists recipeid else -1
     */
    public int getRecipeIdByDate(String date) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        date = date.replaceAll("-","");
        String select_by_date =
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME
                + " WHERE " + DatabaseHelper.COOK_DATE + " == " + date.replaceAll("-","") + ";";
        Cursor c = db.rawQuery(select_by_date, null);
        c.moveToFirst();
        if(c.getCount() == 0){
            return -1;
        } else {
            return c.getInt(c.getColumnIndex(DatabaseHelper.RECIPE_ID));
        }
    }

    /**
     * date(yyyy-mm-dd)を指定するとその日のメニュー名(String)が帰ってくる
     * @param date
     * @return if exists recipeName else ""
     */
    public String getRecipeNameByDate(String date) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        date = date.replaceAll("-","");
        String select_by_date =
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME
                        + " WHERE " + DatabaseHelper.COOK_DATE + " == " + date.replaceAll("-","") + ";";
        Cursor c = db.rawQuery(select_by_date, null);
        c.moveToFirst();
        if(c.getCount() == 0){
            return "";
        } else {
            return c.getString(c.getColumnIndex(DatabaseHelper.RECIPE_NAME));
        }
    }

    /**
     * startDate と endDate　の間のデータ（Cursor形式）を取得する
     * @param startDate
     * @param endDate
     * @return
     */
    public Cursor getRecipeBetween(String startDate, String endDate) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String start = startDate.replaceAll("-","");
        String end = endDate.replaceAll("-","");
        String select_start_to_end =
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME
                + " WHERE "
                        + DatabaseHelper.COOK_DATE + " >= " + start
                        + " AND "
                        + DatabaseHelper.COOK_DATE + " <= " + end
                + " ORDER BY " + DatabaseHelper.COOK_DATE + " DESC"
                +";";
        Cursor c = db.rawQuery(select_start_to_end, null);
        return c;
    }

    public String getRequestRecipeIds(String date, int days){
        if(days == 0){
            return "\"past_recipe_ids\":[],\"reputations\":[]";
        }
        String endDate = date;
        String startDate = getPastDate(date, days);
        Set<String> pastRecipeIds = new HashSet<>();
        String recipeIds = "\"past_recipe_ids\":";

        Cursor c = getRecipeBetween(startDate, endDate);
        if(c.getCount() == 0){
            return "\"past_recipe_ids\":[],\"reputations\":[]";
        }
        c.moveToFirst();
        while(!c.isLast()) {
            pastRecipeIds.add(c.getString(c.getColumnIndex(DatabaseHelper.RECIPE_ID)));
            c.moveToNext();
        }
        c.moveToFirst();

        if(pastRecipeIds.isEmpty()){
            recipeIds += "[]";
        } else {
            recipeIds += "[";
            for (String str : pastRecipeIds) {
                recipeIds += str + ",";
            }
            recipeIds += "]";
        }

        String reputationList = "\"reputations\":[";
        int i = 0;
        for(String recipeId : pastRecipeIds){
            reputationList += getReputations(date, days, recipeId);
            if(i < pastRecipeIds.size()-1){
                reputationList += ",";
            }
            i++;
        }
        reputationList += "]";
        return recipeIds + "," + reputationList;
    }

    public String getReputations(String date, int days, String recipeId){
        String endDate = date;
        String startDate = getPastDate(date, days);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String start = startDate.replaceAll("-","");
        String end = endDate.replaceAll("-","");
        String select_start_to_end =
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME
                + " WHERE "
                        + DatabaseHelper.COOK_DATE + " >= " + start
                        + " AND "
                        + DatabaseHelper.COOK_DATE + " <= " + end
                        + " AND "
                        + DatabaseHelper.RECIPE_ID + " == " + recipeId
                + " ORDER BY " + DatabaseHelper.COOK_DATE + " DESC"
                +";";
        Cursor c = db.rawQuery(select_start_to_end, null);
        c.moveToFirst();
        double evaluations = 0.0;
        for(int i = 0; i < c.getCount(); i++){
            evaluations += c.getDouble(c.getColumnIndex(DatabaseHelper.EVALUATION));
            c.moveToNext();
        }
        evaluations /= c.getCount();
        String evalAve = Double.toString(evaluations);
        String ret = "{\"recipe_id\":" + recipeId
                + ",\"value\":" + evalAve
                + ",\"proposed_time\":" + Integer.toString(c.getCount())
                + "}";
        return ret;
    }

    public String getPastDate(String date, int days) {
        String ymd[] = date.split("-",0);
        int year = Integer.parseInt(ymd[0]);
        int month = Integer.parseInt(ymd[1]);
        int day = Integer.parseInt(ymd[2]);

        Map<String, Integer> today = new HashMap<String, Integer>();
        today.put("year", year);
        today.put("month", month);
        today.put("day", day);

        for(int i = 0; i < days; i++) {
            today = getLastDay(today);
        }

        year = today.get("year");
        month = today.get("month");
        day = today.get("day");
        return Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
    }

    public Map<String, Integer> getLastDay(Map<String, Integer> ymd) {
        int year = ymd.get("year");
        int month = ymd.get("month");
        int day = ymd.get("day");
        int bigNext[] = {2, 4, 6, 8, 11, 1};
        int smallNext[] = {5, 7, 10, 12};

        if(day > 1) {
            day--;
        } else {
            if(Arrays.asList(bigNext).contains(month)) {
                if(month == 1){
                    year--;
                    month = 12;
                } else {
                    month--;
                }
                day = 31;
            } else if (Arrays.asList(smallNext).contains(month)) {
                month--;
                day = 30;
            } else if (month == 3){
                if(year % 4 == 0 && year % 100 != 0 || year % 400 == 0){
                    day = 29;
                } else {
                    day = 28;
                }
                month--;
            }
        }
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("year", year);
        map.put("month", month);
        map.put("day", day);
        return map;
    }

    /**
    public void saveDummyData(){
        saveSingleData(34, "Hamburg", "2016-11-12", 3);
        saveSingleData(45, "Coffee", "2016-11-13", 4);
        saveSingleData(67, "Rice", "2016-11-14", 3);
        saveSingleData(67, "Rice", "2016-11-18", 7);
        saveSingleData(92, "Tako", "2016-11-15", 5);
        saveSingleData(91, "Sushi", "2016-11-17", 5);
        saveSingleData(91, "Ika", "2016-10-23", 5);
        saveSingleData(91, "Sakana", "2016-10-16", 5);
        saveSingleData(91, "Fish", "2016-10-21", 5);
        saveSingleData(78, "Rice cake", "2016-11-09", 5);
        saveSingleData(89, "Strawberry", "2016-11-22", 5);
        saveSingleData(90, "Beer", "2016-10-14", 5);
        saveSingleData(12, "Curry", "2016-11-10", 1);
        saveSingleData(23, "Stew", "2016-11-11", 2);


    }


    public List<String> getAllData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql_request =
                "SELECT * FROM " + DatabaseHelper.TABLE_NAME
                        + " ORDER BY " + DatabaseHelper.COOK_DATE + " DESC" + ";";
        List<String> allData = new ArrayList<>();

        Cursor c = db.rawQuery(sql_request, null);
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            String query = "[";
            query += c.getString(c.getColumnIndex(DatabaseHelper.ID));
            query += ", ";
            query += c.getString(c.getColumnIndex(DatabaseHelper.RECIPE_ID));
            query += ", ";
            query += c.getString(c.getColumnIndex(DatabaseHelper.RECIPE_NAME));
            query += ", ";
            query += c.getString(c.getColumnIndex(DatabaseHelper.COOK_DATE));
            query += ", ";
            query += c.getString(c.getColumnIndex(DatabaseHelper.EVALUATION));
            query += "]";
            allData.add(query);
            c.moveToNext();
        }
        if (allData.isEmpty()) {
            return Collections.emptyList();
        } else {
            return allData;
        }
    }
     */

}
