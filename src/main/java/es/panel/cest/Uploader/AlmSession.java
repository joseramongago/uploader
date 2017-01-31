package es.panel.cest.Uploader;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Assert;
import org.hp.qc.web.restapi.docexamples.docexamples.infrastructure.Base64Encoder;
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
        this.authenticationPoint = "http://" + sessionHost + "/qcbin/" + sessionAuthenticationPoint;
        this.requestHeaders.put("Accept", "application/xml");
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
        byte[] credBytes;
        String credEncodedString;
        Map<String, String> map;
        Response serverResponse;

        credBytes = (username + ":" + password).getBytes();
        credEncodedString = "Basic " + Base64Encoder.encode(credBytes);
        map = new HashMap<String, String>();
        map.put("Authorization", credEncodedString);
        serverResponse = con.httpGet(this.authenticationPoint, null, map);
        Assert.assertEquals(
                "Error: unexpected error when loggin in.",
                HttpURLConnection.HTTP_OK,
                serverResponse.getStatusCode());
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

    /**
     *
     * @param testSetId
     * @return
     * @throws Exception
     */
    public String getXMLTestCasesNameAndId(String testSetId) throws Exception {
        String requirementsUrl;
        StringBuilder b;
        Response serverResponse;
        String ret;

        requirementsUrl = con.buildEntityCollectionUrl("test-instance");
        b = new StringBuilder();
        b.append("fields=name,test-id");
        b.append("&query={contains-test-set.id[");
        b.append(testSetId);
        b.append("]}");
        b.append("&order-by={test-id}");
        b.append("&page-size=999999");
        serverResponse = con.httpGet(requirementsUrl, b.toString(), requestHeaders);
        Assert.assertEquals(
                "Error: failed obtaining response for requirements collection "
                + requirementsUrl + ".",
                HttpURLConnection.HTTP_OK,
                serverResponse.getStatusCode());
        ret = new String(serverResponse.toString().getBytes(), "UTF-8");

        return ret;
    }

    /**
     *
     * @param testCaseId
     * @return
     * @throws Exception
     */
    public String getXMLTestCaseDescription(String testCaseId) throws Exception {
        String requirementsUrl;
        StringBuilder b;
        Response serverResponse;
        String ret;

        requirementsUrl = con.buildEntityCollectionUrl("test");
        b = new StringBuilder();
        b.append("fields=description");
        b.append("&query={id[");
        b.append(testCaseId);
        b.append("]}");
        serverResponse = con.httpGet(requirementsUrl, b.toString(), requestHeaders);
        Assert.assertEquals(
                "Error: failed obtaining response for requirements collection "
                + requirementsUrl + ".",
                HttpURLConnection.HTTP_OK,
                serverResponse.getStatusCode());
        ret = new String(serverResponse.toString().getBytes(), "UTF-8");

        return ret;
    }

    /**
     *
     * @param testCaseId
     * @return
     * @throws Exception
     */
    public String getXMLDesignSteps(String testCaseId) throws Exception {
        String requirementsUrl;
        StringBuilder b;
        Response serverResponse;
        String ret;

        requirementsUrl = con.buildEntityCollectionUrl("design-step");
        b = new StringBuilder();
        b.append("fields=description,expected,link-test");
        b.append("&query={has-parts-test.id[");
        b.append(testCaseId);
        b.append("]}");
        b.append("&order-by={has-parts-test.name;has-parts-test.id;step-order}");
        serverResponse = con.httpGet(requirementsUrl, b.toString(), requestHeaders);
        Assert.assertEquals(
                "Error: failed obtaining response for requirements collection "
                + requirementsUrl + ".",
                HttpURLConnection.HTTP_OK,
                serverResponse.getStatusCode());
        ret = new String(serverResponse.toString().getBytes(), "UTF-8");

        return ret;
    }
}
