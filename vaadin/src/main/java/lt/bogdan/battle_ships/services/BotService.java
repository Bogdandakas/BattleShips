package lt.bogdan.battle_ships.services;

import lt.bogdan.battle_ships.beans.*;

import java.util.List;
import java.util.Random;

public class BotService {

    public static final int COUNT_BEST_TURN = 20;

    public GameTable createUserShipsWithBot(GameTable userGT) {

        for (int shipSize = 4; shipSize > 0; shipSize--) {
            for (int shipCount = 1; shipCount <= 5 - shipSize; shipCount++) {
                boolean trayAnother = true;
                while (trayAnother) {
                    Ship ship = generateRandomStartShipCrd();
                    ship.setSize(shipSize);
                    if (LocalService.checkIsSpaceForShip(userGT, ship)) {
                        LocalService.addShipToUserBF(userGT, ship);
                        LocalService.addShipCoordinates(userGT, ship);
                        trayAnother = false;
                    }
                }
            }
        }
        return userGT;
    }

    public String findTurnCoordinate(GameTable oppGT, AttackShip attackShip) {
        int bestTest = 0;
        while (attackShip.getTurnCoordinate() == null) {
            if (attackShip.getCoordinates().size() == 0) {
                Ship ship = generateRandomStartShipCrd();
                attackShip.setTurnCoordinate(ship);
                attackShip.setDirection(ship.getDirection());
                if (bestTest < COUNT_BEST_TURN) {
                    if (!checkBestTurnCoordinate(oppGT, attackShip)) {
                        attackShip.setTurnCoordinate(null);
                    }
                    bestTest++;
                } else {
                    if (!checkTurnCoordinate(oppGT, attackShip)) {
                        attackShip.setTurnCoordinate(null);
                    }
                }
            } else {
                if (checkAvailableTurnCoordinateNearShip(oppGT, attackShip)) {
                    if (attackShip.getCoordinates().size() == 1) {
                        attackShip.setTurnCoordinate(generateRandomCoordinateAroundSingleShip(attackShip));
                    } else {
                        attackShip.setTurnCoordinate(generateRandomCoordinateAroundMultipleShip(attackShip));
                    }
                    if (!checkTurnCoordinateNearShip(oppGT, attackShip)) {
                        attackShip.setTurnCoordinate(null);
                    }
                } else {
                    setDefaultDetectedShip(attackShip);
                }
            }
        }
        return attackShip.getTurnCoordinate().toString();
    }

    private Ship generateRandomStartShipCrd() {
        Ship ship;
        Random random = new Random();
        if (random.nextInt(10) % 2 > 0) {
            ship = new Ship(Ship.HORIZONTAL, random.nextInt(10), random.nextInt(10));
        } else {
            ship = new Ship(Ship.VERTICAL, random.nextInt(10), random.nextInt(10));
        }
        return ship;
    }

    private Coordinate generateRandomCoordinateAroundSingleShip(AttackShip attackShip) {
        Coordinate coordinate = new Coordinate(getLastCrdShip(attackShip).getRow(), getLastCrdShip(attackShip).getColumn());
        Random random = new Random();
        int direction = random.nextInt(4);
        if (direction == 0) {
            coordinate.setRow(coordinate.getRow() - 1);
        }
        if (direction == 1) {
            coordinate.setRow(coordinate.getRow() + 1);
        }
        if (direction == 2) {
            coordinate.setColumn(coordinate.getColumn() - 1);
        }
        if (direction == 3) {
            coordinate.setColumn(coordinate.getColumn() + 1);
        }
        return coordinate;
    }

    private Coordinate generateRandomCoordinateAroundMultipleShip(AttackShip attackShip) {
        Coordinate coordinate = new Coordinate();
        Random random = new Random();
        int direction = random.nextInt(2);
        if (attackShip.getDirection().equals(Ship.HORIZONTAL)) {
            coordinate.setRow(getLastCrdShip(attackShip).getRow());
            if (direction == 0) {
                coordinate.setColumn(getMinusColumnShip(attackShip));
            } else {
                coordinate.setColumn(getPlusColumnShip(attackShip));
            }
        } else {
            coordinate.setColumn(getLastCrdShip(attackShip).getColumn());
            if (direction == 0) {
                coordinate.setRow(getMinusRowShip(attackShip));
            } else {
                coordinate.setRow(getPlusRowShip(attackShip));
            }
        }
        return coordinate;
    }

    private void setDefaultDetectedShip(AttackShip attackShip) {
        if (attackShip.getCoordinates().size() == 1) {
            attackShip.setRestShipsSize(attackShip.getRestShipsSize() - AttackShip.SINGLE_SHIP_VALUE);
        }
        if (attackShip.getCoordinates().size() == 2) {
            attackShip.setRestShipsSize(attackShip.getRestShipsSize() - AttackShip.DOUBLE_SHIP_VALUE);
        }
        if (attackShip.getCoordinates().size() == 3) {
            attackShip.setRestShipsSize(attackShip.getRestShipsSize() - AttackShip.TRIPLE_SHIP_VALUE);
        }
        if (attackShip.getCoordinates().size() == 4) {
            attackShip.setRestShipsSize(attackShip.getRestShipsSize() - AttackShip.QUADRO_SHIP_VALUE);
        }
        attackShip.getCoordinates().clear();
        attackShip.setTurnCoordinate(null);
    }

    private boolean checkAvailableTurnCoordinateNearShip(GameTable oppGT, AttackShip attackShip) {
        if (attackShip.getCoordinates().size() >= attackShip.getMaxValueShipLength()) {
            return false;
        }
        if (attackShip.getCoordinates().size() == 1) {
            return checkAvailableTurnCoordinateNearSingleBody(oppGT, attackShip);
        } else {
            return checkAvailableTurnCoordinateNearMultipleBody(oppGT, attackShip);
        }
    }

    private boolean checkAvailableTurnCoordinateNearMultipleBody(GameTable oppGT, AttackShip attackShip) {
        Coordinate coordinate;
        int possibilities = 0;
        int beforeIsHit = 0;
        int afterIsHit = 0;
        if (attackShip.getDirection().equals(Ship.HORIZONTAL)) {
            coordinate = getCrdBeforeShip(attackShip);
            if (oppGT.getField(coordinate).equals(GameTable.FREE)) {
                possibilities++;
            }
            coordinate = getCrdAfterShip(attackShip);
            if (oppGT.getField(coordinate).equals(GameTable.FREE)) {
                possibilities++;
            }
            for (int i = -1; i <= 1; i++) {
                coordinate = new Coordinate(getCrdBeforeShip(attackShip).getRow() + i, getCrdBeforeShip(attackShip).getColumn() - 1);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    beforeIsHit = 1;
                }
                coordinate = new Coordinate(getCrdAfterShip(attackShip).getRow() + i, getCrdAfterShip(attackShip).getColumn() + 1);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    afterIsHit = 1;
                }
            }
        } else {
            if (oppGT.getField(getCrdBeforeShip(attackShip)).equals(GameTable.FREE)) {
                possibilities++;
            }
            if (oppGT.getField(getCrdAfterShip(attackShip)).equals(GameTable.FREE)) {
                possibilities++;
            }
            for (int i = -1; i <= 1; i++) {
                coordinate = new Coordinate(getCrdBeforeShip(attackShip).getRow() - 1, getCrdBeforeShip(attackShip).getColumn() + i);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    beforeIsHit = 1;
                }
                coordinate = new Coordinate(getCrdAfterShip(attackShip).getRow() + 1, getCrdAfterShip(attackShip).getColumn() + i);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    afterIsHit = 1;
                }
            }
        }
        return possibilities - beforeIsHit - afterIsHit > 0;
    }

    private boolean checkAvailableTurnCoordinateNearSingleBody(GameTable oppGT, AttackShip attackShip) {
        Coordinate coordinate;
        int possibilities = 0;
        int isLeftHit = 0;
        int isRightHit = 0;
        int isUpHit = 0;
        int isDownHit = 0;

        if (oppGT.getField(getLeftCrdNearShip(attackShip)).equals(GameTable.FREE)) {
            possibilities++;
            for (int i = -1; i <= 1; i++) {
                coordinate = new Coordinate(getLeftCrdNearShip(attackShip).getRow() + i, getLeftCrdNearShip(attackShip).getColumn() - 1);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    isLeftHit = 1;
                }
            }
        }
        if (oppGT.getField(getRightCrdNearShip(attackShip)).equals(GameTable.FREE)) {
            possibilities++;

            for (int i = -1; i <= 1; i++) {
                coordinate = new Coordinate(getRightCrdNearShip(attackShip).getRow() + i, getRightCrdNearShip(attackShip).getColumn() + 1);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    isRightHit = 1;
                }
            }
        }
        if (oppGT.getField(getUpCrdNearShip(attackShip)).equals(GameTable.FREE)) {
            possibilities++;
            for (int i = -1; i <= 1; i++) {
                coordinate = new Coordinate(getUpCrdNearShip(attackShip).getRow() - 1, getUpCrdNearShip(attackShip).getColumn() + i);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    isUpHit = 1;
                }
            }
        }
        if (oppGT.getField(getDownCrdNearShip(attackShip)).equals(GameTable.FREE)) {
            possibilities++;
            for (int i = -1; i <= 1; i++) {
                coordinate = new Coordinate(getDownCrdNearShip(attackShip).getRow() + 1, getDownCrdNearShip(attackShip).getColumn() + i);
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    isDownHit = 1;
                }
            }
        }
        return possibilities - isLeftHit - isRightHit - isUpHit - isDownHit > 0;
    }

    private boolean checkTurnCoordinate(GameTable oppGT, AttackShip attackShip) {
        int row = attackShip.getTurnCoordinate().getRow();
        int column = attackShip.getTurnCoordinate().getColumn();

        if (attackShip.getDirection().equals(Ship.HORIZONTAL)) {
            for (int i = 0; i < attackShip.getMaxValueShipLength(); i++) {
                Coordinate coordinate = new Coordinate(row, column + i);
                if (!oppGT.getField(coordinate).equals(GameTable.FREE)
                        || LocalService.checkIsHitAroundCoordinate(oppGT, coordinate, GameTable.HIT)) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < attackShip.getMaxValueShipLength(); i++) {
                Coordinate coordinate = new Coordinate(row + i, column);
                if (!oppGT.getField(coordinate).equals(GameTable.FREE)
                        || LocalService.checkIsHitAroundCoordinate(oppGT, coordinate, GameTable.HIT)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkBestTurnCoordinate(GameTable oppGT, AttackShip attackShip) {
        int x = attackShip.getTurnCoordinate().getRow();
        int y = attackShip.getTurnCoordinate().getColumn();
        int shipSize = attackShip.getMaxValueShipLength();

        for (int i = -shipSize + 1; i < shipSize; i++) {
            Coordinate coordinate = new Coordinate(x, y + i);
            if (!oppGT.getField(coordinate).equals(GameTable.FREE)
                    || LocalService.checkIsHitAroundCoordinate(oppGT, coordinate, GameTable.HIT)) {
                return false;
            }
        }
        for (int i = -shipSize + 1; i < shipSize; i++) {
            Coordinate coordinate = new Coordinate(x + i, y);
            if (!oppGT.getField(coordinate).equals(GameTable.FREE)
                    || LocalService.checkIsHitAroundCoordinate(oppGT, coordinate, GameTable.HIT)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkTurnCoordinateNearShip(GameTable oppGT, AttackShip attackShip) {
        int row = attackShip.getTurnCoordinate().getRow();
        int column = attackShip.getTurnCoordinate().getColumn();

        if (oppGT.getField(attackShip.getTurnCoordinate()).equals(GameTable.FREE)) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    Coordinate coordinate = new Coordinate(row + i, column + j);
                    if (oppGT.getField(coordinate).equals(GameTable.HIT) &&
                            !checkCrdEqualsShip(coordinate, attackShip)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private Coordinate getCrdBeforeShip(AttackShip attackShip) {
        Coordinate result = new Coordinate();
        List<Coordinate> shipCoordinates = attackShip.getCoordinates();
        if (attackShip.getDirection().equals(Ship.HORIZONTAL)) {
            result.setRow(getLastCrdShip(attackShip).getRow());
            int y = 9;
            for (int i = 0; i < shipCoordinates.size(); i++) {
                if (shipCoordinates.get(i).getColumn() < y) {
                    y = shipCoordinates.get(i).getColumn();
                }
            }
            result.setColumn(y - 1);
        } else {
            result.setColumn(getLastCrdShip(attackShip).getColumn());
            int x = 9;
            for (int i = 0; i < shipCoordinates.size(); i++) {
                if (shipCoordinates.get(i).getRow() < x) {
                    x = shipCoordinates.get(i).getRow();
                }
            }
            result.setRow(x - 1);
        }
        return result;
    }

    private Coordinate getCrdAfterShip(AttackShip attackShip) {
        Coordinate result = new Coordinate();
        List<Coordinate> shipCoordinates = attackShip.getCoordinates();
        if (attackShip.getDirection().equals(Ship.HORIZONTAL)) {
            result.setRow(getLastCrdShip(attackShip).getRow());
            int y = 0;
            for (int i = 0; i < shipCoordinates.size(); i++) {
                if (y < shipCoordinates.get(i).getColumn()) {
                    y = shipCoordinates.get(i).getColumn();
                }
            }
            result.setColumn(y + 1);
        } else {
            result.setColumn(getLastCrdShip(attackShip).getColumn());
            int x = 0;
            for (int i = 0; i < shipCoordinates.size(); i++) {
                if (x < shipCoordinates.get(i).getRow()) {
                    x = shipCoordinates.get(i).getRow();
                }
            }
            result.setRow(x + 1);
        }
        return result;
    }

    private int getMinusColumnShip(AttackShip attackShip) {
        int result = 9;
        for (int i = 0; i < attackShip.getCoordinates().size(); i++) {
            int y = attackShip.getCoordinates().get(i).getColumn();
            if (result > y) {
                result = y;
            }
        }
        if (result == 0) {
            return 0;
        }
        return result - 1;
    }

    private int getPlusColumnShip(AttackShip attackShip) {
        int result = 0;
        for (int i = 0; i < attackShip.getCoordinates().size(); i++) {
            int y = attackShip.getCoordinates().get(i).getColumn();
            if (result < y) {
                result = y;
            }
        }
        if (result == 9) {
            return 9;
        }
        return result + 1;
    }

    private int getMinusRowShip(AttackShip attackShip) {
        int result = 9;
        for (int i = 0; i < attackShip.getCoordinates().size(); i++) {
            int x = attackShip.getCoordinates().get(i).getRow();
            if (result > x) {
                result = x;
            }
        }
        if (result == 0) {
            return 0;
        }
        return result - 1;
    }

    private int getPlusRowShip(AttackShip attackShip) {
        int result = 0;
        for (int i = 0; i < attackShip.getCoordinates().size(); i++) {
            int x = attackShip.getCoordinates().get(i).getRow();
            if (result < x) {
                result = x;
            }
        }
        if (result == 9) {
            return 9;
        }
        return result + 1;
    }

    private boolean checkCrdEqualsShip(Coordinate coordinate, AttackShip attackShip) {
        for (int i = 0; i < attackShip.getCoordinates().size(); i++) {
            if (coordinate.toString().equals(attackShip.getCoordinates().get(i).toString())) {
                return true;
            }
        }
        return false;
    }

    private Coordinate getLastCrdShip(AttackShip attackShip) {
        return attackShip.getCoordinates().get(attackShip.getCoordinates().size() - 1);
    }

    private Coordinate getLeftCrdNearShip(AttackShip attackShip) {
        return new Coordinate(attackShip.getCoordinates().get(0).getRow(), attackShip.getCoordinates().get(0).getColumn() - 1);
    }

    private Coordinate getRightCrdNearShip(AttackShip attackShip) {
        return new Coordinate(attackShip.getCoordinates().get(0).getRow(), attackShip.getCoordinates().get(0).getColumn() + 1);
    }

    private Coordinate getUpCrdNearShip(AttackShip attackShip) {
        return new Coordinate(attackShip.getCoordinates().get(0).getRow() - 1, attackShip.getCoordinates().get(0).getColumn());
    }

    private Coordinate getDownCrdNearShip(AttackShip attackShip) {
        return new Coordinate(attackShip.getCoordinates().get(0).getRow() + 1, attackShip.getCoordinates().get(0).getColumn());
    }

}