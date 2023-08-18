package models;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UpdaterModel handles everything related to fetching and updating the list of programmes.
 *
 * @author Alireza Ramezani, id19ari
 * @version 1.0
 */
public class UpdaterModel {
    /**
     * Update the channel with the given id in the hashmap by fetching the data anew with the help of
     * parser.updateChannel()
     * @param programMap The hashmap with the cache
     * @param id String with the id of the channel that should be updated
     * @return DefaultTableModel containing the information that should be rendered in the GUI
     */
    public DefaultTableModel updateChannel(ConcurrentHashMap<String, ArrayList<Programme>> programMap, String id) {
        ArrayList<Programme> programmes;
        RadioParser parser = new RadioParser();
        programmes = parser.updateChannel(id, programMap);
        return createTable(programmes);
    }

    /**
     * Update the current channel with the latest information in the API.
     * @param programMap Hashmap with the programme cache
     * @param currentChannel Id of the current channel
     * @return DefaultTableModel to be rendered in the GUI.
     */
    public DefaultTableModel channelWorker(ConcurrentHashMap<String, ArrayList<Programme>> programMap, String currentChannel) {
        DefaultTableModel tableModel;
        tableModel = updateChannel(programMap, currentChannel);
        if (tableModel != null && (tableModel.getRowCount() > 0))
            return tableModel;
        else
            return null;
    }

    /**
     * Fetch all the programmes in a channel. Fetch them from the cache if the channel is cached, and from the
     * parser if no such data exists in the cache.
     * @param programMap Programme cache
     * @param id Channel id
     * @return DefaultTableModel to be rendered in the GUI
     */
    public DefaultTableModel getChannel(ConcurrentHashMap<String, ArrayList<Programme>> programMap, String id) {
        ArrayList<Programme> programmes;
        if (programMap.containsKey(id)) {
            programmes = programMap.get(id);
        } else {
            RadioParser parser = new RadioParser();
            programmes = parser.updateChannel(id, programMap);
            programMap.put(id, programmes);
        }
        return createTable(programmes);
    }

    /**
     * Create and fill a DefaultTableModel with data from an ArrayList of programmes and return it.
     * @param programmes ArrayList of programmes to add to tablemodel
     * @return DefaultTableModel to be rendered in GUI
     */
    private DefaultTableModel createTable(ArrayList<Programme> programmes) {
        String[] header = {"bild", "beskrivning", "Program", "Start", "Slut"};
        DefaultTableModel tableModel = new DefaultTableModel(header, 0);
        if (!programmes.isEmpty()) {
            for (Programme p : programmes) {
                String start = p.LTStart;
                String end = p.LTEnd;
                Object[] data = {p.imagePath, p.description, p.name, start, end};
                tableModel.addRow(data);
            }
        }
        return tableModel;
    }
}
