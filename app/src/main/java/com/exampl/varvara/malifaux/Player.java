package com.exampl.varvara.malifaux;

/* Дополнительный класс
    Хранит промежуточные данные об игроке до завершения тура.
    Получает из БД информацию об участнике.
 */

public class Player {

    private int number; //номер участника
    private int[] opponents = {0,0,0,0,0}; //аппоненты. Переменная возвращает номер оппонента, 0 - если оппонента нет/раунд не съигран, -1 - защитана автопобеда.
    private String name; //имя участника
    private String surname; //фамилия участника
    private int points; //текущие очки
    private int pointsFoVictory;
    private int difference;
    private int pointsSum; //общие очки из БД
    private int pointsFoVictorySum;
    private int differenceSum;

    //Конструктор
    public Player(int n, String ne, String se, int p, int pfv, int diff){
        number=n;
        name = ne;
        surname = se;
        pointsSum=p;
        pointsFoVictorySum=pfv;
        differenceSum=diff;
    }


    //принимает число - № раунда и номер оппонента
    public void setOpponents (int i, int n){
        opponents[i-1]=n;
    }

    public void setOpponents (int n0, int n1, int n2, int n3, int n4){
        opponents[0]=n0;
        opponents[1]=n1;
        opponents[2]=n2;
        opponents[3]=n3;
        opponents[4]=n4;
    }

    public int getNumber() {
        return number;
    }

    // метода, для возвращения номера оппонента по № раунда
    public int getOpponents(int i) {
        return opponents[i-1];
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }


    public void setPoints(int points) {
        this.points = points;
    }

    public void setPointsFoVictory(int pointsFoVictory) {
        this.pointsFoVictory = pointsFoVictory;
    }

    public void setDifference(int difference) {
        this.difference = difference;
    }


    public int getPoints() {
        return points;
    }

    public int getPointsFoVictory() {
        return pointsFoVictory;
    }

    public int getDifference() {
        return difference;
    }

    public int getPointsSum() {
        return pointsSum;
    }

    public int getPointsFoVictorySum() {
        return pointsFoVictorySum;
    }

    public int getDifferenceSum() {
        return differenceSum;
    }
}
