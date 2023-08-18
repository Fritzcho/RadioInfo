package models;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Parser handles all the actions that requires fetching data from the API - such as channels and programmes -
 * and holds all the necessary connection-strings.
 *
 * @author Alireza Ramezani, id19ari
 * @version 2.0
 */
public class RadioParser {
    /**
     * Make a GET request to the SR API and return the response code
     * @param url The url for the connection
     * @return int response code
     */
    private static int fetch(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseCode();
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Fetch XML data from API and parse it
     * @param url URl to fetch from
     * @return Document consisting of parsed XML data
     */
    public static Document parse(URL url) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(url.openStream());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return null;
        }
    }

    /**
     * Read all the channel tags from the current page in the parsed document
     * @param channels ArrayList in which the channels are added.
     * @param doc parsed document
     */
    private void readChannels(ArrayList<Channel> channels, Document doc) {
        NodeList nL = doc.getElementsByTagName("channel");
        for (int i = 0; i < nL.getLength(); i++) {
            Element chE = (Element) nL.item(i);
            channels.add(new Channel(
                    chE.getAttribute("id"),
                    chE.getAttribute("name")
            ));
        }
    }

    /**
     * Read programmes from the parsed document. Convert the timezones, if the programmes start 6 hours prior or 12
     * hours later than the current time, build a new programme and add it to the arraylist
     * @param doc Parsed document
     */
    private ArrayList<Programme> readProgrammes(Document doc) {
        ArrayList<Programme> programmes = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("scheduledepisode");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element pE = (Element) nodeList.item(i);

            ZonedDateTime startTime =
                    Instant.parse(pE.getElementsByTagName("starttimeutc").item(0).getTextContent())
                            .atZone(ZoneId.systemDefault());
            ZonedDateTime endTime =
                    Instant.parse(pE.getElementsByTagName("endtimeutc").item(0).getTextContent())
                            .atZone(ZoneId.systemDefault());

            ZonedDateTime earliestEndTime = ZonedDateTime.now().minusHours(6);
            ZonedDateTime latestStartTime = ZonedDateTime.now().plusHours(12);

            //System.out.println("StartTime: "+startTime+"|EndTime: "+endTime+"|EarliestStartTime: "+ earliestStartTime+"|LatestStartTime: "+latestStartTime+"|LocalTime: "+LocalDateTime.now());
            //System.out.println("Currenttime:"+LocalDateTime.now()+"|Endtime:"+endTime+"|Diff"+ChronoUnit.HOURS.between(LocalDateTime.now(), endTime)+"|Title:"+pE.getElementsByTagName("title").item(0).getTextContent());

            if (endTime.isAfter(earliestEndTime) && startTime.isBefore(latestStartTime)) {
                Programme.ProgrammeBuilder progBuilder = new Programme.ProgrammeBuilder(
                        pE.getElementsByTagName("title").item(0).getTextContent(),
                        startTime.toLocalTime().toString(),
                        endTime.toLocalTime().toString()
                );

                if (pE.getElementsByTagName("imageurl").item(0)!=null)
                    progBuilder = progBuilder.setImagePath(pE.getElementsByTagName("imageurl").
                                    item(0).getTextContent());

                if (pE.getElementsByTagName("description").item(0)!=null)
                    progBuilder = progBuilder.setDescription(pE.getElementsByTagName("description").
                            item(0).getTextContent());

                Programme prog = progBuilder.buildProgramme();
                programmes.add(prog);
            }
        }
        return programmes;
    }

    /**
     * Get all the channels available in the SR API.
     * Fetch the response code, if the response code is 200, proceed to fetch data and parse XML page into W3C
     * Document. Parse the total number of pages in the document and proceed to get all elements with the channel
     * tag into a NodeList. Then loop through all nodes and create new models.Channel objects to be saved in the channels set.
     * Replicate the process for each page in the API.
     * @return List of channels
     */
    public ArrayList<Channel> fetchChannels() {
        ArrayList<Channel> channels = new ArrayList<>();
        try {
                URL url = new URL("http://api.sr.se/api/v2/channels/?pagination=false");
                int responsecode = fetch(url);

                if (responsecode != 200) {
                    return null;
                } else {
                    Document doc = parse(url);
                    assert doc != null;
                    readChannels(channels, doc);
                }
        } catch (IOException e) {
            return null;
        }
        return channels;
    }

    /**
     * Method updateChannel responsible for updating, or fetching, all programs win a channel based on the specified
     * channel ID. Parse all elements with the scheduledepisode tag into a NodeList. For every episode with a time
     * difference higher than -6 or lower than 12 to the current localtime, create a new Programme model object and save in
     * programMap and the programmes HashSet.
     * @param id ID of the channel to fetch from
     * @param programMap HashMap of all the fetched programmes.
     * @return HashSet consisting of all the fetched programmes.
     */
    public ArrayList<Programme> updateChannel(String id, ConcurrentHashMap<String, ArrayList<Programme>> programMap){
        ArrayList<Programme> programmes = new ArrayList<>();
        if (!id.isEmpty()) {
            LocalDate today = LocalDate.now();
            try {
                URL url = new URL("http://api.sr.se/v2/scheduledepisodes?channelid=" + id + "&pagination=false"
                        + "&fromDate="
                        + (LocalTime.now().isBefore(LocalTime.parse("07:00")) ? today.minusDays(1) : today)
                        + "&toDate="
                        + (LocalTime.now().isBefore(LocalTime.parse("07:00")) ? today : today.plusDays(1))
                );
                int responsecode = fetch(url);
                if (responsecode != 200) {
                    return null;
                } else {
                    Document doc = parse(url);
                    assert doc != null;
                    programmes = readProgrammes(doc);
                    programMap.put(id, programmes);
                }
            } catch (IOException ex) {
                return null;
            }
        }
        return programmes;
    }
}
