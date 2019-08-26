package com.exampl.varvara.malifaux;

/* Главная активность
*/

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    //Список констант
    public static final String POSITION_PLAYER = "position_player";
    public static final String ROUNDS = "rounds";
    public static final String ROUND_NOW = "rounds_now";

    public static final String SAVE = "save";
    public static final String SAVE_KEY = "save_key";
    public static final String ROUND_SAVE_KEY = "round_save_key";
    static int saveKey;
    private SharedPreferences save;//сохранение данных
    SQLiteOpenHelper playersDB; //Помошник БД
    SQLiteDatabase db; //БД


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        save = getSharedPreferences(MainActivity.SAVE, Context.MODE_PRIVATE); //Сохранять в файл с именем SAVE

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name)); // Вывести заголовок на панели действий.
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Вертикальное расположение

        // Получаем число из настроек SAVE_KEY
        if (save.contains(SAVE_KEY)) saveKey = save.getInt(SAVE_KEY, 0);

    }

    //нажатие на кнопку newEvent
    public void newEvent(View view){
        if (saveKey==0) startEvent();
        else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.yes_event);
            dialog.setNegativeButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            startEvent();
                        }
                    });
            dialog.setPositiveButton(R.string.no,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.cancel();
                        }
                    });
            dialog.show();
        }
    }

    //Запуск мероприятия
    public void startEvent (){
        try {
            playersDB = new PlayersDB(this); //Помошник БД
            db = playersDB.getWritableDatabase(); //БД
            //Пересоздание БД.
            db.execSQL("DROP TABLE PLAYERS_IN_GAME");
            db.execSQL("CREATE TABLE PLAYERS_IN_GAME (_id INTEGER PRIMARY KEY AUTOINCREMENT, " //Ключ БД
                    + "NAME TEXT, SURNAME TEXT, FRACTION TEXT, " // Имя, фамилия, фракция
                    + "POINTS INTEGER, POINTS_FOR_VICTORY INTEGER , DIFFERENCE INTEGER, " //Учет очков
                    + "POINTS_SUM INTEGER, POINTS_FOR_VICTORY_SUM INTEGER , DIFFERENCE_SUM INTEGER, "
                    + "OPPONENT_1 INTEGER, OPPONENT_2 INTEGER, OPPONENT_3 INTEGER, OPPONENT_4 INTEGER, OPPONENT_5 INTEGER);"); //Учет оппонентов

            //отправка интента
            Intent intent = new Intent(this, Event.class);
            startActivity(intent);

        } catch (SQLiteException e) {
            //Если вылезет это исключение, то появится это сообщение
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //нажатие на кнопку continueEvent
    public void continueEvent(View view) {
        if (saveKey==0){
            Toast toast = Toast.makeText(this, R.string.no_event, Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            switch (saveKey){
                case 1:{
                    Intent intent = new Intent(this, Event.class);
                    startActivity(intent);
                    break;
                }
                case 2:{
                    Intent intent = new Intent(this, Play.class);
                    startActivity(intent);
                    break;
                }
                case 3:{
                    Intent intent = new Intent(this, Result.class);
                    startActivity(intent);
                    break;
                }
            }
        }
    }
}
