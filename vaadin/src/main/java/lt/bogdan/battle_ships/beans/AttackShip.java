package lt.bogdan.battle_ships.beans;


import java.util.ArrayList;
import java.util.List;

public class AttackShip {

    public static final int SINGLE_SHIP_VALUE = 1;
    public static final int DOUBLE_SHIP_VALUE = 10;
    public static final int TRIPLE_SHIP_VALUE = 100;
    public static final int QUADRO_SHIP_VALUE = 1000;

    private List<Coordinate> coordinates = new ArrayList();
    private String direction = "";
    private Coordinate turnCoordinate;

    /*
     *After damage ship, 'restShipsSize' deducted of ship value.
     *Also it use for get rest ships max size on opponent battleField.
     */
    private int restShipsSize = 1234;

    @Override
    public String toString() {
        return String.valueOf(turnCoordinate.getColumn()) + String.valueOf(turnCoordinate.getRow());
    }

    public Coordinate getTurnCoordinate() {
        return turnCoordinate;
    }

    public void setTurnCoordinate(Coordinate coordinate) {
        this.turnCoordinate = coordinate;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinate coordinate) {
        this.coordinates.add(coordinate);
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public int getRestShipsSize() {
        return restShipsSize;
    }

    public void setRestShipsSize(int restShipsSize) {
        this.restShipsSize = restShipsSize;
    }

    public int getMaxValueShipLength() {
        return String.valueOf(this.restShipsSize).length();
    }
}