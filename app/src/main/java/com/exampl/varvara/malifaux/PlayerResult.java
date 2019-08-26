package com.exampl.varvara.malifaux;

/* Активность изменения/добавления результатов игрока
    Здесь добавляются/изменяютя результаты игрока
 */

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PlayerResult extends AppCompatActivity {

    EditText editTextPoints1;
    EditText editTextPoints2;
    TextView editTextPointsFoVictory1;
    TextView editTextPointsFoVictory2;
    TextView editTextDiff1;
    TextView editTextDiff2;
    TextView textName1;
    TextView textSurname1;
    TextView textName2;
    TextView textSurname2;
    int points1, pointsFoVictory1, diff1;//значения очков игрока 1
    int points2, pointsFoVictory2, diff2;//значения очков игрока 2
    int No;//номер пары

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_result);

        getSupportActionBar().setTitle(getResources().getString(R.string.app_name_PlayerResult)); // Вывести заголовок на панели действий.
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Вертикальное расположение

        Intent intent = getIntent(); // получение интента
        No = intent.getIntExtra(MainActivity.POSITION_PLAYER, 0);

        //Свяжем переменные с элементами
        editTextPoints1 = (EditText) findViewById(R.id.points1);
        editTextPoints2 = (EditText) findViewById(R.id.points2);
        editTextPointsFoVictory1 = (TextView) findViewById(R.id.pointsFoVictory1);
        editTextPointsFoVictory2 = (TextView) findViewById(R.id.pointsFoVictory2);
        editTextDiff1 = (TextView) findViewById(R.id.diff1);
        editTextDiff2 = (TextView) findViewById(R.id.diff2);

        textName1 = (TextView) findViewById(R.id.textName1);
        textSurname1 = (TextView) findViewById(R.id.textSurname1);
        textName2 = (TextView) findViewById(R.id.textName2);
        textSurname2 = (TextView) findViewById(R.id.textSurname2);

        //Запишем переменные, хранящие очки игроков
        points1=Play.pairPlayers[0][No].getPoints();
        pointsFoVictory1=Play.pairPlayers[0][No].getPointsFoVictory();
        diff1=Play.pairPlayers[0][No].getDifference();
        points2=Play.pairPlayers[1][No].getPoints();
        pointsFoVictory2=Play.pairPlayers[1][No].getPointsFoVictory();
        diff2=Play.pairPlayers[1][No].getDifference();

        //Если раньше были значения - выведем их
        if (points1 != 0 || points2 != 0) pressKey();

        //Выведем имена игроков
        textName1.setText(Play.pairPlayers[0][No].getName());
        textName2.setText(Play.pairPlayers[1][No].getName());
        textSurname1.setText(Play.pairPlayers[0][No].getSurname());
        textSurname2.setText(Play.pairPlayers[1][No].getSurname());

        //Ожидание нажатия кнопки Enter
        editTextPoints1.setOnKeyListener(new View.OnKeyListener()
                                  {
                                      public boolean onKey(View v, int keyCode, KeyEvent event)
                                      {
                                          if(event.getAction() == KeyEvent.ACTION_DOWN &&
                                                  (keyCode == KeyEvent.KEYCODE_ENTER))
                                          {
                                              //Если 1 поле пустое
                                              if (editTextPoints1.getText().toString().equals("")) points1=0;
                                              else  points1 = Integer.parseInt(String.valueOf(editTextPoints1.getText()));
                                              //Если 2 поле пустое
                                              if (editTextPoints2.getText().toString().equals("")) points2=0;
                                              else  points2 = Integer.parseInt(String.valueOf(editTextPoints2.getText()));

                                              pressKey();
                                              return true;
                                          }
                                          return false;
                                      }
                                  }
        );

        //Ожидание нажатия кнопки Enter
        editTextPoints2.setOnKeyListener(new View.OnKeyListener()
                                         {
                                             public boolean onKey(View v, int keyCode, KeyEvent event)
                                             {
                                                 if(event.getAction() == KeyEvent.ACTION_DOWN &&
                                                         (keyCode == KeyEvent.KEYCODE_ENTER))
                                                 {
                                                     //Если 1 поле пустое
                                                     if (editTextPoints1.getText().toString().equals("")) points1=0;
                                                     else  points1 = Integer.parseInt(String.valueOf(editTextPoints1.getText()));
                                                     //Если 2 поле пустое
                                                     if (editTextPoints2.getText().toString().equals("")) points2=0;
                                                     else  points2 = Integer.parseInt(String.valueOf(editTextPoints2.getText()));

                                                     pressKey();
                                                     return true;
                                                 }
                                                 return false;
                                             }
                                         }
        );

    }

    //Заполнение полей очков
    public void pressKey(){


        if (points1>points2) {
            pointsFoVictory1 = 3;
            pointsFoVictory2 = 0;
        }
        else if (points1<points2){
            pointsFoVictory1 = 0;
            pointsFoVictory2 = 3;
        }
        else{
            pointsFoVictory1 = 1;
            pointsFoVictory2 = 1;
        }

        diff1 = points1-points2;
        diff2 = points2-points1;

        editTextPoints1.setText(Integer.toString(points1));
        editTextPoints2.setText(Integer.toString(points2));
        editTextPointsFoVictory1.setText(Integer.toString(pointsFoVictory1));
        editTextPointsFoVictory2.setText(Integer.toString(pointsFoVictory2));
        editTextDiff1.setText(Integer.toString(diff1));
        editTextDiff2.setText(Integer.toString(diff2));
    }

    //Запись очков в массив pairPlayers
    public void changResult(View view) {
        Play.pairPlayers[0][No].setPoints(points1);
        Play.pairPlayers[0][No].setPointsFoVictory(pointsFoVictory1);
        Play.pairPlayers[0][No].setDifference(diff1);
        Play.pairPlayers[1][No].setPoints(points2);
        Play.pairPlayers[1][No].setPointsFoVictory(pointsFoVictory2);
        Play.pairPlayers[1][No].setDifference(diff2);

        //Закрываем окно
        this.finish();
    }
}
