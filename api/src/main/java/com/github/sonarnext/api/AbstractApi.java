package com.github.sonarnext.api;

import com.github.sonarnext.api.utils.UrlEncoder;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.net.URL;

public abstract class AbstractApi implements Constants {

    private final SonarApi sonarApi;

    public AbstractApi(SonarApi sonarApi) {
        this.sonarApi = sonarApi;
    }


    protected int getDefaultPerPage() {
        return (sonarApi.getDefaultPerPage());
    }

    protected SonarApiClient getApiClient() {
        return (sonarApi.getApiClient());
    }

    /**
     * Encode a string to be used as in-path argument for a gitlab api request.
     *
     * Standard URL encoding changes spaces to plus signs, but for arguments that are part of the path,
     * like the :file_path in a "Get raw file" request, gitlab expects spaces to be encoded with %20.
     *
     * @param s the string to encode
     * @return encoded version of s with spaces encoded as %2F
     * @throws SonarApiException if encoding throws an exception
     */
    protected String urlEncode(String s) throws SonarApiException {
        return (UrlEncoder.urlEncode(s));
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response get(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().get(queryParams, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param accepts if non-empty will set the Accepts header to this value
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response getWithAccepts(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams,
        String accepts, Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().getWithAccepts(queryParams, accepts, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP GET call with the specified query parameters and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response get(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams, URL url) throws SonarApiException {
        try {
            return validate(getApiClient().get(queryParams, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP HEAD call with the specified query parameters and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response head(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().head(queryParams, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP POST call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param formData the Form containing the name/value pairs for the POST data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response post(Response.Status expectedStatus, Form formData, Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().post(formData, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP POST call with the specified payload object and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param payload the object instance that will be serialized to JSON and used as the POST data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response post(Response.Status expectedStatus, Object payload, Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().post(payload, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP POST call with the specified payload object and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param stream the StreamingOutput that will be used for the POST data
     * @param mediaType the content-type for the streamed data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response post(Response.Status expectedStatus, StreamingOutput stream, String mediaType,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().post(stream, mediaType, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP POST call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response post(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().post(queryParams, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP POST call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param formData the Form containing the name/value pairs for the POST data
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response post(Response.Status expectedStatus, Form formData, URL url) throws SonarApiException {
        try {
            return validate(getApiClient().post(formData, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform a file upload with the specified File instance and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param mediaType the content-type of the uploaded file, if null will be determined from fileToUpload
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response upload(Response.Status expectedStatus, String name, File fileToUpload, String mediaType,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().upload(name, fileToUpload, mediaType, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform a file upload with the specified File instance and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param mediaType the content-type of the uploaded file, if null will be determined from fileToUpload
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response upload(Response.Status expectedStatus, String name, File fileToUpload, String mediaType,
        URL url) throws SonarApiException {
        try {
            return validate(getApiClient().upload(name, fileToUpload, mediaType, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform a file upload with the specified File instance and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param mediaType the content-type of the uploaded file, if null will be determined from fileToUpload
     * @param formData the Form containing the name/value pairs
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response upload(Response.Status expectedStatus, String name, File fileToUpload, String mediaType,
        Form formData, URL url) throws SonarApiException {

        try {
            return validate(getApiClient().upload(name, fileToUpload, mediaType, formData, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP PUT call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response put(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().put(queryParams, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP PUT call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response put(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams, URL url) throws SonarApiException {
        try {
            return validate(getApiClient().put(queryParams, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP PUT call with the specified payload object and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param payload the object instance that will be serialized to JSON and used as the PUT data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response put(Response.Status expectedStatus, Object payload, Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().put(payload, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP PUT call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param formData the Form containing the name/value pairs for the POST data
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response putWithFormData(Response.Status expectedStatus, Form formData, Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().put(formData, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }


    /**
     * Perform a file upload using the HTTP PUT method with the specified File instance and path objects,
     * returning a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response putUpload(Response.Status expectedStatus, String name, File fileToUpload, Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().putUpload(name, fileToUpload, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform a file upload using the HTTP PUT method with the specified File instance and path objects,
     * returning a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param name the name for the form field that contains the file name
     * @param fileToUpload a File instance pointing to the file to upload
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response putUpload(Response.Status expectedStatus, String name, File fileToUpload, URL url) throws SonarApiException {
        try {
            return validate(getApiClient().putUpload(name, fileToUpload, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP DELETE call with the specified form data and path objects, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param pathArgs variable list of arguments used to build the URI
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response delete(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams,
        Object... pathArgs) throws SonarApiException {
        try {
            return validate(getApiClient().delete(queryParams, pathArgs), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Perform an HTTP DELETE call with the specified form data and URL, returning
     * a ClientResponse instance with the data returned from the endpoint.
     *
     * @param expectedStatus the HTTP status that should be returned from the server
     * @param queryParams multivalue map of request parameters
     * @param url the fully formed path to the GitLab API endpoint
     * @return a ClientResponse instance with the data returned from the endpoint
     * @throws SonarApiException if any exception occurs during execution
     */
    protected Response delete(Response.Status expectedStatus, MultivaluedMap<String, String> queryParams, URL url) throws SonarApiException {
        try {
            return validate(getApiClient().delete(queryParams, url), expectedStatus);
        } catch (Exception e) {
            throw handle(e);
        }
    }

    /**
     * Convenience method for adding query and form parameters to a get() or post() call.
     *
     * @param formData the Form containing the name/value pairs
     * @param name the name of the field/attribute to add
     * @param value the value of the field/attribute to add
     */
    protected void addFormParam(Form formData, String name, Object value) throws IllegalArgumentException {
        addFormParam(formData, name, value, false);
    }

    /**
     * Convenience method for adding query and form parameters to a get() or post() call.
     * If required is true and value is null, will throw an IllegalArgumentException.
     *
     * @param formData the Form containing the name/value pairs
     * @param name the name of the field/attribute to add
     * @param value the value of the field/attribute to add
     * @param required the field is required flag
     * @throws IllegalArgumentException if a required parameter is null or empty
     */
    protected void addFormParam(Form formData, String name, Object value, boolean required) throws IllegalArgumentException {

        if (value == null) {

            if (required) {
                throw new IllegalArgumentException(name + " cannot be empty or null");
            }

            return;
        }

        String stringValue = value.toString();
        if (required && stringValue.trim().length() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty or null");
        }

        formData.param(name, stringValue);
    }

    /**
     * Validates response the response from the server against the expected HTTP status and
     * the returned secret token, if either is not correct will throw a SonarApiException.
     *
     * @param response response
     * @param expected expected response status
     * @return original response if the response status is expected
     * @throws SonarApiException if HTTP status is not as expected, or the secret token doesn't match
     */
    protected Response validate(Response response, Response.Status expected) throws SonarApiException {

        int responseCode = response.getStatus();
        int expectedResponseCode = expected.getStatusCode();

        if (responseCode != expectedResponseCode) {

            // If the expected code is 200-204 and the response code is 200-204 it is OK.  We do this because
            // GitLab is constantly changing the expected code in the 200 to 204 range
            if (expectedResponseCode > 204 || responseCode > 204 || expectedResponseCode < 200 || responseCode < 200)
                throw new SonarApiException(response);
        }

        return (response);
    }

    /**
     * Wraps an exception in a SonarApiException if needed.
     *
     * @param thrown the exception that should be wrapped
     * @return either the untouched SonarApiException or a new GitLabApiExceptin wrapping a non-SonarApiException
     */
    protected SonarApiException handle(Exception thrown) {

        if (thrown instanceof SonarApiException) {
            return ((SonarApiException) thrown);
        }

        return (new SonarApiException(thrown));
    }

    /**
     * Creates a MultivaluedMap instance containing the "per_page" param.
     *
     * @param perPage the number of projects per page
     * @return a MultivaluedMap instance containing the "per_page" param
     */
    protected MultivaluedMap<String, String> getPerPageQueryParam(int perPage) {
        return (new SonarApiForm().withParam(PER_PAGE_PARAM, perPage).asMap());
    }

    /**
     * Creates a MultivaluedMap instance containing "page" and "per_page" params.
     *
     * @param page the page to get
     * @param perPage the number of projects per page
     * @return a MultivaluedMap instance containing "page" and "per_page" params
     */
    protected MultivaluedMap<String, String> getPageQueryParams(int page, int perPage) {
        return (new SonarApiForm().withParam(PAGE_PARAM, page).withParam(PER_PAGE_PARAM, perPage).asMap());
    }

    /**
     * Creates a MultivaluedMap instance containing "page" and "per_page" params.
     *
     * @param page the page to get
     * @param perPage the number of projects per page
     * @param customAttributesEnabled enables customAttributes for this query
     * @return a MultivaluedMap instance containing "page" and "per_page" params
     */
    protected MultivaluedMap<String, String> getPageQueryParams(int page, int perPage, boolean customAttributesEnabled) {

        SonarApiForm form = new SonarApiForm().withParam(PAGE_PARAM, page).withParam(PER_PAGE_PARAM, perPage);
        if (customAttributesEnabled)
            return (form.withParam("with_custom_attributes", true).asMap());

        return (form.asMap());
    }

    /**
     * Creates a MultivaluedMap instance containing the "per_page" param with the default value.
     *
     * @return a MultivaluedMap instance containing the "per_page" param with the default value
     */
    protected MultivaluedMap<String, String> getDefaultPerPageParam() {
        return (new SonarApiForm().withParam(PER_PAGE_PARAM, getDefaultPerPage()).asMap());
    }

    /**
     * Creates a MultivaluedMap instance containing the "per_page" param with the default value.
     *
     * @param customAttributesEnabled enables customAttributes for this query
     * @return a MultivaluedMap instance containing the "per_page" param with the default value
     */
    protected MultivaluedMap<String, String> getDefaultPerPageParam(boolean customAttributesEnabled) {

        SonarApiForm form = new SonarApiForm().withParam(PER_PAGE_PARAM, getDefaultPerPage());
        if (customAttributesEnabled)
            return (form.withParam("with_custom_attributes", true).asMap());

        return (form.asMap());
    }
}
