package com.github.sonarnext.api;

import com.github.sonarnext.api.utils.JacksonJson;
import com.github.sonarnext.api.utils.MaskingLoggingFilter;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.media.multipart.Boundary;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.net.ssl.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SonarApiClient implements AutoCloseable {

    protected static final String SUDO_HEADER           = "Sudo";
    protected static final String AUTHORIZATION_HEADER  = "Authorization";

    private final ClientConfig clientConfig;
    private Client apiClient;
    private final String baseUrl;
    private String hostUrl;
    private final String authToken;
    private boolean ignoreCertificateErrors;
    private SSLContext openSslContext;
    private HostnameVerifier openHostnameVerifier;
    private Integer connectTimeout;
    private Integer readTimeout;


    public SonarApiClient(String hostUrl, String privateToken) {
        this(hostUrl, privateToken, null);
    }

    /**
     * Construct an instance to communicate with a GitLab API server using the specified GitLab API version,
     * server URL and private token.
     *
     * @param hostUrl the URL to the GitLab API server
     * @param authToken the private token to authenticate with
     * @param clientConfigProperties the properties given to Jersey's clientconfig
     */
    public SonarApiClient(String hostUrl, String authToken, Map<String, Object> clientConfigProperties) {

        // Remove the trailing "/" from the hostUrl if present
        this.hostUrl = (hostUrl.endsWith("/") ? hostUrl.replaceAll("/$", "") : hostUrl);
        this.baseUrl = this.hostUrl;

        this.authToken = authToken;

        clientConfig = new ClientConfig();
        if (clientConfigProperties != null) {

            if (clientConfigProperties.containsKey(ClientProperties.PROXY_URI)) {
                clientConfig.connectorProvider(new ApacheConnectorProvider());
            }

            for (Map.Entry<String, Object> propertyEntry : clientConfigProperties.entrySet()) {
                clientConfig.property(propertyEntry.getKey(), propertyEntry.getValue());
            }
        }

        // Disable auto-discovery of feature and services lookup, this will force Jersey
        clientConfig.property(ClientProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        clientConfig.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);

        clientConfig.register(JacksonJson.class);
        clientConfig.register(MultiPartFeature.class);
    }

    /**
     * Close the underlying {@link Client} and its associated resources.
     */
    @Override
    public void close() {
        if (apiClient != null) {
            apiClient.close();
        }
    }

    /**
     * Enable the logging of the requests to and the responses from the GitLab server API.
     *
     * @param logger the Logger instance to log to
     * @param level the logging level (SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST)
     *  maxEntitySize maximum number of entity bytes to be logged.  When logging if the maxEntitySize
     * is reached, the entity logging  will be truncated at maxEntitySize and "...more..." will be added at
     * the end of the log entry. If maxEntitySize is <= 0, entity logging will be disabled
     * @param maskedHeaderNames a list of header names that should have the values masked
     */
    void enableRequestResponseLogging(Logger logger, Level level, int maxEntityLength, List<String> maskedHeaderNames) {

        MaskingLoggingFilter loggingFilter = new MaskingLoggingFilter(logger, level, maxEntityLength, maskedHeaderNames);
        clientConfig.register(loggingFilter);

        // Recreate the Client instance if already created.
        if (apiClient != null) {
            createApiClient();
        }
    }

    /**
     * Sets the per request connect and read timeout.
     *
     * @param connectTimeout the per request connect timeout in milliseconds, can be null to use default
     * @param readTimeout the per request read timeout in milliseconds, can be null to use default
     */
    void setRequestTimeout(Integer connectTimeout, Integer readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Get the auth token being used by this client.
     *
     * @return the auth token being used by this client
     */
    String getAuthToken() {
        return (authToken);
    }

    /**
     * Construct a REST URL with the specified path arguments.
     *
     * @param pathArgs variable list of arguments used to build the URI
     * @return a REST URL with the specified path arguments
     * @throws IOException if an error occurs while constructing the URL
     */
    protected URL getApiUrl(Object... pathArgs) throws IOException {
        String url = appendPathArgs(this.hostUrl, pathArgs);
        return (new URL(url));
    }

    /**
     * Construct a REST URL with the specified path arguments using
     * Gitlab base url.
     *
     * @param pathArgs variable list of arguments used to build the URI
     * @return a REST URL with the specified path arguments
     * @throws IOException if an error occurs while constructing the URL
     */
    protected URL getUrlWithBase(Object... pathArgs) throws IOException {
        String url = appendPathArgs(this.baseUrl, pathArgs);
        return (new URL(url));
    }

    private String appendPathArgs(String url, Object... pathArgs) {
        StringBuilder urlBuilder = new StringBuilder(url);
        for (Object pathArg : pathArgs) {
            if (pathArg != null) {
                urlBuilder.append("/");
                urlBuilder.append(pathArg.toString());
            }
        }
        return urlBuilder.toString();
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response get(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (get(queryParams, url));
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response get(MultivaluedMap<String, String> queryParams, URL url) {
        return (invocation(url, queryParams).get());
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param accepts if non-empty will set the Accepts header to this value
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response getWithAccepts(MultivaluedMap<String, String> queryParams, String accepts, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (getWithAccepts(queryParams, url, accepts));
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @param accepts if non-empty will set the Accepts header to this value
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response getWithAccepts(MultivaluedMap<String, String> queryParams, URL url, String accepts) {
        return (invocation(url, queryParams, accepts).get());
    }

    /**
     * Perform an HTTP HEAD call with the specified query parameters and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response head(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (head(queryParams, url));
    }

    /**
     * Perform an HTTP HEAD call with the specified query parameters and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response head(MultivaluedMap<String, String> queryParams, URL url) {
        return (invocation(url, queryParams).head());
    }

    /**
     * Perform an HTTP POST call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(Form formData, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return post(formData, url);
    }

    /**
     * Perform an HTTP POST call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a Response instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return post(queryParams, url);
    }

    /**
     * Perform an HTTP POST call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response post(Form formData, URL url) {
        if (formData instanceof SonarApiForm) {
            return (invocation(url, null).post(Entity.entity(formData.asMap(), MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
        } else if (formData != null) {
            return (invocation(url, null).post(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
        } else {
            return (invocation(url, null).post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
        }
    }

    /**
     * Perform an HTTP POST call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parametersformData the Form containing the name/value pairs
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response post(MultivaluedMap<String, String> queryParams, URL url) {
        return (invocation(url, queryParams).post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
    }

    /**
     * Perform an HTTP POST call with the specified payload object and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param payload the object instance that will be serialized to JSON and used as the POST data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(Object payload, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        Entity<?> entity = Entity.entity(payload, MediaType.APPLICATION_JSON);
        return (invocation(url, null).post(entity));
    }

    /**
     * Perform an HTTP POST call with the specified StreamingOutput, MediaType, and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param stream the StreamingOutput instance that contains the POST data
     * @param mediaType the content-type of the POST data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response post(StreamingOutput stream, String mediaType, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (invocation(url, null).post(Entity.entity(stream, mediaType)));
    }

    /**
     * Perform a file upload using the specified media type, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param mediaTypeString the content-type of the uploaded file, if null will be determined from fileToUpload
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response upload(String name, File fileToUpload, String mediaTypeString, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (upload(name, fileToUpload, mediaTypeString, null, url));
    }

    /**
     * Perform a file upload using the specified media type, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param mediaTypeString the content-type of the uploaded file, if null will be determined from fileToUpload
     * @param formData the Form containing the name/value pairs
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response upload(String name, File fileToUpload, String mediaTypeString, Form formData, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (upload(name, fileToUpload, mediaTypeString, formData, url));
    }

    /**
     * Perform a file upload using multipart/form-data, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param mediaTypeString the content-type of the uploaded file, if null will be determined from fileToUpload
     * @param formData the Form containing the name/value pairs
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response upload(String name, File fileToUpload, String mediaTypeString, Form formData, URL url) throws IOException {

        MediaType mediaType = (mediaTypeString != null ? MediaType.valueOf(mediaTypeString) : null);
        try (FormDataMultiPart multiPart = new FormDataMultiPart()) {

            if (formData != null) {
                MultivaluedMap<String, String> formParams = formData.asMap();
                formParams.forEach((key, values) -> {
                    if (values != null) {
                        values.forEach(value -> multiPart.field(key, value));
                    }
                });
            }

            FileDataBodyPart filePart = mediaType != null ?
                new FileDataBodyPart(name, fileToUpload, mediaType) :
                new FileDataBodyPart(name, fileToUpload);
            multiPart.bodyPart(filePart);
            final Entity<?> entity = Entity.entity(multiPart, Boundary.addBoundary(multiPart.getMediaType()));
            return (invocation(url, null).post(entity));
        }
    }

    /**
     * Perform a file upload using multipart/form-data using the HTTP PUT method, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response putUpload(String name, File fileToUpload, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (putUpload(name, fileToUpload, url));
    }

    /**
     * Perform a file upload using multipart/form-data using the HTTP PUT method, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload

     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response putUpload(String name, File fileToUpload, URL url) throws IOException {

        try (MultiPart multiPart = new FormDataMultiPart()) {
            multiPart.bodyPart(new FileDataBodyPart(name, fileToUpload, MediaType.APPLICATION_OCTET_STREAM_TYPE));
            final Entity<?> entity = Entity.entity(multiPart, Boundary.addBoundary(multiPart.getMediaType()));
            return (invocation(url, null).put(entity));
        }
    }

    /**
     * Perform an HTTP PUT call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response put(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return (put(queryParams, url));
    }

    /**
     * Perform an HTTP PUT call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response put(MultivaluedMap<String, String> queryParams, URL url) {
        if (queryParams == null || queryParams.isEmpty()) {
            Entity<?> empty = Entity.text("");
            return (invocation(url, null).put(empty));
        } else {
            return (invocation(url, null).put(Entity.entity(queryParams, MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
        }
    }

    /**
     * Perform an HTTP PUT call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response put(Form formData, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        return put(formData, url);
    }

    /**
     * Perform an HTTP PUT call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param formData the Form containing the name/value pairs
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     */
    protected Response put(Form formData, URL url) {
        if (formData instanceof SonarApiForm)
            return (invocation(url, null).put(Entity.entity(formData.asMap(), MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
        else
            return (invocation(url, null).put(Entity.entity(formData, MediaType.APPLICATION_FORM_URLENCODED_TYPE)));
    }

    /**
     * Perform an HTTP PUT call with the specified payload object and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param payload the object instance that will be serialized to JSON and used as the PUT data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response put(Object payload, Object... pathArgs) throws IOException {
        URL url = getApiUrl(pathArgs);
        Entity<?> entity = Entity.entity(payload, MediaType.APPLICATION_JSON);
        return (invocation(url, null).put(entity));
    }

    /**
     * Perform an HTTP DELETE call with the specified form data and path objects, returning
     * a Response instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a Response instance with the data returned from the endpoint
     * @throws IOException if an error occurs while constructing the URL
     */
    protected Response delete(MultivaluedMap<String, String> queryParams, Object... pathArgs) throws IOException {
        return (delete(queryParams, getApiUrl(pathArgs)));
    }

    /**
     * Perform an HTTP DELETE call with the specified form data and URL, returning
     * a Response instance with the data returned from the endpoint.
     *
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a Response instance with the data returned from the endpoint
     */
    protected Response delete(MultivaluedMap<String, String> queryParams, URL url) {
        return (invocation(url, queryParams).delete());
    }

    protected Invocation.Builder invocation(URL url, MultivaluedMap<String, String> queryParams) {
        return (invocation(url, queryParams, MediaType.APPLICATION_JSON));
    }

    protected Client createApiClient() {

        // Explicitly use an instance of the JerseyClientBuilder, this allows this
        // library to work when both Jersey and Resteasy are present
        ClientBuilder clientBuilder = new JerseyClientBuilder().withConfig(clientConfig);

        // Register JacksonJson as the ObjectMapper provider.
        clientBuilder.register(JacksonJson.class);

        if (ignoreCertificateErrors) {
            clientBuilder.sslContext(openSslContext).hostnameVerifier(openHostnameVerifier);
        }

        apiClient = clientBuilder.build();
        return (apiClient);
    }

    protected Invocation.Builder invocation(URL url, MultivaluedMap<String, String> queryParams, String accept) {

        if (apiClient == null) {
            createApiClient();
        }

        WebTarget target = apiClient.target(url.toExternalForm()).property(ClientProperties.FOLLOW_REDIRECTS, true);
        if (queryParams != null) {
            for (Map.Entry<String, List<String>> param : queryParams.entrySet()) {
                target = target.queryParam(param.getKey(), param.getValue().toArray());
            }
        }

        String authValue = this.authToken;
        Invocation.Builder builder = target.request();
        if (accept == null || accept.trim().length() == 0) {
            builder = builder.header(AUTHORIZATION_HEADER, authValue);
        } else {
            builder = builder.header(AUTHORIZATION_HEADER, authValue).accept(accept);
        }

        // Set the per request connect timeout
        if (connectTimeout != null) {
            builder.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
        }

        // Set the per request read timeout
        if (readTimeout != null) {
            builder.property(ClientProperties.READ_TIMEOUT, readTimeout);
        }

        return (builder);
    }

    /**
     * Used to set the host URL to be used by OAUTH2 login in GitLabApi.
     */
    void setHostUrlToBaseUrl() {
        this.hostUrl = this.baseUrl;
    }

    /**
     * Returns true if the API is setup to ignore SSL certificate errors, otherwise returns false.
     *
     * @return true if the API is setup to ignore SSL certificate errors, otherwise returns false
     */
    public boolean getIgnoreCertificateErrors() {
        return (ignoreCertificateErrors);
    }

    /**
     * Sets up the Jersey system ignore SSL certificate errors or not.
     *
     * @param ignoreCertificateErrors if true will set up the Jersey system ignore SSL certificate errors
     */
    public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {

        if (this.ignoreCertificateErrors == ignoreCertificateErrors) {
            return;
        }

        if (!ignoreCertificateErrors) {

            this.ignoreCertificateErrors = false;
            openSslContext = null;
            openHostnameVerifier = null;
            apiClient = null;

        } else {

            if (setupIgnoreCertificateErrors()) {
                this.ignoreCertificateErrors = true;
                apiClient = null;
            } else {
                this.ignoreCertificateErrors = false;
                apiClient = null;
                throw new RuntimeException("Unable to ignore certificate errors.");
            }
        }
    }

    /**
     * Sets up Jersey client to ignore certificate errors.
     *
     * @return true if successful at setting up to ignore certificate errors, otherwise returns false.
     */
    private boolean setupIgnoreCertificateErrors() {

        // Create a TrustManager that trusts all certificates
        TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) throws CertificateException {
            }
        }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            openSslContext = sslContext;
            openHostnameVerifier = hostnameVerifier;
        } catch (GeneralSecurityException ex) {
            openSslContext = null;
            openHostnameVerifier = null;
            return (false);
        }

        return (true);
    }

}
