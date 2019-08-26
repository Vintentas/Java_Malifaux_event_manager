package com.exampl.varvara.malifaux;

/* Активность начала ивента.
    Тут выводится список игроков, Количество раундов
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter;
import android.widget.CursorAdapter;


public class Event extends AppCompatActivity {

    private SQLiteOpenHelper playersDB; //Помошник БД
    private SQLiteDatabase db; //БД
    private Cursor cursor; //Курсор БД
    private CursorAdapter аdapter; //адаптер курсора
    private ListView listPlayers; //Списковое представление
    private int sumPlayersInGame; //количество игроков
    private int rounds; //количество раундов
    private SharedPreferences save;//сохранение данных

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        MainActivity.saveKey = 1; //Записали ключ состояния
        save = getSharedPreferences(MainActivity.SAVE, Context.MODE_PRIVATE); //Сохранять в файл с именем SAVE

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name_event)); // Вывести заголовок на панели действий.
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Вертикальное расположение

        //Слушатель для ListView
        listPlayers = (ListView) findViewById(R.id.list_players);
        listPlayers.setOnItemClickListener(itemClickListener);

        //Заполнение спискового представления
        listViewFilling ();
        //Заполнение поля количества игроков
        sumPlayers();

    }

    @Override
    public void onRestart(){
        super.onRestart();
        //Заполнение спискового представления
        listViewFilling ();
        //Заполнение поля количества игроков
        sumPlayers();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }

    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences.Editor editor = save.edit();
        editor.putInt(MainActivity.SAVE_KEY, MainActivity.saveKey);
        editor.apply();
    }


    //Заполнение спискового пердставления из БД
    public void listViewFilling (){
        //Запалнение ListView из БД
        try {
            playersDB = new PlayersDB(this); //Помошник БД
            db = playersDB.getReadableDatabase(); //БД
            //Курсор выбирает 3 столбца из таблицы, без параметров
            cursor = db.query ("PLAYERS_IN_GAME",
                    new String[] {"_id", "NAME", "SURNAME", "FRACTION"},
                    null,null, null, null,null);
            //адаптер БД
            аdapter = new SimpleCursorAdapter(this,
                    R.layout.list_event,
                    cursor,
                    new String[]{"NAME", "SURNAME", "FRACTION"},
                    new int[]{R.id.text_name, R.id.text_surname, R.id.text_fraction}, 0);
            //Установка адаптера
            listPlayers.setAdapter(аdapter);
        }
        catch (SQLiteException e){
            //Если вылезет это исключение, то появится это сообщение
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Заполнение поля количества игроков
    public void sumPlayers (){
         try {
         playersDB = new PlayersDB(this); //Помошник БД
         db = playersDB.getReadableDatabase(); //БД
         //Курсор считает количество участников
         cursor = db.query ("PLAYERS_IN_GAME",
                 new String[] {"COUNT(_id) AS COUNTER"},
                null,null, null, null,null);
         //Выводит количество в TextView
         if (cursor.moveToFirst()) {
                String count=cursor.getString(0);
                TextView namberPlayers = (TextView) findViewById(R.id.namberPlayers);
                namberPlayers.setText(count);
                sumPlayersInGame = Integer.parseInt(count); //Количество игроков
        }
    }
    catch (SQLiteException e){
        //Если вылезет это исключение, то появится это сообщение
        Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
        toast.show();
    }
}


    //Действие по нажатию на кнопку "добавить новыого игрока"
    public void addPlayer (View view){
        //отправка интента к New_player
        Intent intent = new Intent(this, New_player.class);
        intent.putExtra(MainActivity.POSITION_PLAYER, (int) 0);
        startActivity(intent);
    }

    //Действие по нажатию на имя в списке
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
            //Отправка интента к New_player
            Intent intent = new Intent(Event.this, New_player.class);
            intent.putExtra(MainActivity.POSITION_PLAYER, (int) id);//Вложенный номер игрока
            startActivity(intent);
        }
    };

    //Действие по нажатию на кнопку "Запустить ивент"
    public void startEvent (View view){
        Intent intent = new Intent(Event.this, Play.class);

        //Проверяем кол-во раундов
        Spinner roundsSpinner = (Spinner) findViewById(R.id.spinner);
        rounds = (int) roundsSpinner.getSelectedItemPosition(); //Кол-во раундов
        //Если раундов 3, участников должно быть не меньше 4
        if (rounds==0){
            if (sumPlayersInGame < 4){
                Toast toast = Toast.makeText(this, R.string.not_enough_players_1, Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                //Сохранение rounds и roundNow
                SharedPreferences.Editor editor = save.edit();
                editor.putInt(MainActivity.ROUNDS, 3);
                editor.putInt(MainActivity.ROUND_NOW, 1);
                editor.putInt(MainActivity.ROUND_SAVE_KEY, 0);
                editor.apply();
                //отправляем интент к Result c номером кол-ва раундов
                startActivity(intent);
                this.finish();
            }
        }
        //Если раундов 4, участников должно быть не меньше 9
        else if (rounds==1){
            if (sumPlayersInGame < 9){
                Toast toast = Toast.makeText(this, R.string.not_enough_players_2, Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                //Сохранение rounds и roundNow
                SharedPreferences.Editor editor = save.edit();
                editor.putInt(MainActivity.ROUNDS, 4);
                editor.putInt(MainActivity.ROUND_NOW, 1);
                editor.putInt(MainActivity.ROUND_SAVE_KEY, 0);
                editor.apply();
                //отправляем интент к Result c номером кол-ва раундов
                startActivity(intent);
                this.finish();
            }
        }
        //Если раундов 5, участников должно быть не меньше 16
        else if (rounds==2){
            if (sumPlayersInGame < 16){
                Toast toast = Toast.makeText(this, R.string.not_enough_players_3, Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                //Сохранение rounds и roundNow
                SharedPreferences.Editor editor = save.edit();
                editor.putInt(MainActivity.ROUNDS, 5);
                editor.putInt(MainActivity.ROUND_NOW, 1);
                editor.putInt(MainActivity.ROUND_SAVE_KEY, 0);
                editor.apply();
                //отправляем интент к Result c номером кол-ва раундов
                startActivity(intent);
                this.finish();
            }
        }
    }



}
