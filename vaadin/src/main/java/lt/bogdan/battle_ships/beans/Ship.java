package lt.bogdan.battle_ships.beans;

public class Ship extends Coordinate {

    public static final String SHIP = "S";
    public static final String HORIZONTAL = "H";
    public static final String VERTICAL = "V";

    private String direction;
    private int size;

    public Ship(String direction, int row, int column) {
        this.direction = direction;
        super.setRow(row);
        super.setColumn(column);
    }

    public Ship(String direction, String column, int row, int size) {
        this.direction = direction;
        this.size = size;
        super.setRow(Integer.parseInt(column));
        super.setColumn(row);
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}