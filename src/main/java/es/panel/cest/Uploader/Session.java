package es.panel.cest.Uploader;


/**
 *
 * @author panel
 */
public interface Session {

    /**
     *
     * @param username
     * @param password
     * @throws Exception
     */
    public void login(String username, String password)
            throws Exception;

    /**
     *
     * @throws Exception
     */
    public void logout()
            throws Exception;

    /**
     *
     * @return @throws Exception
     */
    public boolean isAuthenticated()
            throws Exception;

    public String getXMLTestCaseID(String testConfigurationID)
            throws Exception;
    
    public void updateTestCase(String testCaseID, boolean updateStatus, String pathToFile) throws Exception;
}
