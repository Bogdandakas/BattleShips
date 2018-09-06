package lt.bogdan.battle_ships.ui;

import lt.bogdan.battle_ships.beans.*;
import lt.bogdan.battle_ships.services.BotService;
import lt.bogdan.battle_ships.services.GameServiceImpl;
import lt.bogdan.battle_ships.services.LocalService;

import java.util.Scanner;

public class App {

    private static final String NAME = "Bogdan";
    private static final String EMAIL = "bogdan@gmail.com";
    private static final String USER_ID = "User id: ";
    private static final String GAME_ID = "Game id: ";
    private static final String STATUS = "Status: ";
    private static final String TURN = "Turn: ";
    private static final String WINNER = "Winner: ";
    private static final String LOG_INFO = "Log info: ";
    private static final String COORDINATE = "Coordinate: ";
    private static final String HIT = "Hit: ";
    private static final String WAITING_FOR_TURN = "Waiting for turn: ";
    private static final String TIME_OUT = "Time out has occurred";
    private static final String SHIPS_COORDINATES = "Ships coordinates: ";
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String MANUALLY = "MANUALLY GAME";
    private static final String BOT = "BOT GAME";
    private static final int MAX_SHIP_SIZE = 4;
    private static final String SHIP_SIZE = ", ship size: ";
    private static final String SHIP_NUMBER = "Ship number: ";

    static Scanner scanner = new Scanner(System.in);
    static GameServiceImpl gameServiceImpl = new GameServiceImpl();
    static ServerResponse servResp = new ServerResponse();
    static LocalService localService = new LocalService();
    static BotService botService = new BotService();
    static String gameType = MANUALLY;

    public static void main(String[] args) {
        startGame();
        scanner.close();
    }

    private static void startGame() {
        User user = null;
        String gameId = "";
        GameTable userGT = null;
        GameTable oppGT = null;

        boolean continueOn = true;
        while (continueOn) {
            printLn("0. Exit");
            printLn("1. Create user");
            printLn("2. Join user");
            printLn("3. Create ships");
            printLn("4. Setup game");
            printLn("5. Response status");
            printLn("6. Start battle");
            printLn("7. Change the game type");
            printLn(gameType);

            int input = scanner.nextInt();
            switch (input) {
                case 0:
                    continueOn = false;
                    break;
                case 1:
                    if (user == null) {
                        user = gameServiceImpl.createUser(NAME, EMAIL);
                        printLn(USER_ID + user.getId());
                    }
                    break;
                case 2:
                    gameId = gameServiceImpl.join(user.getId()).getId();
                    servResp = gameServiceImpl.status(gameId);
                    printLn(GAME_ID + gameId);
                    break;
                case 3:
                    userGT = new GameTable();
                    oppGT = new GameTable();
                    if (gameType.equals(MANUALLY)) {
                        printLn("Input ship first coordinate. Format LLN");
                        printLn("First letter - ship direction: 'V' or 'H'");
                        printLn("Second letter - column coordinate");
                        printLn("Number - row coordinate");
                        createUserShipsManually(userGT);
                    } else {
                        botService.createUserShipsWithBot(userGT);
                        printBattleFields(userGT, oppGT);
                    }
                    break;
                case 4:
                    if (servResp.getStatus().equals(GameServiceImpl.READY_FOR_SHIPS)) {
                        gameServiceImpl.setup(gameId, user.getId(), userGT.getShipsCoordinates());
                        break;
                    }
                case 5:
                    if (!gameId.isEmpty()) {
                        servResp = gameServiceImpl.status(gameId);
                        printServerResponse(servResp);
                    }
                    break;
                case 6:
                    if (servResp.getStatus().equals(GameServiceImpl.READY_TO_PLAY))
                        run(userGT, oppGT, gameId, user);
                    break;
                case 7:
                    if (gameType.equals(MANUALLY)) {
                        gameType = BOT;
                    } else {
                        gameType = MANUALLY;
                    }
                    break;
                default:
                    break;
            }

            if (!gameId.isEmpty()) {
                wait(1);
                servResp = gameServiceImpl.status(gameId);
                printLn(servResp.getStatus());
            }
        }
    }

    public static void run(GameTable userGT, GameTable oppGT, String gameId, User user) {
        AttackShip attackShip = new AttackShip();
        boolean continueRun = true;
        while (continueRun) {
            servResp = gameServiceImpl.status(gameId);

            if (servResp.getStatus().equals(GameServiceImpl.READY_TO_PLAY)) {
                printLn(TURN + servResp.getNextTurnForUserId());
                int eventsCount = servResp.getEvents().size();

                if (servResp.getNextTurnForUserId().equals(user.getId())) {
                    String turnCoordinate = getTurnCoordinate(oppGT, attackShip);
                    gameServiceImpl.turn(gameId, user.getId(), turnCoordinate);
                    printLn(TURN + user.getName() + " " + turnCoordinate);
                    servResp = gameServiceImpl.status(gameId);
                    localService.updateAttackShip(user, servResp, attackShip);
                } else {
                    while (checkNotTurnOpponent(gameId, eventsCount)) {
                        printLn(WAITING_FOR_TURN + servResp.getNextTurnForUserId());
                        wait(1);
                    }
                }
            }
            localService.updateGameTables(user, userGT, oppGT, servResp);
            printBattleFields(userGT, oppGT);

            if (servResp.getStatus().equals(GameServiceImpl.FINISHED)) {
                servResp = gameServiceImpl.status(gameId);
                localService.updateGameTables(user, userGT, oppGT, servResp);
                printBattleFields(userGT, oppGT);
                printServerResponse(servResp);
                continueRun = false;
            }
        }
    }

    private static void createUserShipsManually(GameTable userGT) {
        Scanner scanner = new Scanner(System.in);
        for (int shipSize = MAX_SHIP_SIZE; shipSize > 0; shipSize--) {
            for (int shipNumber = 1; shipNumber <= 5 - shipSize; shipNumber++) {
                printLn(SHIP_NUMBER + shipNumber + SHIP_SIZE + shipSize);
                boolean trayAnother = true;
                while (trayAnother) {
                    String input = scanner.next();
                    Ship ship = new Ship(input.substring(0, 1), input.substring(2, 3), convertLetterToNumber(input.substring(1, 2)), shipSize);
                    if (localService.checkIsSpaceForShip(userGT, ship)) {
                        localService.addShipToUserBF(userGT, ship);
                        localService.addShipCoordinates(userGT, ship);
                        printBattleFields(userGT, new GameTable());
                        trayAnother = false;

                    }
                }
            }
        }
    }

    private static String getTurnCoordinate(GameTable oppGT, AttackShip attackShip) {
        String turnCoordinate;
        if (gameType.equals(MANUALLY)) {
            turnCoordinate = scanInputTurnCoordinate(attackShip);
        } else {
            turnCoordinate = botService.findTurnCoordinate(oppGT, attackShip);
        }
        return turnCoordinate;
    }

    private static String scanInputTurnCoordinate(AttackShip attackShip) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next();
        attackShip.setTurnCoordinate(new Coordinate(input.substring(1, 2), convertLetterToNumber(input.substring(0, 1))));
        return attackShip.getTurnCoordinate().toString();
    }

    private static int convertLetterToNumber(String letter) {
        for (int i = 0; i < GameTable.COLUMNS.length; i++) {
            if (GameTable.COLUMNS[i].equals(letter)) {
                return i;
            }
        }
        return -1;
    }

    public static void printBattleFields(GameTable userGT, GameTable oppGT) {
        String[] area = GameTable.COLUMNS;
        printSpaceAndObject(1, EMPTY);
        for (int column = 0; column < area.length; column++) {
            printSpaceAndObject(1, area[column]);
        }
        printSpaceAndObject(5, EMPTY);
        for (int column = 0; column < area.length; column++) {
            printSpaceAndObject(1, area[column]);
        }
        print("\n");

        for (int row = 0; row < area.length; row++) {
            print(row);
            for (int column = 0; column < area.length; column++) {
                printSpaceAndObject(1, userGT.getField(new Coordinate(row, column)));
            }
            printSpaceAndObject(4, row);
            for (int column = 0; column < area.length; column++) {
                printSpaceAndObject(1, oppGT.getField(new Coordinate(row, column)));
            }
            print("\n");
        }
        printLn(SHIPS_COORDINATES + userGT.getShipsCoordinates());
    }

    private static void wait(int second) {
        try {
            Thread.sleep(1000 * second);
        } catch (InterruptedException e) {
            System.out.println(TIME_OUT);
        }
    }

    private static boolean checkNotTurnOpponent(String gameId, int eventsCount) {
        if (gameServiceImpl.status(gameId).getEvents().size() > eventsCount) {
            return false;
        } else {
            return true;
        }

    }

    private static void printServerResponse(ServerResponse serverResponse) {
        if (serverResponse.getWinnerUserId().isEmpty()) {
            StringBuilder status = new StringBuilder();
            status.append(GAME_ID + serverResponse.getId()).append(" ");
            status.append(STATUS + serverResponse.getStatus()).append(" ");
            status.append(TURN).append(serverResponse.getNextTurnForUserId());
            printLn(status);

            if (serverResponse.getEvents() != null && serverResponse.getEvents().size() != 0) {
                for (int i = 0; i < serverResponse.getEvents().size(); i++) {
                    StringBuilder event = new StringBuilder();
                    event.append(LOG_INFO);
                    event.append(TURN).append(serverResponse.getEvents().get(i).getUserId()).append(" ");
                    event.append(COORDINATE).append(serverResponse.getEvents().get(i).getCoordinate()).append(" ");
                    event.append(HIT).append(serverResponse.getEvents().get(i).isHit());
                    printLn(event);
                }
            }
        } else {
            printLn(WINNER + serverResponse.getWinnerUserId());
        }
    }

    private static void printSpaceAndObject(int count, Object obj) {
        for (int i = 0; i < count; i++) {
            System.out.print(SPACE);
        }
        System.out.print(obj);
    }

    private static void print(Object obj) {
        System.out.print(obj);
    }

    private static void printLn(Object obj) {
        System.out.println(obj);
    }

}
