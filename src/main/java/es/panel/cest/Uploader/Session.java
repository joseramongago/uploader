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
     * @return
     * @throws Exception 
     */
    public boolean isAuthenticated()
            throws Exception;

    /**
     * 
     * @param testSetId
     * @return
     * @throws Exception 
     */
    public String getXMLTestCasesNameAndId(String testSetId)
            throws Exception;

    /**
     * 
     * @param testCaseId
     * @return
     * @throws Exception 
     */
    public String getXMLTestCaseDescription(String testCaseId)
            throws Exception;

    /**
     * 
     * @param testCaseId
     * @return
     * @throws Exception 
     */
    public String getXMLDesignSteps(String testCaseId)
            throws Exception;
}
