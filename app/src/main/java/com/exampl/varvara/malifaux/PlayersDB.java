package com.exampl.varvara.malifaux;

/*  Дополнительный класс.
    Реализация БД.
    БД хранит имена, фамилии, фракции участников, а так же учитывает очки и оппонентов
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlayersDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "PlayersDataBase"; // Имя базы данных
    private static final int DB_VERSION = 1; // Версия базы данных

    //Конструкторы, переопределяем родительский класс
    PlayersDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //Создание новой БД, если БД не существует
    @Override
    public void onCreate(SQLiteDatabase db) {
        //выполняем создание новой таблицы в строчном режиме
        db.execSQL("CREATE TABLE PLAYERS_IN_GAME (_id INTEGER PRIMARY KEY AUTOINCREMENT, " //Ключ БД
                + "NAME TEXT, SURNAME TEXT, FRACTION TEXT, " // Имя, фамилия, фракция
                + "POINTS INTEGER, POINTS_FOR_VICTORY INTEGER , DIFFERENCE INTEGER, " //Учет очков
                + "POINTS_SUM INTEGER, POINTS_FOR_VICTORY_SUM INTEGER , DIFFERENCE_SUM INTEGER, "
                + "OPPONENT_1 INTEGER, OPPONENT_2 INTEGER, OPPONENT_3 INTEGER, OPPONENT_4 INTEGER, OPPONENT_5 INTEGER);"); //Учет оппонентов
    }

    //Обновление БД, если БД существует
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}