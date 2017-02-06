package es.panel.cest.Uploader;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpResponse;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Assert;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Constants;
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
        //this.requestHeaders.put("Accept", "application/xml");
    }

    /**
     * @param username
     * @param password
     * @throws Exception
     *
     * Logging in to our system is standard http login (basic authentication),
     * where one must store the returned cookies for further use.
     */
        /**
         * byte[] credBytes;
         * String credEncodedString;
         * credBytes = (username + ":" + password).getBytes("UTF-8");
         * credEncodedString = "Basic " + Base64.getEncoder().encodeToString(credBytes);
         */
    
    public void login(String username, String password) throws Exception {               
        Map<String, String> map;
        HttpResponse serverResponse;
       
        Encryptor e = new Encryptor();
        String passEncrypted = e.encrypt(password);
       // String data = "j_username=" + username + "&j_password=" + passEncrypted;

        map = new HashMap<String, String>();

        
        serverResponse = con.httpPost1(this.authenticationPoint, username, passEncrypted , map);
        Assert.assertEquals(
                "Error: unexpected error when loggin in.",
                500,
                serverResponse.getStatusLine().getStatusCode());
    }

    /**
     * @throws Exception close session on server and clean session cookies on
     * client
     */
    public void logout() throws Exception {
        Response serverResponse;

        serverResponse = con.httpGet(con.buildUrl("authentication-point/logout"), null, null);
        Assert.assertEquals(
                "Error: unexpected error when loggin out.",
                HttpURLConnection.HTTP_OK,
                serverResponse.getStatusCode());
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
        String requirementsUrl = requirementsUrl = con.buildEntityCollectionUrl("test-instance") + "/" + testCaseID;
        String[] pathsToFile = pathsToFiles.split(";");
        String fileName;
        
        if (updateStatus) 
            updateStatus(requirementsUrl);
        for(String path : pathsToFile) {
            if (path.contains("\""))
                path = path.replaceAll("\"", "");

            if (path.contains("\\"))// || pathToFile.contains("/"))
                fileName = path.substring(path.lastIndexOf("\\") + 1, path.length());
            else
                fileName = path;
            requestHeaders.put("Slug", fileName);
            requestHeaders.put("Content-Type", "application/octet-stream");

           
            File fileToAttach = new File(path);
            FileInputStream fileToAttachFis = new FileInputStream(fileToAttach);
            byte[] fileContent = new byte[(int)fileToAttach.length()];
            fileToAttachFis.read(fileContent);
            fileToAttachFis.close();

            Response response = con.httpPost(requirementsUrl + "/attachments", fileContent, requestHeaders);
            if (response.getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                throw new Exception(response.toString());
            }
        }
    }
    
    private void updateStatus(String requirementsUrl) throws Exception {
        String updatedEntityXml = generateSingleFieldUpdateXml("status", "Passed");

        requestHeaders.put("Content-Type", "application/xml");
        requestHeaders.put("Accept", "application/xml");        
        
        Response putResponse = con.httpPut(requirementsUrl, updatedEntityXml.getBytes(), requestHeaders);
        
        if (putResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
            throw new Exception(putResponse.toString());
        }
    }
    
    private static String generateSingleFieldUpdateXml(String field, String value) {
        return "<Entity Type=\"test-instance\"><Fields>"
                + Constants.generateFieldXml(field, value)
                + "</Fields></Entity>";
    }
}
