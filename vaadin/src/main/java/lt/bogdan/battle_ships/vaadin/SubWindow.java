package lt.bogdan.battle_ships.vaadin;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class SubWindow {

    private static final String USER_ID = "User id: ";
    private static final String GAME_ID = "Game id: ";
    private static final String OPPONENT_ID = "Opponent id: ";
    private static final String SHIPS_COORDINATES = "Ships coordinates: ";
    public static final String GAME_INFO = "Game info";

    Window subWindow;

    public SubWindow() {
        subWindow = new Window(GAME_INFO);
    }

    public void createWindow(String userId, String gameId, String shipsCoordinates, String oppId){

        VerticalLayout windowContent = new VerticalLayout();
        windowContent.setMargin(true);
        windowContent.addComponent(new Label(USER_ID + userId));
        windowContent.addComponent(new Label(GAME_ID + gameId));
        windowContent.addComponent(new Label(SHIPS_COORDINATES + shipsCoordinates));
        windowContent.addComponent(new Label(OPPONENT_ID + oppId));
        windowContent.addComponent(new Label(MyUI.TURN + MyUI.USER));
        windowContent.addComponent(new Label(MyUI.TURN + MyUI.OPPONENT));

        subWindow.setContent(windowContent);
        subWindow.setModal(true);
        UI.getCurrent().addWindow(subWindow);
    }

}
