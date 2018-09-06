package lt.bogdan.battle_ships.beans;

public class Coordinate {

    private int row;
    private int column;

    public Coordinate(){}

    public Coordinate(int row, int column){
        this.row = row;
        this.column = column;
    }

    public Coordinate(String row, int column){
        this.row = Integer.parseInt(row);
        this.column = column;
    }

    @Override
    public String toString() {
        return GameTable.COLUMNS[column] + row;
    }

    public String toString(int row){
        return GameTable.COLUMNS[column + row];
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}