import models.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RadioInfoController is the controller class in the MVC structure of RadioInfo.
 * The class handles all necessary logic for fetching data from the SR API while also handling data caching
 * and updating fetched data in the background. The controller also contains all the necessary listeners to handle
 * changes in the GUI
 *
 * @author Alireza Ramezani, id19ari
 * @version 3.0
 */
public class RadioInfoController {
    private View gui;
    private final ConcurrentHashMap<String, ArrayList<Programme>> programMap = new ConcurrentHashMap<>();
    private String currentChannel;

    /**
     * Fetch all channel and start the GUI through SwingUtilities. Start the background thread responsible for
     * automatic updates of the cache and put the thread to sleep for an hour.
     */
    public RadioInfoController() {
        ArrayList<Channel> channels = new RadioParser().fetchChannels();
        if (channels==null) {
            displayError("Anslutningsfel när kanalerna skulle hämtas");
            return;
        }

        DefaultListModel<Channel> listModel = new DefaultListModel<>();

        for (Channel ch : channels) {
            listModel.addElement(ch);
        }

        SwingUtilities.invokeLater(()-> {
            gui = new View(listModel, this);
        });

        autoUpdate();
    }

    /**
     * Starts the background thread that keeps updating all the cached channels in the background, once every 60 min.
     * Since only the already fetched channels are to be updated, this method cannot make use of the
     * Parser.fetchChannels() method but rather loops through the cache and updates the channels one by one.
     * When all channels are updated, the current channel is then also updated in the GUI
     */
    private void autoUpdate() {
        Thread refreshCache = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000*60*60);
                    SwingWorker<Void, Void> sw = new SwingWorker() {
                        DefaultTableModel table;
                        @Override
                        protected Object doInBackground() {
                            synchronized (programMap) {
                                for (String key : programMap.keySet()) {
                                    new RadioParser().updateChannel(key, programMap);
                                }
                                table = new UpdaterModel().channelWorker(programMap, currentChannel);
                                return null;
                            }
                        }
                        @Override
                        protected void done() {
                            super.done();
                            configureTable(table);
                        }
                    };
                    sw.execute();
                } catch (InterruptedException ie) {
                    displayError("Automatic cache update failed");
                }
            }
        });
        refreshCache.start();
    }

    /**
     * Fetches programme info. Although since the title and description is already saved in the table-model, the only
     * new information that it fetches is the image based on a given image-path.
     * When image is fetched, it configures a JDialog in the gui.
     * @param imagePath API path to fetch the image
     * @param description The programme description
     * @param name The programme title
     * @param selRow The selected row in the table-model, used to make sure the row isn't the table header.
     */
    public void getProgrammeInfo(String imagePath, String description, String name, int selRow) {
        SwingWorker<Void, Void> sw = new SwingWorker<>() {
            BufferedImage image;
            @Override
            protected Void doInBackground() {
                image = new ProgrammeSelector().getInfo(imagePath, selRow);
                return null;
            }
            @Override
            protected void done() {
                super.done();
                configureDetails(image, name, description);
            }
        };
        sw.execute();
    }

    /**
     * Fetches programmes for the selected channel based on the channel id, and updates the GUI table with all the
     * fetched programmes
     * @param id Channel id
     */
    public void selectChannel(String id) {
        SwingWorker<Void, Void> sw = new SwingWorker<>() {
            DefaultTableModel table;
            @Override
            protected Void doInBackground() {
                synchronized (programMap) {
                    ChannelSelector selector = new ChannelSelector();
                    table = selector.selectChannel(id, programMap);
                    currentChannel = selector.getCurrentChannel();
                    return null;
                }
            }
            @Override
            protected void done() {
                super.done();
                configureTable(table);
            }
        };
        sw.execute();
    }

    /**
     * Updates the currently selected channel by fetching the programmes anew and then updating the table model in the
     * GUI
     */
    public void updateChannel() {
        SwingWorker<Void, Void> sw = new SwingWorker<>() {
            DefaultTableModel table;
            @Override
            protected Void doInBackground() {
                synchronized (programMap) {
                    table = new UpdaterModel().channelWorker(programMap, currentChannel);
                    return null;
                }
            }
            @Override
            protected void done() {
                super.done();
                configureTable(table);
            }
        };
        sw.execute();
    }

    /**
     * Display JDialog with error-message in the gui
     * @param msg String with the error message
     */
    private void displayError(String msg) {
        SwingUtilities.invokeLater(()-> gui.configureErrorDialog(msg));
    }

    /**
     * Configures the JTable in the GUI with new programmes
     * !SHOULD NOT BE USED OUTSIDE A SWING-WORKER!
     * @param tableModel The updated table-model that should be displayed in the gui
     */
    private void configureTable(DefaultTableModel tableModel) {
        if (tableModel != null) {
            gui.configureTable(tableModel);
        } else {
            gui.configureErrorDialog("Fel när kanaler eller kanalernas innehåll skulle hämtas");
        }
    }

    /**
     * Configures the JDialog with program details
     * !SHOULD NOT BE USED OUTSIDE A SWING-WORKER!
     * @param img buffered image
     * @param t programme title
     * @param description programme description
     */
    private void configureDetails(BufferedImage img, String t, String description) {
        if (t != null && img != null && description != null) {
            gui.configureDetailDialog(img, t, description);
        } else {
            gui.configureErrorDialog("Fel när data om programmet skulle hämtas");
        }
    }
}
