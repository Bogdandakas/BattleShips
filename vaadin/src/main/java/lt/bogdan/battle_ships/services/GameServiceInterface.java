package lt.bogdan.battle_ships.services;


import lt.bogdan.battle_ships.beans.User;
import lt.bogdan.battle_ships.beans.ServerResponse;

public interface GameServiceInterface {

    User createUser(String name, String email);

    public ServerResponse join(String user_id);

    public ServerResponse setup(String game_Id, String user_Id, String data);

    public ServerResponse turn(String game_Id, String user_Id, String data);

    public ServerResponse status(String game_Id);

}
