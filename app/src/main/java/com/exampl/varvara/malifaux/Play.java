package com.exampl.varvara.malifaux;

/* Активность процесса игры.
    Здесь происходит распределение игроков, запись результатьв, подведение итогов
 */


import android.content.ContentValues;
import android.content.Context;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import com.exampl.varvara.malifaux.Player;

public class Play extends AppCompatActivity {


    private SQLiteOpenHelper playersDB; //Помошник БД
    private SQLiteDatabase db; //БД
    private Cursor cursor; //Курсор БД
    private CursorAdapter аdapter; //адаптер курсора
    private int rounds, roundNow; //количество раундов, текущий раунд
    private ListView listPlayers; //Списковое представление
    private TextView nowRound; //Информационное поле текущего раунда
    private TextView allRound; //Информационное поле кол-ва раундов
    private TextView namberPlayers; //Информационное поле кол-ва игроков
    private Player[] players; //Массив хранит информацию об участниках
    private int sumPlayersInGame; //количество игроков в игре
    static Player [] [] pairPlayers; //Пары участников, ссылки на этементы массива players, состоит либо из div
    private int mod, div; //остаток от деления и целое, будет ли игрок с автопобедой и количество пар участников
    private SharedPreferences save;//сохранение данных
    private int roundSaveKey; //ключ, позволяет запустить сортировку для 1 тура только 1 раз, если 0 - ивент только создан, если 1 - сортировка уже была


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        MainActivity.saveKey = 2; //Записали ключ состояния
        save = getSharedPreferences(MainActivity.SAVE, Context.MODE_PRIVATE); //Сохранять в файл с именем SAVE

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name_Play)); // Вывести заголовок на панели действий.
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Вертикальное расположение

        //Слушатель для ListView
        listPlayers = (ListView) findViewById(R.id.list_players); //Списковое представлени
        listPlayers.setOnItemClickListener(itemClickListener);

        // Получаем число из настроек rounds и roundNow и roundSaveKey
        if (save.contains(MainActivity.ROUNDS)) rounds = save.getInt(MainActivity.ROUNDS, 0);
        if (save.contains(MainActivity.ROUND_NOW)) roundNow = save.getInt(MainActivity.ROUND_NOW, 0);
        if (save.contains(MainActivity.ROUND_SAVE_KEY)) roundSaveKey = save.getInt(MainActivity.ROUND_SAVE_KEY, 0);

         //Заполнение элементов
        if (roundNow==1){//для 1 раунда
            filling();//заполняем переменные и шапку
            if (roundSaveKey==0) {//Если это 1 запуск, то сортируем
                swissSystemForFirstRound ();//Распределяем на пары
                roundSaveKey=1;
            }
            else pair(1);//Иначе только делим на пары без сортировки
        }
        else {//для остальных раундов
            filling();//заполняем переменные и шапку
            swissSystemForOverseasRounds (roundNow);//Распределяем на пары
        }

        fillingListView();//Выводим список

        //автопобеда
        TextView bye = (TextView) findViewById(R.id.bye);
        bye.setText("-");
        for (int i=0; i<sumPlayersInGame; i++){
            if (players[i].getOpponents(roundNow) == -1) {
                //Запись очков участнику
                players[i].setPoints(5);
                players[i].setPointsFoVictory(3);
                players[i].setDifference(5);
                //Заполнение информационного поля
                bye.setText(players[i].getName() +" "+ players[i].getSurname() +
                            " 5/" + Integer.toString(players[i].getPoints()+players[i].getPointsSum()) +
                            " 3/" + Integer.toString(players[i].getPointsFoVictory()+players[i].getPointsFoVictorySum()) +
                            " 5/" + Integer.toString(players[i].getDifference()+players[i].getDifferenceSum()));
                break;
            }
        }


    }

    public void onRestart(){
        super.onRestart();
        fillingListView();//Выводим список
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
        editor.putInt(MainActivity.ROUND_SAVE_KEY, roundSaveKey);
        editor.apply();

        //Запись промежуточных очков в БД
        playersDB = new PlayersDB(this); //Помошник БД
        db = playersDB.getWritableDatabase(); //БД

        for (int i=0; i<sumPlayersInGame; i++) {
            int playerNo = players[i].getNumber();
            ContentValues content = new ContentValues();
            content.put("POINTS", players[i].getPoints());
            content.put("POINTS_FOR_VICTORY", players[i].getPointsFoVictory());
            content.put("DIFFERENCE", players[i].getDifference());
            db.update("PLAYERS_IN_GAME", content, "_id = ?", new String[]{Integer.toString(playerNo)});
        }
    }


    //Заполнение переменных и информационной шапки
    public  void filling (){

        //Заполнение Информации об игроках из БД
        try {
            playersDB = new PlayersDB(this); //Помошник БД
            db = playersDB.getReadableDatabase(); //БД

            //Записываем кол-во игроков
            cursor = db.query ("PLAYERS_IN_GAME",
                    new String[] {"COUNT(_id) AS COUNTER"},
                    null,null, null, null,null);
            if (cursor.moveToFirst()) {
                sumPlayersInGame = Integer.parseInt(cursor.getString(0)); //Количество игроков
            }

            //Записываем игроков в список
            players = new Player[sumPlayersInGame];//Список состоит из кол-ва игроков, которе вернула БД
            cursor = db.query ("PLAYERS_IN_GAME",
                    new String[] {"_id", "NAME", "SURNAME",
                                  "POINTS_SUM", "POINTS_FOR_VICTORY_SUM" , "DIFFERENCE_SUM",
                                  "POINTS", "POINTS_FOR_VICTORY" , "DIFFERENCE",
                                  "OPPONENT_1", "OPPONENT_2", "OPPONENT_3", "OPPONENT_4", "OPPONENT_5"},
                                null,null, null, null,
                                "POINTS_FOR_VICTORY_SUM DESC, POINTS_SUM DESC, DIFFERENCE_SUM DESC");
            int i=0;
            //Пока курсор перебирает значения, данные игроков записываются в массив
            while (cursor.moveToNext()) {
                players[i] = new Player(Integer.parseInt(cursor.getString(0)),
                                        cursor.getString(1),
                                        cursor.getString(2),
                                        cursor.getInt(3),
                                        cursor.getInt(4),
                                        cursor.getInt(5));
                players[i].setPoints(cursor.getInt(6));
                players[i].setPointsFoVictory(cursor.getInt(7));
                players[i].setDifference(cursor.getInt(8));
                players[i].setOpponents(cursor.getInt(9),
                                        cursor.getInt(10),
                                        cursor.getInt(11),
                                        cursor.getInt(12),
                                        cursor.getInt(13));
                i++;
            }
        }
        catch (SQLiteException e){
            //Если вылезет это исключение, то появится это сообщение
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //Записываем переменные информационных текстовых полей
        nowRound = (TextView) findViewById(R.id.nowRound); //Информационное поле текущего раунда
        allRound = (TextView) findViewById(R.id.allRound); //Информационное поле кол-ва раундов
        namberPlayers = (TextView) findViewById(R.id.namberPlayers); //Информационное поле кол-ва игроков
        nowRound.setText(Integer.toString(roundNow));
        allRound.setText(Integer.toString(rounds));
        namberPlayers.setText(Integer.toString(sumPlayersInGame));
    }

    //Заполнение спискового пердставления
    public void fillingListView (){

        ArrayList<HashMap<String, String>> arrayList=new ArrayList<>();

        for (int i=0; i<div; i++){
            HashMap<String, String> map;
            map = new HashMap<>();
             map.put("name1", pairPlayers[0][i].getName());
             map.put("surname1", pairPlayers[0][i].getSurname());
             map.put("points1",Integer.toString(pairPlayers[0][i].getPoints()));
             map.put("pointsFoVictory1", Integer.toString(pairPlayers[0][i].getPointsFoVictory()));
             map.put("difference1", Integer.toString(pairPlayers[0][i].getDifference()));
             map.put("pointsSum1",Integer.toString(pairPlayers[0][i].getPointsSum()+pairPlayers[0][i].getPoints()));
             map.put("pointsFoVictorySum1", Integer.toString(pairPlayers[0][i].getPointsFoVictorySum()+pairPlayers[0][i].getPointsFoVictory()));
             map.put("differenceSum1", Integer.toString(pairPlayers[0][i].getDifferenceSum()+pairPlayers[0][i].getDifference()));
              map.put("name2", pairPlayers[1][i].getName());
              map.put("surname2", pairPlayers[1][i].getSurname());
              map.put("points2",Integer.toString(pairPlayers[1][i].getPoints()));
              map.put("pointsFoVictory2", Integer.toString(pairPlayers[1][i].getPointsFoVictory()));
              map.put("difference2", Integer.toString(pairPlayers[1][i].getDifference()));
              map.put("pointsSum2",Integer.toString(pairPlayers[1][i].getPointsSum()+pairPlayers[1][i].getPoints()));
              map.put("pointsFoVictorySum2", Integer.toString(pairPlayers[1][i].getPointsFoVictorySum()+pairPlayers[1][i].getPointsFoVictory()));
              map.put("differenceSum2", Integer.toString(pairPlayers[1][i].getDifferenceSum()+pairPlayers[1][i].getDifference()));
            arrayList.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this,
                arrayList,
                R.layout.list_play,
                new String[]{"name1", "surname1", "points1", "pointsFoVictory1", "difference1",
                                "pointsSum1", "pointsFoVictorySum1", "differenceSum1",
                             "name2", "surname2", "points2", "pointsFoVictory2", "difference2",
                                "pointsSum2", "pointsFoVictorySum2", "differenceSum2"},
                new int[]{R.id.name1, R.id.surname1, R.id.points1, R.id.pointsFoVictory1, R.id.difference1,
                                R.id.pointsSum1, R.id.pointsFoVictorySum1, R.id.differenceSum1,
                          R.id.name2, R.id.surname2, R.id.points2, R.id.pointsFoVictory2, R.id.difference2,
                                R.id.pointsSum2, R.id.pointsFoVictorySum2, R.id.differenceSum2});
        listPlayers.setAdapter(adapter);

    }


    //Распределение игроков для 1 тура выполняется в случайном порядке
    public void swissSystemForFirstRound (){

        mod = sumPlayersInGame % 2; //Если 0, то четное количество участников, если 1 - не четное
        div = sumPlayersInGame / 2; //Количество пар участников
        pairPlayers = new Player [2] [div];//Пары игроков, хранит сылки на массив участников players

        //При нечетном количестве игроков, инициализируем последний элемент
        if (mod!=0){
            int a = (int) (Math.random()*sumPlayersInGame); //выберем ирока с автопобедой соучайным образом
            players[a].setOpponents(1, -1); //игрок с номером а в 1 раунде получает -1 - автопобеду
        }

        //распределение остальных игроков
        for (int i=0; i<div-1; i++){//Внешний цикл - количество пар без последней
            for (int j=0; j<sumPlayersInGame; j++){//внутренний цикл - количество участников
                if (players[j].getOpponents(1)==0){ //берем j-ого участника, если оппонент в 1 раунда =0
                    do{
                        int a = (int) (Math.random()*sumPlayersInGame);//генерируем случайное число а
                        if (players[a].getOpponents(1)==0 &&  !(players[j].equals(players[a]))){//если а-тый участник не имеет оппонента, то записываем пару
                            //pairPlayers[0][i]=players[j];//1 участник в i-й паре - j-тый участник
                            //pairPlayers[1][i]=players[a];//2 участник в i-й паре - а-тый участник
                            players[j].setOpponents(1, players[a].getNumber());//регестрируем у j-ого участника оппонента
                            players[a].setOpponents(1, players[j].getNumber());//регестрируем у а-ого участника оппонента
                        }
                    } while(players[j].getOpponents(1)==0);//цикл с пост условием, пока оппонент не будет найден
                    break;//переходим к следующей паре
                }
            }
        }

        //для последней пары участников
        for (int i=0; i<sumPlayersInGame; i++){//внешний цыкл - количество участников
            if (players[i].getOpponents(1)==0){   //берем i-ого участника, оппонент в 1 раунда =0
                for (int j=i+1; j<sumPlayersInGame; j++){//внутренний цыкл - количество участников, начинается с найденного участника+1
                    if (players[j].getOpponents(1)==0){//берем j-ого участника, оппонент в 1 раунда =0
                       // pairPlayers[0][div-1]=players[i];//1 участник в последней паре - i-тый участник
                       // pairPlayers[1][div-1]=players[j];//2 участник в последней паре - j-тый участник
                        players[j].setOpponents(1, players[i].getNumber());//регестрируем у j-ого участника оппонента
                        players[i].setOpponents(1, players[j].getNumber());//регестрируем у i-ого участника оппонента
                        break;
                    }
                }
            }
        }

        //Записывает пары игроков
        pair(1);

        //Запись оппонентов в БД
        playersDB = new PlayersDB(this); //Помошник БД
        db = playersDB.getWritableDatabase(); //БД
        for (int i=0; i<sumPlayersInGame; i++) {
            int playerNo = players[i].getNumber();
            ContentValues content = new ContentValues();
            content.put("OPPONENT_1", players[i].getOpponents(1));
            db.update("PLAYERS_IN_GAME", content, "_id = ?", new String[]{Integer.toString(playerNo)});
        }
    }

    //Распределение игроков для последующих туров, принимает номер тура
    public void swissSystemForOverseasRounds (int No) {

        mod = sumPlayersInGame % 2; //Если 0, то четное количество участников, если 1 - не четное
        div = sumPlayersInGame / 2; //Количество пар участников
        pairPlayers = new Player [2] [div];//Пары игроков, хранит сылки на массив участников players

        //При нечетном количестве игроков, инициализируем последний элемент
        if (mod != 0) {
            for (int i = sumPlayersInGame - 1; i >= 0; i--) {//цикл, выбираем игрока с конца списка
                int key = 0;
                for (int j = 1; j < No; j++)
                    if (players[i].getOpponents(j) == -1)
                        key++;//Если до этого у игрока были автопобеды - он не подходит
                if (key == 0) {
                    players[i].setOpponents(No, -1); //игрок с номером i в раунде No получает -1 - автопобеду
                    break;
                }
            }
        }

        //распределение игроков
        for (int i = 0; i < sumPlayersInGame; i++) { //внешний цикл - игрок i
            if (players[i].getOpponents(No) == 0) { //если игрок i не еще имеет оппонентов
                for (int j = i + 1; j < sumPlayersInGame; j++) { //внутренний цикл 1 - выбирем для него игрока j
                    if (players[j].getOpponents(No) == 0 && !(players[i].equals(players[j]))) { //если игрок j не еще имеет оппонентов
                        int key = 0;
                        for (int k = 1; k < No; k++)
                            if (players[i].getNumber() == players[j].getOpponents(k))
                                key++;//Если до этого игроки i и j уже встречались - они не подходят
                        if (key == 0) {
                            players[i].setOpponents(No, players[j].getNumber());//регестрируем у i-ого участника оппонента - j-тый участник
                            players[j].setOpponents(No, players[i].getNumber());//регестрируем у j-ого участника оппонента - i-тый участник
                            break;//Если нашелся партнер для i, завершим из внутренний цикл 1
                        }
                    }
                    //Если мы перебрали всех участников и не смогли никого подобрать
                    if (j == sumPlayersInGame - 1) {//Перейдем в этот блок на последнем элементе
                        for (int l = sumPlayersInGame - 1; l >= 0; l--) {//внутренний цикл 2 - выбирем для него игрока l (идем с конца списка)
                            int key = 0;
                            for (int k = 1; k < No; k++)
                                if (players[i].getNumber() == players[l].getOpponents(k) || //Если до этого игроки i и l уже встречались - они не подходят
                                        (players[i].equals(players[l])) || //Если не эквеволентны
                                        players[l].getOpponents(No) == -1)
                                    key++; //Если у игрока l нет автопобеды
                            if (key == 0) { //Нашли подходящего l игрока
                                for (int k = 0; k < sumPlayersInGame; k++) {//внутренний цикл 3
                                    if (players[l].getOpponents(No) == players[k].getNumber()) {//найдем игрока k - оппонента игрока l
                                        players[i].setOpponents(No, players[l].getNumber());//регестрируем у i-ого участника оппонента - участник l
                                        players[l].setOpponents(No, players[i].getNumber());//регестрируем у l-ого нового участника оппонента - участник i
                                        players[k].setOpponents(No, 0);//регестрируем у k-ого участника отсутствие оппонента - 0
                                        i = k - 1;
                                        break;//Если нашелся партнер для i, завершим из внутренний цикл 3
                                    }
                                }
                                break;//завершим из внутренний цикл 2
                            }
                        }
                        break;//завершим из внутренний цикл 1
                    }
                }
            }
        }

        //Запись по парам
        pair(No);

        //Запись оппонентов в БД
        playersDB = new PlayersDB(this); //Помошник БД
        db = playersDB.getWritableDatabase(); //БД
        for (int i=0; i<sumPlayersInGame; i++) {
            int playerNo = players[i].getNumber();
            ContentValues content = new ContentValues();
            content.put("OPPONENT_2", players[i].getOpponents(2));
            content.put("OPPONENT_3", players[i].getOpponents(3));
            content.put("OPPONENT_4", players[i].getOpponents(4));
            content.put("OPPONENT_5", players[i].getOpponents(5));
            db.update("PLAYERS_IN_GAME", content, "_id = ?", new String[]{Integer.toString(playerNo)});
        }
    }

    //Записывает пары игроков
    public  void pair (int No){
        div = sumPlayersInGame / 2; //Количество пар участников
        pairPlayers = new Player [2] [div];//Пары игроков, хранит сылки на массив участников players
        //Запись по парам
        for (int i = 0; i < div; i++) {//внешний цикл - пары
            for (int j = 0; j < sumPlayersInGame; j++) {//внутренний цикл - игроки
                int key = 0;
                for (int k = 0; k < div; k++)
                    if (players[j].equals(pairPlayers[0][k]) ||players[j].equals(pairPlayers[1][k]))
                        key++;
                if (key == 0) {  //Если j-ый игрок еще не записан в список
                    for (int k = 0; k < sumPlayersInGame; k++)
                        if (players[j].getOpponents(No) == players[k].getNumber() && !(players[j].equals(players[k]))) {//находим для i-ого игрока оппонента k
                            pairPlayers[0][i] = players[j];//1 участник в i-й паре - j-тый участник
                            pairPlayers[1][i] = players[k];//2 участник в i-й паре - k-тый участник
                            break;
                        }
                }
            }
        }
    }


    //Действие по нажатию на кнопку "Завершить раунд"
    public void finishRound(View view) {

        //Изменяем очки в БД
        playersDB = new PlayersDB(this); //Помошник БД
        db = playersDB.getWritableDatabase(); //БД

        for (int i=0; i<sumPlayersInGame; i++) {
            int playerNo = players[i].getNumber();
            ContentValues content = new ContentValues();
              content.put("POINTS_SUM", players[i].getPoints()+players[i].getPointsSum());
              content.put("POINTS_FOR_VICTORY_SUM", players[i].getPointsFoVictory()+players[i].getPointsFoVictorySum());
              content.put("DIFFERENCE_SUM", players[i].getDifference()+players[i].getDifferenceSum());
            db.update("PLAYERS_IN_GAME", content, "_id = ?", new String[]{Integer.toString(playerNo)});
            //Обнуляем текущие данные
            players[i].setPoints(0);
            players[i].setPointsFoVictory(0);
            players[i].setDifference(0);
        }


        //Сохранение rounds и roundNow
        SharedPreferences.Editor editor = save.edit();
        editor.putInt(MainActivity.ROUNDS, rounds);
        editor.putInt(MainActivity.ROUND_NOW, roundNow);
        editor.apply();

        //Отправка интента к Result
        Intent intent = new Intent(Play.this, Result.class);
        startActivity(intent);
        this.finish();

    }

    //Действие по нажатию на имя в списке
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
            //Отправка интента к PlayerResult
            Intent intent = new Intent(Play.this, PlayerResult.class);
            intent.putExtra(MainActivity.POSITION_PLAYER, (int) id);
            startActivity(intent);
        }
    };



}
