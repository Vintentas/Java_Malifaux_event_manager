package com.exampl.varvara.malifaux;

/* Результирующая активность
    Тут выводятся данные о прошедшей/текущей (между раундами) игре, результаты игроков
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Result extends AppCompatActivity {

    private SQLiteOpenHelper playersDB; //Помошник БД
    private SQLiteDatabase db; //БД
    private Cursor cursor; //Курсор БД
    private CursorAdapter аdapter; //адаптер курсора
    private ListView listPlayers; //Списковое представление
    private TextView nowRoundTextView; //Информационное поле текущего раунда
    private TextView allRoundTextView; //Информационное поле кол-ва раундов
    private TextView namberPlayersTextView; //Информационное поле кол-ва игроков
    private Button button;
    private int rounds, roundNow; //количество раундов, текущий раунд
    private int sumPlayersInGame; //количество игроков в игре
    private SharedPreferences save;//сохранение данных


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        MainActivity.saveKey = 3; //Записали ключ состояния
        save = getSharedPreferences(MainActivity.SAVE, Context.MODE_PRIVATE); //Сохранять в файл с именем SAVE

        //получение интента, количество раундов
     /*   Intent intent = getIntent();
        rounds = intent.getIntExtra(MainActivity.ROUNDS, 0);
        roundNow = intent.getIntExtra(MainActivity.ROUND_NOW, 0);
*/
        // Получаем число из настроек SAVE_KEY
        if (save.contains(MainActivity.ROUNDS)) rounds = save.getInt(MainActivity.ROUNDS, 0);
        if (save.contains(MainActivity.ROUND_NOW)) roundNow = save.getInt(MainActivity.ROUND_NOW, 0);

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name_Result)); // Вывести заголовок на панели действий.
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Вертикальное расположение

        //Слушатель для ListView
        listPlayers = (ListView) findViewById(R.id.list_players); //Списковое представлени
        listPlayers.setOnItemClickListener(itemClickListener);

        //Заполнение спискового пердставления
        filling();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        db.close();
        cursor.close();
    }

    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences.Editor editor = save.edit();
        editor.putInt(MainActivity.SAVE_KEY, MainActivity.saveKey);
        editor.apply();
    }


    //Заполнение спискового пердставления и информационной шапки
    public void filling (){
        //Запалнение ListView из БД
        try {
            playersDB = new PlayersDB(this); //Помошник БД
            db = playersDB.getReadableDatabase(); //БД
            //Курсор выбирает 3 столбца из таблицы, без параметров
            cursor = db.query ("PLAYERS_IN_GAME",
                    new String[] {"_id", "NAME", "SURNAME", "FRACTION", "POINTS_SUM", "POINTS_FOR_VICTORY_SUM" , "DIFFERENCE_SUM"},
                    null,null, null, null,"POINTS_FOR_VICTORY_SUM DESC, POINTS_SUM DESC, DIFFERENCE_SUM DESC");
            //адаптер БД
            аdapter = new SimpleCursorAdapter(this,
                    R.layout.list_result,
                    cursor,
                    new String[]{"NAME", "SURNAME", "FRACTION", "POINTS_FOR_VICTORY_SUM", "POINTS_SUM" , "DIFFERENCE_SUM"},
                    new int[]{R.id.text_name, R.id.text_surname, R.id.text_fraction, R.id.text_result1, R.id.text_result2, R.id.text_result3}, 0);
            //Установка адаптера
            listPlayers.setAdapter(аdapter);

            //Записываем кол-во игроков
            cursor = db.query ("PLAYERS_IN_GAME",
                    new String[] {"COUNT(_id) AS COUNTER"},
                    null,null, null, null,null);
            if (cursor.moveToFirst()) {
                sumPlayersInGame = Integer.parseInt(cursor.getString(0)); //Количество игроков
            }
        }
        catch (SQLiteException e){
            //Если вылезет это исключение, то появится это сообщение
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //Записываем переменные информационных текстовых полей
        nowRoundTextView = (TextView) findViewById(R.id.nowRound); //Информационное поле текущего раунда
        allRoundTextView = (TextView) findViewById(R.id.allRound); //Информационное поле кол-ва раундов
        namberPlayersTextView = (TextView) findViewById(R.id.namberPlayers); //Информационное поле кол-ва игроков
        nowRoundTextView.setText(Integer.toString(roundNow));
        allRoundTextView.setText(Integer.toString(rounds));
        namberPlayersTextView.setText(Integer.toString(sumPlayersInGame));

        //Заполнение кнопки
        button = (Button) findViewById(R.id.button);
        if (roundNow == rounds) button.setText(R.string.finish_event);
        else button.setText(R.string.next_round);

    }


    //Действие по нажатию на кнопке: "следующий раунт/завершить ивент"
    public void nextRound(View view) {
        if (roundNow != rounds){

            SharedPreferences.Editor editor = save.edit();
            editor.putInt(MainActivity.ROUNDS, rounds);
            editor.putInt(MainActivity.ROUND_NOW, roundNow+1);
            editor.apply();


            Intent intent = new Intent(Result.this, Play.class);
          //  intent.putExtra(MainActivity.ROUND_NOW, (int) (roundNow+1));
          //  intent.putExtra(MainActivity.ROUNDS, (int) rounds);
            //отправляем интент к Play
            startActivity(intent);
        }
        else {
            MainActivity.saveKey = 0; //Записали ключ состояния
        }
            this.finish();
    }


    //Действие по нажатию на имя в списке - возможность удалить игрока
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {

            final int playerNo = (int) id;

            //Если раундов 3, участников должно быть не меньше 4
            if (rounds==3){
                if (sumPlayersInGame <= 4){
                    Toast toast = Toast.makeText(Result.this, R.string.not_enough_players_1, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Result.this);
                    dialog.setTitle(R.string.delele);
                    dialog.setNegativeButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        playersDB = new PlayersDB(Result.this); //Помошник БД
                                        db = playersDB.getWritableDatabase(); //БД
                                        db.delete("PLAYERS_IN_GAME", "_id = ?", new String[] {Integer.toString(playerNo)});
                                        db.close();
                                    }
                                    catch (SQLiteException e){
                                        //Если вылезет это исключение, то появится это сообщение
                                        Toast toast = Toast.makeText(Result.this, "Database unavailable", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                    filling ();
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
            //Если раундов 4, участников должно быть не меньше 9
            else if (rounds==4){
                if (sumPlayersInGame <= 9){
                    Toast toast = Toast.makeText(Result.this, R.string.not_enough_players_2, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Result.this);
                    dialog.setTitle(R.string.delele);
                    dialog.setNegativeButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        playersDB = new PlayersDB(Result.this); //Помошник БД
                                        db = playersDB.getWritableDatabase(); //БД
                                        db.delete("PLAYERS_IN_GAME", "_id = ?", new String[] {Integer.toString(playerNo)});
                                        db.close();
                                    }
                                    catch (SQLiteException e){
                                        //Если вылезет это исключение, то появится это сообщение
                                        Toast toast = Toast.makeText(Result.this, "Database unavailable", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                    filling ();
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
            //Если раундов 5, участников должно быть не меньше 16
            else if (rounds==5){
                if (sumPlayersInGame <= 16){
                    Toast toast = Toast.makeText(Result.this, R.string.not_enough_players_3, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Result.this);
                    dialog.setTitle(R.string.delele);
                    dialog.setNegativeButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        playersDB = new PlayersDB(Result.this); //Помошник БД
                                        db = playersDB.getWritableDatabase(); //БД
                                        db.delete("PLAYERS_IN_GAME", "_id = ?", new String[] {Integer.toString(playerNo)});
                                        db.close();
                                    }
                                    catch (SQLiteException e){
                                        //Если вылезет это исключение, то появится это сообщение
                                        Toast toast = Toast.makeText(Result.this, "Database unavailable", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                    filling ();
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
        }
    };
}
