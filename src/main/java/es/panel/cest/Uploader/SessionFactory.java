package es.panel.cest.Uploader;

/**
 *
 * @author panel
 */
public class SessionFactory {

    /**
     *
     */
    public SessionFactory() {
    }

    /**
     *
     * @param sessionType
     * @param sessionHost
     * @param sessionPort
     * @param sessionAuthenticationPoint
     * @param domain
     * @param project
     * @return
     */
    public Session createSession(String sessionType, String sessionHost,
            String sessionPort, String sessionAuthenticationPoint,
            String domain, String project) {
        if (sessionType.equalsIgnoreCase("AlmSession")) {
            return new AlmSession(sessionHost, sessionPort,
                    sessionAuthenticationPoint, domain, project);
        } else {
            return null;
        }
    }
}
