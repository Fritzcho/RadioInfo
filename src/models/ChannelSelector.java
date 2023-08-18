package models;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class MenuSelectListener is responsible for the actionPerformed when
 * a channel, is selected in the GUI.
 *
 * @author Alireza Ramezani, id19ari
 * @version 1.0
 */
public class ChannelSelector {
    private String currentChannel;
    public String getCurrentChannel() {
        return currentChannel;
    }


    /**
     * Call the updateChannel() method to fetch all the programmes in the channel.
     * If there is already a cache for the channel, use the cache instead of fetching new data.
     * Then configure the GUI JTable with the data and change currentChannel to the new channel id.
     * @param id channel id
     * @param programMap Hashmap of all the programs
     */
    public DefaultTableModel selectChannel(String id, ConcurrentHashMap<String, ArrayList<Programme>> programMap) {
        DefaultTableModel tableModel;
        tableModel = new UpdaterModel().getChannel(programMap, id);
        currentChannel = id;
        if (tableModel != null && (tableModel.getRowCount()>0)) {
            return tableModel;
        } else {
            return null;
        }
    }
}