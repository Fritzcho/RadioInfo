package models;

/**
 * Model/data class models.Channel that represents a channel object.
 *
 * @author Alireza Ramezani, id19ari
 * @version 1.0
 */
public class Channel {
    public final String id;
    public final String name;
    public Channel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * To string method that returns the channel name only, used in the GUI list
     * @return String channel name
     */
    @Override
    public String toString() {
        return name;
    }
}
