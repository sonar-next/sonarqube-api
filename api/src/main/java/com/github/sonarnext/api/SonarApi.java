package com.github.sonarnext.api;

import com.github.sonarnext.api.utils.MaskingLoggingFilter;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SonarApi implements AutoCloseable {


    private final static Logger LOGGER = Logger.getLogger(SonarApi.class.getName());

    public static final int DEFAULT_PER_PAGE = 20;


    // Used to keep track of SonarApiExceptions on calls that return Optional<?>
    private static final Map<Integer, SonarApiException> optionalExceptionMap =
        Collections.synchronizedMap(new WeakHashMap<>());

    SonarApiClient apiClient;
    private String gitLabServerUrl;
    private Map<String, Object> clientConfigProperties;
    private int defaultPerPage = DEFAULT_PER_PAGE;

    CeApi ceApi;

    /**
     * @return Logger
     */
    public static Logger getLogger() {
        return (LOGGER);
    }


    /**
     * Constructs a GitLabApi instance set up to interact with the GitLab server using GitLab API version 4.
     *
     * @param hostUrl the URL of the GitLab server
     * @param secretToken use this token to validate received payloads
     * @param clientConfigProperties config
     */
    public SonarApi(String hostUrl, String secretToken, Map<String, Object> clientConfigProperties) {
        this.gitLabServerUrl = hostUrl;
        this.clientConfigProperties = clientConfigProperties;
        apiClient = new SonarApiClient(hostUrl, secretToken, clientConfigProperties);
    }


    /**
     * Create a new GitLabApi instance that is logically a duplicate of this instance, with the exception of sudo state.
     *
     * @return a new GitLabApi instance that is logically a duplicate of this instance, with the exception of sudo state.
     */
    public final SonarApi duplicate() {

        SonarApi gitLabApi = new SonarApi(gitLabServerUrl,
            getAuthToken(), clientConfigProperties);

        if (getIgnoreCertificateErrors()) {
            gitLabApi.setIgnoreCertificateErrors(true);
        }

        gitLabApi.defaultPerPage = this.defaultPerPage;
        return (gitLabApi);
    }

    /**
     * Close the underlying {@link javax.ws.rs.client.Client} and its associated resources.
     */
    @Override
    public void close() {
        if (apiClient != null) {
            apiClient.close();
        }
    }

    public CeApi getCeApi() {
        synchronized (this) {
            if (ceApi == null) {
                ceApi = new CeApi(this);
            }
            return ceApi;
        }
    }

    /**
     * Sets the per request connect and read timeout.
     *
     * @param connectTimeout the per request connect timeout in milliseconds, can be null to use default
     * @param readTimeout the per request read timeout in milliseconds, can be null to use default
     */
    public void setRequestTimeout(Integer connectTimeout, Integer readTimeout) {
        apiClient.setRequestTimeout(connectTimeout, readTimeout);
    }

    /**
     * Fluent method that sets the per request connect and read timeout.
     *
     * @param connectTimeout the per request connect timeout in milliseconds, can be null to use default
     * @param readTimeout the per request read timeout in milliseconds, can be null to use default
     * @return this SonarApi instance
     */
    public SonarApi withRequestTimeout(Integer connectTimeout, Integer readTimeout) {
        apiClient.setRequestTimeout(connectTimeout, readTimeout);
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the SonarApi shared Logger instance and Level.FINE as the level.
     *
     * @return this SonarApi instance
     */
    public SonarApi withRequestResponseLogging() {
        enableRequestResponseLogging();
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the SonarApi shared Logger instance.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @return this SonarApi instance
     */
    public SonarApi withRequestResponseLogging(Level level) {
        enableRequestResponseLogging(level);
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API.
     *
     * @param logger the Logger instance to log to
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @return this SonarApi instance
     */
    public SonarApi withRequestResponseLogging(Logger logger, Level level) {
        enableRequestResponseLogging(logger, level);
        return (this);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the SonarApi shared Logger instance and Level.FINE as the level.
     */
    public void enableRequestResponseLogging() {
        enableRequestResponseLogging(LOGGER, Level.FINE);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API
     * using the SonarApi shared Logger instance. Logging will NOT include entity logging and
     * will mask PRIVATE-TOKEN and Authorization headers.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     */
    public void enableRequestResponseLogging(Level level) {
        enableRequestResponseLogging(LOGGER, level, 0);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger. Logging will NOT include entity logging and will mask PRIVATE-TOKEN
     * and Authorization headers..
     *
     * @param logger the Logger instance to log to
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     */
    public void enableRequestResponseLogging(Logger logger, Level level) {
        enableRequestResponseLogging(logger, level, 0);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * SonarApi shared Logger instance. Logging will mask PRIVATE-TOKEN and Authorization headers.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     * is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     * the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     */
    public void enableRequestResponseLogging(Level level, int maxEntitySize) {
        enableRequestResponseLogging(LOGGER, level, maxEntitySize);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger. Logging will mask PRIVATE-TOKEN and Authorization headers.
     *
     * @param logger the Logger instance to log to
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     * is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     * the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     */
    public void enableRequestResponseLogging(Logger logger, Level level, int maxEntitySize) {
        enableRequestResponseLogging(logger, level, maxEntitySize, MaskingLoggingFilter.DEFAULT_MASKED_HEADER_NAMES);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * SonarApi shared Logger instance.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Level level, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(LOGGER, level, 0, maskedHeaderNames);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger.
     *
     * @param logger the Logger instance to log to
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Logger logger, Level level, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(logger, level, 0, maskedHeaderNames);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * SonarApi shared Logger instance.
     *
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     * is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     * the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Level level, int maxEntitySize, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(LOGGER, level, maxEntitySize, maskedHeaderNames);
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API using the
     * specified logger.
     *
     * @param logger the Logger instance to log to
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     * @param maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     * is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     * the end of the log entry. If maxEntitySize is &lt;= 0, entity logging will be disabled
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    public void enableRequestResponseLogging(Logger logger, Level level, int maxEntitySize, List<String> maskedHeaderNames) {
        apiClient.enableRequestResponseLogging(logger, level, maxEntitySize, maskedHeaderNames);
    }

    /**
     * Get the auth token being used by this client.
     *
     * @return the auth token being used by this client
     */
    public String getAuthToken() {
        return (apiClient.getAuthToken());
    }


    /**
     * Get the URL to the GitLab server.
     *
     * @return the URL to the GitLab server
     */
    public String getGitLabServerUrl() {
        return (gitLabServerUrl);
    }

    /**
     * Get the default number per page for calls that return multiple items.
     *
     * @return the default number per page for calls that return multiple item
     */
    public int getDefaultPerPage() {
        return (defaultPerPage);
    }

    /**
     * Set the default number per page for calls that return multiple items.
     *
     * @param defaultPerPage the new default number per page for calls that return multiple item
     */
    public void setDefaultPerPage(int defaultPerPage) {
        this.defaultPerPage = defaultPerPage;
    }

    /**
     * Return the SonarApiClient associated with this instance. This is used by all the sub API classes
     * to communicate with the GitLab API.
     *
     * @return the SonarApiClient associated with this instance
     */
    SonarApiClient getApiClient() {
        return (apiClient);
    }

    /**
     * Returns true if the API is setup to ignore SSL certificate errors, otherwise returns false.
     *
     * @return true if the API is setup to ignore SSL certificate errors, otherwise returns false
     */
    public boolean getIgnoreCertificateErrors() {
        return (apiClient.getIgnoreCertificateErrors());
    }

    /**
     * Sets up the Jersey system ignore SSL certificate errors or not.
     *
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     */
    public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {
        apiClient.setIgnoreCertificateErrors(ignoreCertificateErrors);
    }


    /**
     * Create and return an Optional instance associated with a SonarApiException.
     *
     * @param <T> the type of the Optional instance
     * @param glae the SonarApiException that was the result of a call to the GitLab API
     * @return the created Optional instance
     */
    protected static final <T> Optional<T> createOptionalFromException(SonarApiException glae) {
        Optional<T> optional = Optional.empty();
        optionalExceptionMap.put(System.identityHashCode(optional),  glae);
        return (optional);
    }

    /**
     * Get the exception associated with the provided Optional instance, or null if no exception is
     * associated with the Optional instance.
     *
     * @param optional the Optional instance to get the exception for
     * @return the exception associated with the provided Optional instance, or null if no exception is
     * associated with the Optional instance
     */
    public static final SonarApiException getOptionalException(Optional<?> optional) {
        return (optionalExceptionMap.get(System.identityHashCode(optional)));
    }

    /**
     * Return the Optional instances contained value, if present, otherwise throw the exception that is
     * associated with the Optional instance.
     *
     * @param <T> the type for the Optional parameter
     * @param optional the Optional instance to get the value for
     * @return the value of the Optional instance if no exception is associated with it
     * @throws SonarApiException if there was an exception associated with the Optional instance
     */
    public static final <T> T orElseThrow(Optional<T> optional) throws SonarApiException {

        SonarApiException glea = getOptionalException(optional);
        if (glea != null) {
            throw (glea);
        }

        return (optional.get());
    }
}
