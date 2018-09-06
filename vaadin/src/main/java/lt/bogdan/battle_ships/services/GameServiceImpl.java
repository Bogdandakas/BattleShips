package lt.bogdan.battle_ships.services;

import lt.bogdan.battle_ships.beans.Event;
import lt.bogdan.battle_ships.beans.ServerResponse;
import lt.bogdan.battle_ships.beans.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GameServiceImpl implements GameServiceInterface {
//    private static final String SERVER_URL = "http://miskoverslas.lt/laivu_musis/";
//        public static final String SERVER_URL = "http://192.168.1.231:9999/";
    public static final String SERVER_URL = "http://78.56.120.176:9999/";

    private static final String CREATE_USER_METHOD = "create_user";
    private static final String JOIN_METHOD = "join";
    private static final String SETUP_METHOD = "setup";
    private static final String TURN_METHOD = "turn";
    private static final String STATUS_METHOD = "status";
    private static final String AND = "&";
    private static final String SEPARATOR = "?";

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String NEXT_TURN_FOR_USER_ID = "nextTurnForUserId";
    private static final String STATUS = "status";
    private static final String WINNER_USER_ID = "winnerUserId";
    private static final String DATE = "date";
    private static final String COORDINATE = "coordinate";
    private static final String COLUMN = "column";
    private static final String HIT = "hit";
    private static final String EVENTS = "events";
    private static final String USER_ID = "userId";

    private static final String ATTRIBUTE_NAME = "name=";
    private static final String ATTRIBUTE_EMAIL = "email=";
    private static final String ATTRIBUTE_USER_ID = "user_id=";
    private static final String ATTRIBUTE_GAME_ID = "game_id=";
    private static final String ATTRIBUTE_DATA = "data=";

    public static final String READY_FOR_SECOND_PLAYER = "READY_FOR_SECOND_PLAYER";
    public static final String READY_TO_PLAY = "READY_TO_PLAY";
    public static final String FINISHED = "FINISHED";
    public static final String READY_FOR_SHIPS = "READY_FOR_SHIPS";

    @Override
    public User createUser(String name, String email) {
        String url = SERVER_URL + CREATE_USER_METHOD + SEPARATOR + ATTRIBUTE_NAME + name + AND + ATTRIBUTE_EMAIL + email;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request);

            String resp = getResponseAsString(response.getEntity().getContent());

            return convertUser(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ServerResponse join(String user_id) {
        System.out.println(SERVER_URL + JOIN_METHOD + SEPARATOR +
                ATTRIBUTE_USER_ID + user_id);
        return getServerResponse(SERVER_URL + JOIN_METHOD + SEPARATOR +
                ATTRIBUTE_USER_ID + user_id);
    }

    @Override
    public ServerResponse setup(String game_Id, String user_Id, String data) {
        System.out.println(SERVER_URL + SETUP_METHOD + SEPARATOR +
                ATTRIBUTE_GAME_ID + game_Id + AND +
                ATTRIBUTE_USER_ID + user_Id + AND +
                ATTRIBUTE_DATA + data);
        return getServerResponse(SERVER_URL + SETUP_METHOD + SEPARATOR +
                ATTRIBUTE_GAME_ID + game_Id + AND +
                ATTRIBUTE_USER_ID + user_Id + AND +
                ATTRIBUTE_DATA + data);
    }

    @Override
    public ServerResponse turn(String game_Id, String user_Id, String data) {
        System.out.println(SERVER_URL + TURN_METHOD + SEPARATOR +
                ATTRIBUTE_GAME_ID + game_Id + AND +
                ATTRIBUTE_USER_ID + user_Id + AND +
                ATTRIBUTE_DATA + data);
        return getServerResponse(SERVER_URL + TURN_METHOD + SEPARATOR +
                ATTRIBUTE_GAME_ID + game_Id + AND +
                ATTRIBUTE_USER_ID + user_Id + AND +
                ATTRIBUTE_DATA + data);
    }

    @Override
    public ServerResponse status(String game_Id) {
        return getServerResponse(SERVER_URL + STATUS_METHOD + SEPARATOR +
                ATTRIBUTE_GAME_ID + game_Id);
    }

    private User convertUser(String body) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(body);
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            String name = (String) jsonObject.get(NAME);
            String email = (String) jsonObject.get(EMAIL);
            String id = (String) jsonObject.get(ID);
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setName(name);
            return user;
        }
        return null;
    }

    private ServerResponse convertServerResponse(String body) throws ParseException {
        ServerResponse serverResponse = new ServerResponse();

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(body);

        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            String gameId = (String) jsonObject.get(ID);
            String nextTurnForUserId = (String) jsonObject.get(NEXT_TURN_FOR_USER_ID);
            String status = (String) jsonObject.get(STATUS);
            String winnerUserId = (String) jsonObject.get(WINNER_USER_ID);

            serverResponse.setId(gameId);
            serverResponse.setNextTurnForUserId(nextTurnForUserId);
            serverResponse.setStatus(status);
            serverResponse.setWinnerUserId(winnerUserId);

            JSONArray eventsArr = (JSONArray) jsonObject.get(EVENTS);
            serverResponse.setEvents(convertEvents(eventsArr));
        }
        return serverResponse;
    }

    private List<Event> convertEvents(JSONArray eventsArr) {
        List<Event> eventList = new LinkedList<>();

        for (int i = 0; i < eventsArr.size(); i++) {
            Event event = new Event();

            Map<String, Object> mapEvent = (Map<String, Object>) eventsArr.get(i);
            for (String key : mapEvent.keySet()) {

                if (key.equals(DATE)) {
                    event.setDate(new Date((Long) mapEvent.get(key)));
                }
                if (key.equals(HIT)) {
                    event.setHit((Boolean) mapEvent.get(key));
                }
                if (key.equals(USER_ID)) {
                    event.setUserId(mapEvent.get(key).toString());
                }
                if (key.equals(COORDINATE)) {
                    Map<String, Object> mapCoordinate = (Map<String, Object>) mapEvent.get(COORDINATE);
                    StringBuilder xY = new StringBuilder();

                    for (String key1 : mapCoordinate.keySet()) {
                        if (key1.equals(COLUMN)) {
                            xY.append(mapCoordinate.get(key1).toString());
                        } else {
                            xY.append(mapCoordinate.get(key1).toString());
                        }
                    }
                    event.setCoordinate(xY.toString());
                }
            }
            eventList.add(event);
        }
        return eventList;
    }

    private ServerResponse getServerResponse(String url) {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);

            HttpResponse response = client.execute(request);

            String resp = getResponseAsString(response.getEntity().getContent());

            return convertServerResponse(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getResponseAsString(InputStream inputStream) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

}
