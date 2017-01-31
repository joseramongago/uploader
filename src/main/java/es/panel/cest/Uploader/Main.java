package es.panel.cest.Uploader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.panel.template_generator.Generator;

import es.panel.cest.Uploader.Session;
import es.panel.cest.Uploader.SessionFactory;

/**
 * Hello world!
 *
 */
public class Main {
	
	private static final Properties PROPERTIES = new Properties();
	
    public static void main( String[] args ) {
    	try {
	    	SessionFactory sessionFactory;
	        Session session;
        
			PROPERTIES.load(new FileInputStream("config.properties"));
	
	        String sessionType = PROPERTIES.getProperty("session_type");
	        String sessionHost = PROPERTIES.getProperty("session_host");
	        String sessionPort = PROPERTIES.getProperty("session_port");
	        String sessionAuthenticationPoint = PROPERTIES.getProperty("session_authentication_point");
	        
	        String domain = PROPERTIES.getProperty("domain");
	        String project = PROPERTIES.getProperty("project");
	        String username = PROPERTIES.getProperty("username");
	        String password = PROPERTIES.getProperty("password");
	        
	        String excelPath = PROPERTIES.getProperty("path");
	        
	        sessionFactory = new SessionFactory();
	        session = sessionFactory.createSession(sessionType,sessionHost,sessionPort,sessionAuthenticationPoint,domain,
	        		project);
	        session.login(username, password);
	        
	        Uploader uploader = new Uploader(session);
	        uploader.update(excelPath);
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
