package es.panel.cest.Uploader;

import es.panel.cest.Uploader.session_factory.Session;
import es.panel.cest.Uploader.session_factory.Xml;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

public class Uploader {

    private final Session session;
    private BufferedReader br = null;
    private final Xml xml;

    public Uploader(Session session) {
        this.session = session;
        this.xml = new Xml();
    }

    public void update(String csvPath, String csvSplitBy) throws IOException, Exception {
        br = new BufferedReader(new FileReader(csvPath));
        String line;

        while ((line = br.readLine()) != null) {
            String[] testCase = line.split(csvSplitBy,2);

            if (StringUtils.containsIgnoreCase(testCase[0], "ID") || testCase[0].isEmpty())
                continue;
            String xmlCase = session.getXMLTestCaseID(testCase[0]);

            Node testInstanceID = xml.getNodeList(xmlCase,"//Field[@Name='id']//Value").item(0);
            Node testInstanceStatus = xml.getNodeList(xmlCase, "//Field[@Name='status']//Value").item(0);
            
            boolean updateStatus = false;
            if (!testInstanceStatus.getTextContent().equals("Passed"))
                updateStatus = true;
            session.updateTestCase(testInstanceID.getTextContent(), updateStatus, testCase[1]);
            System.out.println(" * Fichero adjuntado al caso de prueba con ID: " + testCase[0]);
        }
    }
}
