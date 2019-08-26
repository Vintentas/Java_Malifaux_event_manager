package com.exampl.varvara.malifaux;

/*  Активность нового игрока.
    Здесь можно добавить, изменить или удлить данные об игроке.
    Данные заносятся в БД.
 */

import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class New_player extends AppCompatActivity  {
    private SQLiteOpenHelper playersDB; //Помошник БД
    private SQLiteDatabase db; //БД
    private Cursor cursor; //курсор
    private TextView nameTextView; //Текстовые поля
    private TextView surnameTextView;
    private String fraction;
    private Spinner fractionTextView;
    private int playerNo; //Целое чтсло, хранит id игрока.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_player);

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name_newPlayer)); // Вывести заголовок на панели действий.
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Вертикальное расположение

        //Получаем ссылки TextView
        nameTextView = (TextView) findViewById(R.id.playerName);
        surnameTextView = (TextView) findViewById(R.id.playerSurname);
        fractionTextView = (Spinner) findViewById(R.id.playerFraction);

        //получение интента, id игрока, если переход выполнен по стоке или 0, если переход выполнен по кнопке
        Intent intent = getIntent();
        playerNo = intent.getIntExtra(MainActivity.POSITION_PLAYER, 0);

        //Если получен id, то текстовые поля заполняются данными из БД
        if (playerNo>0){
            try {
                playersDB = new PlayersDB(this); //Помошник БД
                db = playersDB.getWritableDatabase(); //БД
                //Курсор выбирает 3 столбца из таблицы, с параметром id
                cursor = db.query ("PLAYERS_IN_GAME",
                        new String[] {"_id", "NAME", "SURNAME", "FRACTION"},
                        "_id = ?",new String[] {Integer.toString(playerNo)},
                        null, null,null);
                if (cursor.moveToFirst()) {
                    nameTextView.setText(cursor.getString(1));
                    surnameTextView.setText(cursor.getString(2));
                    fraction = cursor.getString(3);
                    ArrayAdapter adapter = (ArrayAdapter) fractionTextView.getAdapter();
                    int position = adapter.getPosition(fraction);

                    fractionTextView.setSelection(position);
                }
                cursor.close();
                db.close();
            }
            catch (SQLiteException e){
                //Если вылезет это исключение, то появится это сообщение
                Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
            //Изменим надпись на кнопке
            Button button= (Button) findViewById(R.id.newPlayer);
            button.setText(R.string.chang_player);
        }
        else {
            Button button= (Button) findViewById(R.id.deletePlayer);
            button.setText(R.string.cancel_player);
        }
    }

    //Действие по нажатию на кнопку "добавить новыого игрока"
    public void addNewPlayer (View view){
        //Читаем из TextView
        String name = nameTextView.getText().toString();
        String surname = surnameTextView.getText().toString();
        String fraction = fractionTextView.getSelectedItem().toString();

        if (name.length()<1){
            Toast toast = Toast.makeText(this, R.string.not_name, Toast.LENGTH_SHORT);
            toast.show();
        }

        else{

            //Если передается id игрока, то меняем данные в БД
            if (playerNo > 0) {
                try {
                    playersDB = new PlayersDB(this); //Помошник БД
                    db = playersDB.getWritableDatabase(); //БД
                    //передаем строки из текстового поля в БД
                    ContentValues player = new ContentValues(); //Контекстная переменная имя-параметр
                    player.put("NAME", name);
                    player.put("SURNAME", surname);
                    player.put("FRACTION", fraction);
                    db.update("PLAYERS_IN_GAME", player, "_id = ?", new String[]{Integer.toString(playerNo)});
                    db.close();
                } catch (SQLiteException e) {
                    //Если вылезет это исключение, то появится это сообщение
                    Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            //Иначе создаем новую запись
            else {
                try {
                    playersDB = new PlayersDB(this); //Помошник БД
                    db = playersDB.getWritableDatabase(); //БД
                    //передаем строки из текстового поля в БД
                    ContentValues player = new ContentValues(); //Контекстная переменная имя-параметр
                    player.put("NAME", name);
                    player.put("SURNAME", surname);
                    player.put("FRACTION", fraction);
                    player.put("POINTS", 0);
                    player.put("POINTS_FOR_VICTORY", 0);
                    player.put("DIFFERENCE", 0);
                    player.put("POINTS_SUM", 0);
                    player.put("POINTS_FOR_VICTORY_SUM", 0);
                    player.put("DIFFERENCE_SUM", 0);
                    player.put("OPPONENT_1", 0);
                    player.put("OPPONENT_2", 0);
                    player.put("OPPONENT_3", 0);
                    player.put("OPPONENT_4", 0);
                    player.put("OPPONENT_5", 0);

                    db.insert("PLAYERS_IN_GAME", null, player);//добавление строки в БД
                    db.close();
                } catch (SQLiteException e) {
                    //Если вылезет это исключение, то появится это сообщение
                    Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            //Закрываем окно
            this.finish();
        }
    }

    //Действие по нажатию на кнопку "удалить игрока"
    public void deletePlayer (View view){

        //Если был выбран игрок, удаляем его по id
        if (playerNo>0){
            try {
                playersDB = new PlayersDB(this); //Помошник БД
                db = playersDB.getWritableDatabase(); //БД
                db.delete("PLAYERS_IN_GAME", "_id = ?", new String[] {Integer.toString(playerNo)});
                db.close();
            }
            catch (SQLiteException e){
                //Если вылезет это исключение, то появится это сообщение
                Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        //Закрываем окно
        this.finish();
    }
}

