package lt.bogdan.battle_ships.beans;

public class GameTable {

    public static final String[] COLUMNS = {"K", "I", "L", "O", "M", "E", "T", "R", "A", "S"};
    public static final String FREE = "";
    public static final String TURN = "¤";
    public static final String HIT = "X";
    public static final String COORDINATE_SEPARATOR = "-";
    public static final String SHIPS_SEPARATOR = "!";
    public static final String OUT = "Out";

    private String[][] fields = new String[10][10];

    // user ships coordinates for setup with opponent
    private String shipsCoordinates = "";


    public GameTable() {
        for (int row = 0; row < fields.length; row++) {
            for (int column = 0; column < fields.length; column++) {
                this.fields[row][column] = FREE;
            }
        }
    }

    public String getShipsCoordinates() {
        return shipsCoordinates;
    }

    public void setShipsCoordinates(String shipsCoordinates) {
        this.shipsCoordinates = shipsCoordinates;
    }

    public String getField(Coordinate coordinate) {
        if(coordinate.getRow()>=0 && coordinate.getRow()<=9 && coordinate.getColumn()>=0 && coordinate.getColumn()<=9){
            return fields[coordinate.getRow()][coordinate.getColumn()];
        }
        return OUT;
    }

    public void setField(Coordinate coordinate, String value) {
        this.fields[coordinate.getRow()][coordinate.getColumn()] = value;
    }
}
