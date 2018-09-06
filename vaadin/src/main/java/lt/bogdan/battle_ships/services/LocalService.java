package lt.bogdan.battle_ships.services;

import lt.bogdan.battle_ships.beans.*;

public class LocalService {

    public void updateGameTables(User user, GameTable userGT, GameTable oppGT, ServerResponse servResp) {

        if (!servResp.getEvents().isEmpty()) {
            for (int i = 0; i < servResp.getEvents().size(); i++) {
                Event event = servResp.getEvents().get(i);
                String yX = event.getCoordinate();

                if (event.getUserId().equals(user.getId())) {
                    oppGT.setField(convertStringToCoordinate(yX), getHitOrTurn(event.isHit()));
                } else {
                    userGT.setField(convertStringToCoordinate(yX), getHitOrTurn(event.isHit()));
                }
            }
        }
    }

    public void updateAttackShip(User user, ServerResponse servResp, AttackShip attackShip) {
        for (int i = 0; i < servResp.getEvents().size(); i++) {
            Event event = servResp.getEvents().get(i);
            if (attackShip.getTurnCoordinate() != null &&
                    event.getCoordinate().equals(attackShip.getTurnCoordinate().toString()) && event.getUserId().equals(user.getId())) {
                if (event.isHit()) {
                    addCoordinateToAttackShip(attackShip.getTurnCoordinate(), attackShip);
                    setDirectionAttackShip(attackShip);
                }
            }
        }
        attackShip.setTurnCoordinate(null);
    }

    public boolean checkIsTurnTrue(User user, ServerResponse servResp, String turnCoordinate) {
        for (int i = 0; i < servResp.getEvents().size(); i++) {
            Event event = servResp.getEvents().get(i);
            if (turnCoordinate != null &&
                    event.getCoordinate().equals(turnCoordinate) && event.getUserId().equals(user.getId())) {
                if (event.isHit()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addShipToUserBF(GameTable userGT, Ship ship) {
        int x = ship.getRow();
        int y = ship.getColumn();
        if (ship.getDirection().equals(Ship.HORIZONTAL)) {
            for (int shipSize = 0; shipSize < ship.getSize(); shipSize++) {
                userGT.setField(new Coordinate(x, y + shipSize), Ship.SHIP);
            }
        } else {
            for (int shipSize = 0; shipSize < ship.getSize(); shipSize++) {
                userGT.setField(new Coordinate(x + shipSize, y), Ship.SHIP);
            }
        }
    }

    public static void addShipCoordinates(GameTable userGT, Ship ship) {
        StringBuilder shipsCoordinates = new StringBuilder();
        if (!userGT.getShipsCoordinates().isEmpty()) {
            shipsCoordinates.append(userGT.getShipsCoordinates()).append(GameTable.SHIPS_SEPARATOR);
        }
        shipsCoordinates.append(createShipCoordinates(ship));
        userGT.setShipsCoordinates(shipsCoordinates.toString());
    }

    public static boolean checkIsSpaceForShip(GameTable userGT, Ship ship) {
        int x = ship.getRow();
        int y = ship.getColumn();

        if (ship.getDirection().equals(Ship.HORIZONTAL)) {
            for (int i = y; i < y + ship.getSize(); i++) {
                Coordinate coordinate = new Coordinate(x, i);
                if (!userGT.getField(coordinate).equals(GameTable.FREE) ||
                        checkIsHitAroundCoordinate(userGT, coordinate, Ship.SHIP)) {
                    return false;
                }
            }
        } else {
            for (int i = x; i < x + ship.getSize(); i++) {
                Coordinate coordinate = new Coordinate(i, y);
                if (!userGT.getField(coordinate).equals(GameTable.FREE) ||
                        checkIsHitAroundCoordinate(userGT, coordinate, Ship.SHIP)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkIsHitAroundCoordinate(GameTable table, Coordinate coordinate, String fieldValue) {
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
                if (table.getField(new Coordinate(coordinate.getRow() + j, coordinate.getColumn() + k)).equals(fieldValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    public GameTable clearGameTable(GameTable gameTable){
        for (int row = 0; row < GameTable.COLUMNS.length; row++) {
            for (int column = 0; column < GameTable.COLUMNS.length; column++) {
                gameTable.setField(new Coordinate(row, column),GameTable.FREE);
            }
        }
        return gameTable;
    }

    private Coordinate convertStringToCoordinate(String text) {
        Coordinate result = new Coordinate(Integer.parseInt(text.substring(1, 2)), 0);
        for (int i = 0; i < GameTable.COLUMNS.length; i++) {
            if (text.substring(0, 1).equals(GameTable.COLUMNS[i])) {
                result.setColumn(i);
            }
        }
        return result;
    }

    private String getHitOrTurn(boolean hit) {
        if (hit) {
            return GameTable.HIT;
        } else {
            return GameTable.TURN;
        }
    }

    private static String createShipCoordinates(Ship ship) {
        StringBuilder sb = new StringBuilder();
        sb.append(ship.toString()).append(GameTable.COORDINATE_SEPARATOR);
        if (ship.getDirection().equals(Ship.HORIZONTAL)) {
            sb.append(ship.toString(ship.getSize() - 1)).append(ship.getRow());
        } else {
            sb.append(ship.toString(0)).append(ship.getRow() + ship.getSize() - 1);
        }
        return sb.toString();
    }

    private void addCoordinateToAttackShip(Coordinate coordinate, AttackShip attackShip) {
        attackShip.setCoordinates(coordinate);
    }

    private void setDirectionAttackShip(AttackShip attackShip) {
        if (attackShip.getCoordinates().size() > 1) {
            for (int i = 1; i < attackShip.getCoordinates().size(); i++) {
                if (attackShip.getCoordinates().get(i).getRow() == attackShip.getCoordinates().get(0).getRow()) {
                    attackShip.setDirection(Ship.HORIZONTAL);
                } else {
                    attackShip.setDirection(Ship.VERTICAL);
                }
            }
        }
    }

}