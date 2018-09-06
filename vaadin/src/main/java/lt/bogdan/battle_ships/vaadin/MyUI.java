package lt.bogdan.battle_ships.vaadin;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.UIEvents;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import lt.bogdan.battle_ships.beans.*;
import lt.bogdan.battle_ships.services.BotService;
import lt.bogdan.battle_ships.services.GameServiceImpl;
import lt.bogdan.battle_ships.services.LocalService;

import javax.servlet.annotation.WebServlet;
import java.util.Scanner;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    private static final String NAME = "Bogdan";
    private static final String EMAIL = "bogdangerass@gmail.com";
    private static final String STATUS = "Status: ";
    private static final String GAME_VERSION = "Battle ships 18.8 ";
    private static final String IS_ON = "is on ";
    private static final String MANUALLY = "MANUALLY GAME";
    private static final String BOT = "BOT GAME";
    public static final String TURN = "Turn ";
    public static final String USER = "user";
    public static final String OPPONENT = "opponent";
    private static final String WINNER = "Winner: ";
    private static final String EMPTY = " ";
    private static final String CREATE_USER = "Create user";
    private static final String JOIN_GAME = "Join game";
    private static final String CREATE_SHIPS = "Create ships";
    private static final String SETUP_GAME = "Setup game";
    private static final String CHANGE_TYPE = "Change type";
    private static final String EXIT = "Exit";
    private static final String READY_FOR_CREATE_USER = "READY_FOR_CREATE_USER";
    private static final String READY_FOR_JOIN_GAME = "READY_FOR_JOIN_GAME";

    static LocalService localService = new LocalService();
    static BotService botService = new BotService();
    GameTable userGT = new GameTable();
    GameTable oppGT = new GameTable();
    WindowUI battleField = new WindowUI();
    SubWindow subWindow = new SubWindow();
    AttackShip attackShip;
    ServerResponse servResp = new ServerResponse();
    GameServiceImpl gameServiceImpl = new GameServiceImpl();
    User user = new User();

    String gameId = "";
    String gameType = BOT;
    int i = 0;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        servResp.setStatus(READY_FOR_CREATE_USER);
        setPollInterval(1000);

        VerticalLayout layout = new VerticalLayout();

        Layout headerLayout = new HorizontalLayout();
        Label headerLabel = new Label();
        headerLayout.addComponent(headerLabel);

        Layout menuLayout = new HorizontalLayout();
        MenuBar menuBar = createMenuBar();
        menuLayout.addComponent(menuBar);

        Layout statusLayout = new HorizontalLayout();
        Label statusLabel = new Label();
        statusLayout.addComponent(statusLabel);

        Layout infoLayout = new HorizontalLayout();
        Label infoLabel = new Label();
        infoLayout.addComponent(infoLabel);

        Layout battleFieldLayout = new HorizontalLayout();
        battleFieldLayout.addComponent(battleField);

        addPollListener(new UIEvents.PollListener() {
            @Override
            public void poll(UIEvents.PollEvent event) {
                i++;
                headerLabel.setValue(GAME_VERSION + gameType + EMPTY + IS_ON + String.format("%02d:%02d:%02d", i / 3600, (i % 3600) / 60, (i % 60)));

                if (!gameId.isEmpty()) {
                    servResp = gameServiceImpl.status(gameId);
                }
                if (servResp.getStatus().equals(GameServiceImpl.READY_TO_PLAY)) {
                    if (servResp.getNextTurnForUserId().equals(user.getId())) {
                        String turnCoordinate = getTurnCoordinate(oppGT, attackShip);
                        if (!turnCoordinate.isEmpty()) {
                            gameServiceImpl.turn(gameId, user.getId(), turnCoordinate);
                        }
                    }

                    servResp = gameServiceImpl.status(gameId);
                    if (servResp.getNextTurnForUserId().equals(user.getId())) {
                        infoLabel.setValue(TURN + NAME);
                    } else {
                        infoLabel.setValue(TURN + OPPONENT);
                    }
                    localService.updateAttackShip(user, servResp, attackShip);
                    localService.updateGameTables(user, userGT, oppGT, servResp);
                    updateOppBF(oppGT);
                    updateUserBF(userGT);
                }

                enabledMenuBar(menuBar, servResp.getStatus());

                if (servResp.getStatus().equals(GameServiceImpl.FINISHED)) {
                    localService.updateGameTables(user, userGT, oppGT, servResp);
                    updateUserBF(userGT);
                    infoLabel.setValue(getWinnerName());
                }
                statusLabel.setValue(STATUS + servResp.getStatus());
            }

        });

        layout.addComponent(headerLayout);
        layout.addComponent(menuLayout);
        layout.addComponent(statusLayout);
        layout.addComponent(infoLayout);
        layout.addComponent(battleFieldLayout);
        setContent(layout);

    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        MenuBar.Command command = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                String oppId = EMPTY;
                try {
                    if (!user.getId().equals(servResp.getNextTurnForUserId())) {
                        oppId = servResp.getNextTurnForUserId();
                    }
                } catch (Exception e) {
                }
                subWindow.createWindow(user.getId(), gameId, userGT.getShipsCoordinates(), oppId);
            }
        };
        MenuBar.MenuItem gameInfo = menuBar.addItem(SubWindow.GAME_INFO, command);

        MenuBar.Command create1 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                user = gameServiceImpl.createUser(NAME, EMAIL);
                servResp.setStatus(READY_FOR_JOIN_GAME);
            }
        };
        MenuBar.MenuItem createUser = menuBar.addItem(CREATE_USER, null, create1);

        MenuBar.Command command1 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                gameId = gameServiceImpl.join(user.getId()).getId();
                attackShip = new AttackShip();
                servResp.setStatus(servResp.getStatus() + EMPTY + gameId);
            }
        };
        MenuBar.MenuItem joinUser = menuBar.addItem(JOIN_GAME, null, command1);

        MenuBar.Command command2 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {

                userGT = botService.createUserShipsWithBot(new GameTable());
                battleField.createBattleField(userGT, localService.clearGameTable(oppGT));

            }
        };
        MenuBar.MenuItem createShips = menuBar.addItem(CREATE_SHIPS, null, command2);

        MenuBar.Command command3 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                gameServiceImpl.setup(gameId, user.getId(), userGT.getShipsCoordinates());
            }
        };
        MenuBar.MenuItem setupGame = menuBar.addItem(SETUP_GAME, null, command3);

        MenuBar.Command command4 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                setGameType();
            }
        };
        MenuBar.MenuItem gameType = menuBar.addItem(CHANGE_TYPE, null, command4);

        MenuBar.Command command5 = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MyUI.this.close();
            }
        };
        MenuBar.MenuItem gameExit = menuBar.addItem(EXIT, null, command5);

        return menuBar;
    }

    private void setGameType() {
        if (gameType.equals(MANUALLY)) {
            gameType = BOT;
        } else {
            gameType = MANUALLY;
        }
    }

    private String getWinnerName() {
        String result = WINNER + OPPONENT;
        if (user.getId().equals(servResp.getWinnerUserId())) {
            result = WINNER + NAME;
        }
        return result;
    }

    private void enabledMenuBar(MenuBar menuBar, String status) {
        for (int j = 0; j < menuBar.getItems().size(); j++) {
            menuBar.getItems().get(j).setEnabled(true);

            if (status.equals(READY_FOR_CREATE_USER)) {
                if (menuBar.getItems().get(j).getText() == JOIN_GAME || menuBar.getItems().get(j).getText() == SETUP_GAME) {
                    menuBar.getItems().get(j).setEnabled(false);
                }
            }
            if (status.equals(GameServiceImpl.READY_FOR_SECOND_PLAYER) || status.equals(READY_FOR_JOIN_GAME)) {
                if (menuBar.getItems().get(j).getText() == CREATE_USER || menuBar.getItems().get(j).getText() == SETUP_GAME) {
                    menuBar.getItems().get(j).setEnabled(false);
                }
            }
            if (status.equals(GameServiceImpl.READY_FOR_SHIPS)) {
                if (menuBar.getItems().get(j).getText() == CREATE_USER) {
                    menuBar.getItems().get(j).setEnabled(false);
                }
            }
            if (status.equals(GameServiceImpl.READY_TO_PLAY)) {
                if (menuBar.getItems().get(j).getText() == CREATE_USER || menuBar.getItems().get(j).getText() == JOIN_GAME ||
                        menuBar.getItems().get(j).getText() == CREATE_SHIPS || menuBar.getItems().get(j).getText() == SETUP_GAME ||
                        menuBar.getItems().get(j).getText() == CHANGE_TYPE) {
                    menuBar.getItems().get(j).setEnabled(false);
                }
            }
            if (status.equals(GameServiceImpl.FINISHED)) {
                if (menuBar.getItems().get(j).getText() == SETUP_GAME || menuBar.getItems().get(j).getText() == CREATE_SHIPS) {
                    menuBar.getItems().get(j).setEnabled(false);
                }
            }
        }
    }

    private void updateUserBF(GameTable userGT) {
        for (int row = 0; row < GameTable.COLUMNS.length; row++) {
            for (int column = 0; column < GameTable.COLUMNS.length; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                if (userGT.getField(coordinate).equals(GameTable.TURN)) {
                    battleField.updateOnClickField(userGT, coordinate.toString(), "", GameTable.TURN);
                }
                if (userGT.getField(coordinate).equals(GameTable.HIT)) {
                    battleField.updateOnClickField(userGT, coordinate.toString(), "Red", GameTable.HIT);
                }
            }
        }
    }

    private void updateOppBF(GameTable oppGT) {
        for (int row = 0; row < GameTable.COLUMNS.length; row++) {
            for (int column = 0; column < GameTable.COLUMNS.length; column++) {
                Coordinate coordinate = new Coordinate(row, column);
                if (oppGT.getField(coordinate).equals(GameTable.TURN)) {
                    battleField.updateOnClickField(oppGT, coordinate.toString(), "", GameTable.TURN);
                }
                if (oppGT.getField(coordinate).equals(GameTable.HIT)) {
                    battleField.updateOnClickField(oppGT, coordinate.toString(), "friendly", GameTable.HIT);
                }
            }
        }
    }

    private String getEnabledButtonId() {

        for (Component component : battleField.mainLayout) {
            if (component != null &&
                    !component.isEnabled()) {
                component.setEnabled(true);
                return component.getId().substring(0, 2);
            }
        }
        return "";
    }

    private String getTurnCoordinate(GameTable oppGT, AttackShip attackShip) {
        String result = "";
        if (gameType.equals(MANUALLY)) {
           result = getEnabledButtonId();
            if (!result.isEmpty()) {
                attackShip.setTurnCoordinate(new Coordinate(result.substring(1, 2), convertLetterToNumber(result.substring(0, 1))));
            }
        } else {
            result = botService.findTurnCoordinate(oppGT, attackShip);
        }
        return result;
    }

    private static void createUserShipsManually(GameTable userGT) {
        Scanner scanner = new Scanner(System.in);
        for (int shipSize = 4; shipSize > 0; shipSize--) {
            for (int shipNumber = 1; shipNumber <= 5 - shipSize; shipNumber++) {
//                printLn(SHIP_NUMBER + shipNumber + SHIP_SIZE + shipSize);
                boolean trayAnother = true;
                while (trayAnother) {
                    String input = scanner.next();
                    Ship ship = new Ship(input.substring(0, 1), input.substring(2, 3), convertLetterToNumber(input.substring(1, 2)), shipSize);
                    if (localService.checkIsSpaceForShip(userGT, ship)) {
                        localService.addShipToUserBF(userGT, ship);
                        localService.addShipCoordinates(userGT, ship);
//                        printBattleFields(userBF, new WindowUI());
                        trayAnother = false;

                    }
                }
            }
        }
    }

    private static int convertLetterToNumber(String letter) {
        for (int i = 0; i < GameTable.COLUMNS.length; i++) {
            if (GameTable.COLUMNS[i].equals(letter)) {
                return i;
            }
        }
        return -1;
    }
}
