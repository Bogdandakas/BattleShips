package lt.bogdan.battle_ships.beans;

import java.util.LinkedList;
import java.util.List;

public class ServerResponse {

    private String id;
    private String nextTurnForUserId;
    private String status;
    private String winnerUserId;
    private List<Event> events = new LinkedList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNextTurnForUserId() {
        return nextTurnForUserId;
    }

    public void setNextTurnForUserId(String nextTurnForUserId) {
        this.nextTurnForUserId = nextTurnForUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWinnerUserId() {
        return winnerUserId;
    }

    public void setWinnerUserId(String winnerUserId) {
        this.winnerUserId = winnerUserId;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
