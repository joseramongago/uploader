package es.panel.cest.Uploader;

import es.panel.cest.Uploader.session_factory.Session;
import es.panel.cest.Uploader.session_factory.SessionFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class Main {

    private static final Properties PROPERTIES = new Properties();

    public static void main(String[] args) {
        try {
            SessionFactory sessionFactory;
            Session session;

            PROPERTIES.load(new FileInputStream("D:\\workspaces\\Uploader\\resources\\config.properties"));

            String sessionType = PROPERTIES.getProperty("session_type");
            String sessionHost = PROPERTIES.getProperty("session_host");
            String sessionPort = PROPERTIES.getProperty("session_port");
            String sessionAuthenticationPoint = PROPERTIES.getProperty("session_authentication_point");

            String domain = PROPERTIES.getProperty("domain");
            String project = PROPERTIES.getProperty("project");
            String username = PROPERTIES.getProperty("username");
            String password = PROPERTIES.getProperty("password");

            String csvPath = PROPERTIES.getProperty("path");
            String csvSplitBy = (PROPERTIES.getProperty("csvSplitBy").isEmpty()) ? ";" : PROPERTIES.getProperty("csvSplitBy");
            
            sessionFactory = new SessionFactory();
            session = sessionFactory.createSession(sessionType, sessionHost, sessionPort, sessionAuthenticationPoint, domain,
            project);
            
            session.login(username, password);
            Uploader uploader = new Uploader(session);
            uploader.update(csvPath, csvSplitBy);
            session.logout();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
