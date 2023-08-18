import models.Channel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Class GUI builds the program graphical user interface and contains all the necessary components for the GUI to work
 * properly.
 *
 * @author Alireza Ramezani, id19ari
 * @version 2.0
 */
public class View {
    private final JFrame frame;
    private JTable jt;
    private DefaultTableModel tableModel;
    private RadioInfoController controller;

    /**
     * Create the GUI and configure the frame and menubar, and also the table headings.
     * @param listModel listModel to instansiate a JList
     */
    public View(DefaultListModel<Channel> listModel, RadioInfoController controller) {
        this.controller = controller;
        frame = new JFrame("Radio Info");
        String[] header = {"Beskrivning", "Bild", "Program", "Start", "Slut"};
        tableModel = new DefaultTableModel(header, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        configureMenu();
        configureFrame(listModel);
    }

    public void configureErrorDialog(String errorMsg) {
        JDialog dialog = new JDialog(frame, "Fel");
        JLabel error = new JLabel(errorMsg);
        JPanel panel = new JPanel();

        panel.add(error);
        dialog.add(panel);
        dialog.setSize(new Dimension(500, 100));
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    /**
     * Configure JDialog popup to display extra information about a programme to users
     * @param img Buffered Image to display
     * @param t String containing the programme title
     * @param description String containing the programme description
     */
    public void configureDetailDialog(BufferedImage img, String t, String description) {
        JDialog dialog = new JDialog(frame, "Mer info");
        JLabel image = new JLabel(new ImageIcon(img));
        JLabel title = new JLabel(t);
        JTextArea desc = new JTextArea(description);
        JPanel panel = new JPanel();
        JPanel innerPanel = new JPanel();
        JScrollPane innerPScroll = new JScrollPane(innerPanel);

        title.setFont(new Font("Verdana", Font.PLAIN, 25));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setSize(new Dimension(480, 50));
        desc.setEditable(false);
        desc.setLineWrap(true);
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setPreferredSize(new Dimension(480, 250));
        innerPScroll.setSize(500, 150);

        panel.add(image, BorderLayout.PAGE_START);
        innerPanel.add(title);
        innerPanel.add(desc);
        panel.add(innerPScroll, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.setSize(new Dimension(500, 650));
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    /**
     * Configure the list containing all channels
     * @param listModel listModel containing all channels fetched
     * @return JScrollpane containing the channel list
     */
    private JScrollPane configureList(DefaultListModel<Channel> listModel) {
        JList list = new JList(listModel);
        list.getSelectionModel().addListSelectionListener(arg0 -> {
            if (!arg0.getValueIsAdjusting()) {
                Channel channel = (Channel)list.getSelectedValue();
                controller.selectChannel(channel.id);
            }
        });
        return new JScrollPane(list);
    }

    /**
     * Configure the table containing all programmes in a selected channel
     * @param tableModel List of programmes in the current channel
     */
    public void configureTable(DefaultTableModel tableModel) {
        this.tableModel.setRowCount(0);
        for (int row = 1; row<tableModel.getRowCount();row++) {
            String[] data = {
                    tableModel.getValueAt(row, 0).toString(),
                    tableModel.getValueAt(row, 1).toString(),
                    tableModel.getValueAt(row, 2).toString(),
                    tableModel.getValueAt(row, 3).toString(),
                    tableModel.getValueAt(row, 4).toString(),
            };
            this.tableModel.addRow(data);
        }
        this.tableModel.fireTableDataChanged();
    }

    /**
     * Configure the JFrame
     * @param listModel listModel of channels
     */
    private void configureFrame(DefaultListModel<Channel> listModel) {
        jt = new JTable(tableModel);
        jt.setAutoCreateRowSorter(true);
        jt.setBounds(30,40,200,300);
        jt.removeColumn(jt.getColumnModel().getColumn(0));
        jt.removeColumn(jt.getColumnModel().getColumn(0));
        JScrollPane sp=new JScrollPane(jt);
        getMoreInfo();

        frame.add(sp, BorderLayout.CENTER);
        frame.add(configureList(listModel), BorderLayout.LINE_START);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000,700);
        frame.setVisible(true);
    }

    /**
     * Get more info about specific programmme
     */
    private void getMoreInfo() {
        jt.getSelectionModel().addListSelectionListener(event -> {
            if(!event.getValueIsAdjusting()) {
                if (jt.getSelectedRow() > -1) {
                    controller.getProgrammeInfo(
                            tableModel.getValueAt(jt.getSelectedRow(), 0).toString(),
                            tableModel.getValueAt(jt.getSelectedRow(), 1).toString(),
                            tableModel.getValueAt(jt.getSelectedRow(), 2).toString(),
                            jt.getSelectedRow()
                    );
                }
            }
        });
    }

    /**
     * Configure the JMenuBar to be added in the GUI
     */
    private void configureMenu() {
        JMenu menu = new JMenu("Meny");
        JMenuBar menuBar = new JMenuBar();
        JMenuItem refresh = new JMenuItem("Uppdatera nuvarande kanal");
        menu.add(refresh);
        refresh.addActionListener(new ChannelUpdater());
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
    }

    /**
     * UpdateListener listens to the update channel option in the application menu. Uppon selection, it updates the
     * channel currently selected in the view with newly fetched data.
     */
    public class ChannelUpdater implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            controller.updateChannel();
        }
    }
}
