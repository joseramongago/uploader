package es.panel.cest.Uploader.session_factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Assert;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Response;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.RestConnector;

/**
 *
 * @author panel
 */
public class AlmSession implements Session {

    private final RestConnector con;
    private final String authenticationPoint;
    private final Map<String, String> requestHeaders = new HashMap<String, String>();
    private final Xml xmlExtractor;
    /**
     *
     * @param sessionHost
     * @param sessionPort
     * @param sessionAuthenticationPoint
     * @param domain
     * @param project
     */
    public AlmSession(String sessionHost, String sessionPort,
            String sessionAuthenticationPoint, String domain, String project) {
        if (!sessionPort.isEmpty()) {
            sessionPort = ":" + sessionPort;
        }

        con = RestConnector.getInstance().init(
                new HashMap<String, String>(),
                "http://"
                + sessionHost
                + sessionPort
                + "/qcbin",
                domain,
                project);
        this.authenticationPoint = "http://" + sessionHost + sessionPort + "/qcbin/" + sessionAuthenticationPoint;
        xmlExtractor = new Xml();
    }

    /**
     * @param username
     * @param password
     * @throws Exception
     *
     * Logging in to our system is standard http login (basic authentication),
     * where one must store the returned cookies for further use.
     */
    public void login(String username, String password) throws Exception {               
        HttpResponse serverResponse;
       
        Encryptor e = new Encryptor();
        String passEncrypted = e.encrypt(password); 

        serverResponse = con.httpPost1(this.authenticationPoint, username, 
                passEncrypted , new HashMap<String, String>());
        // El log in en el REST de la empresa nos lleva a página en blanco que
        // devuelve error 500 pero es correcto.
        if (serverResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_INTERNAL_ERROR) {
            System.out.println("* Error: unexpected error when loggin in.");
        }
    }

    /**
     * @throws Exception close session on server and clean session cookies on
     * client
     */
    public void logout() throws Exception {
        Response serverResponse;

        serverResponse = con.httpGet(con.buildUrl("authentication-point/logout"), null, null);
        
        if (serverResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
            System.out.println("* Error: unexpected error when loggin out.");
        }
    }

    /**
     *
     * @return @throws Exception
     */
    public boolean isAuthenticated() throws Exception {
        String isAuthenticateUrl;
        Response response;

        isAuthenticateUrl = con.buildUrl("rest/is-authenticated");
        response = con.httpGet(isAuthenticateUrl, null, null);
        int responseCode = response.getStatusCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return true;
        } else {
            throw response.getFailure();
        }
    }

    public String getXMLTestCaseID(String testConfigurationID) throws Exception {
        String requirementsUrl;
        StringBuilder b;
        Response serverResponse;
        String ret;

        requirementsUrl = con.buildEntityCollectionUrl("test-instance");
        b = new StringBuilder();
        b.append("fields=id,status");
        b.append("&query={test-id[");
        b.append(testConfigurationID);
        b.append("];cycle-id[12404]}"); // ;cycle-id=[12404] por la duplicación de casos (se repite el CONFIGURATION ID)
        serverResponse = con.httpGet(requirementsUrl, b.toString(), requestHeaders);

        Assert.assertEquals(
                "Error: failed obtaining response for requirements collection "
                + requirementsUrl + ".",
                HttpURLConnection.HTTP_OK,
                serverResponse.getStatusCode());
        ret = new String(serverResponse.toString().getBytes(), "UTF-8");

        return ret;
    }
    
    public void updateTestCase(String testCaseID, boolean updateStatus, String pathsToFiles) throws Exception {
        String requirementsUrl = con.buildEntityCollectionUrl("test-instance") + "/" + testCaseID;
        
        if (pathsToFiles.contains("\""))
            pathsToFiles = pathsToFiles.replaceAll("\"", "");
        
        String[] pathsToFile = pathsToFiles.split(";");
        
        if (updateStatus) 
            updateStatus(testCaseID);
        for(String path : pathsToFile) {
            byte[] fileContent = readFile(path);

            Response response = con.httpPost(requirementsUrl + "/attachments", fileContent, requestHeaders);
            if (response.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                throw new Exception(response.toString());
            }
        }
    }
    
    private byte[] readFile(String pathToFile) throws IOException {
        String fileName = null;

        if (pathToFile.contains("\\"))// || pathToFile.contains("/"))
            fileName = pathToFile.substring(pathToFile.lastIndexOf("\\") + 1, pathToFile.length());
        else
            fileName = pathToFile;
        requestHeaders.put("Slug", fileName);
        requestHeaders.put("Content-Type", "application/octet-stream");

        File fileToAttach = new File(pathToFile);
        FileInputStream fileToAttachFis = new FileInputStream(fileToAttach);
        byte[] fileContent = new byte[(int)fileToAttach.length()];
        fileToAttachFis.read(fileContent);
        fileToAttachFis.close();
        
        return fileContent;
    }
    
    private void updateStatus(String testCaseID) throws Exception {
        String requirementsUrl = con.buildEntityCollectionUrl("test-instance");
        String updatedEntityXml = xmlExtractor.generateSingleFieldUpdateXml("status", "Passed");

        requirementsUrl += "/" + testCaseID;
        
        requestHeaders.put("Content-Type", "application/xml");
        requestHeaders.put("Accept", "application/xml");        
        
        Response putResponse = con.httpPut(requirementsUrl, updatedEntityXml.getBytes(), requestHeaders);
        
        if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception(putResponse.toString());
        }
    }
}
