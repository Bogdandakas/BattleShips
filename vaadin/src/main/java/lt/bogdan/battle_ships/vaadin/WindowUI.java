package lt.bogdan.battle_ships.vaadin;


import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import lt.bogdan.battle_ships.beans.Coordinate;
import lt.bogdan.battle_ships.beans.GameTable;
import lt.bogdan.battle_ships.beans.Ship;

public class WindowUI extends CustomComponent {

    Layout mainLayout;

    public WindowUI() {
        mainLayout = new GridLayout(23, 11);
        this.setCompositionRoot(mainLayout);
        createBattleField(new GameTable(), new GameTable());
    }

    public void createBattleField(GameTable userBF, GameTable oppGT) {
        mainLayout.removeAllComponents();
        int tableLength = GameTable.COLUMNS.length;
        createHeaderBody(tableLength);
        createBattleTablesBody(tableLength, userBF, oppGT);
    }

    private void createHeaderBody(int tableLength) {
        mainLayout.addComponent(new Label());
        createBattleFieldHeader(tableLength);
        addEmptyLabel(1);
        addEmptyLabel(1);
        createBattleFieldHeader(tableLength);
    }

    private void createBattleFieldHeader(int tableLegth) {
        for (int column = 0; column < tableLegth; column++) {
            mainLayout.addComponent(new Label(GameTable.COLUMNS[column]));
        }
    }

    private void createBattleTablesBody(int length, GameTable userGT, GameTable oppGT) {
        for (int row = 0; row < length; row++) {
            mainLayout.addComponent(new Label(String.valueOf(row)));
            for (int column = 0; column < length; column++) {
                Button button = createButton(userGT, row, column);
                mainLayout.addComponent(button);
                updateOnClickField(userGT, new Coordinate(row, column).toString(), "", button.getCaption());
            }

            addEmptyLabel(10);
            mainLayout.addComponent(new Label(String.valueOf(row)));

            for (int column = 0; column < length; column++) {
                Button button = createButton(oppGT, row, column);
                mainLayout.addComponent(createButton(oppGT, row, column));
                updateOnClickField(oppGT, new Coordinate(row, column).toString(), "", "");
            }
        }
    }

    private Button createButton(GameTable gameTable, int row, int column) {
        Coordinate coordinate = new Coordinate(row, column);
        Button button = new Button(gameTable.getField(coordinate),
                click -> onClickField(gameTable, coordinate));
        button.setId(coordinate.toString() + gameTable.toString());
        button.setWidth("43px");
        return button;
    }

    public void updateOnClickField(GameTable gameTable, String coordinate, String styleName, String captionValue) {
        for (Component component : mainLayout) {
            try {
                if (component.getId() != null &&
                        component.getId().equals(coordinate + gameTable.toString())) {
                    if (!styleName.isEmpty()) {
                        component.setStyleName(styleName);
                    }
                    if (!captionValue.isEmpty()) {
                        component.setCaption(captionValue);
                    }
                    if (captionValue.equals(Ship.SHIP)) {
                        component.setStyleName("DarkGray");
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void onClickField(GameTable gameTable, Coordinate coordinate) {
        setButtonEnabled(gameTable, coordinate);
//        updateOnClickField(gameTable, coordinate.toString(), "", GameTable.TURN);
        gameTable.setField(coordinate, GameTable.TURN);
    }

    private void setButtonEnabled(GameTable gameTable, Coordinate coordinate) {
        for (Component component : mainLayout) {
            if (component.getId() != null && component.getId().equals(coordinate.toString() + gameTable.toString())) {
                component.setEnabled(false);
            }
        }
    }


    private void onClickCreateShip(GameTable gameTable, Coordinate coordinate) {
        gameTable.setField(coordinate, Ship.SHIP);
    }

    private void addEmptyLabel(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("&nbsp;");
        }
        mainLayout.addComponent(new Label(sb.toString(), ContentMode.HTML));
    }

}
