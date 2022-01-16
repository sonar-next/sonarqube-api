package com.github.sonarnext.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.sonarnext.api.utils.JacksonJson;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class SonarApiException extends Exception {

    private static final long serialVersionUID = 1L;
    private Response.StatusType statusInfo;
    private int httpStatus;
    private String message;
    private Map<String, List<String>> validationErrors;
    private List<String> errors = new ArrayList<>(2);

    /**
     * Create a SonarApiException instance with the specified message.
     *
     * @param message the message for the exception
     */
    public SonarApiException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * Create a SonarApiException instance with the specified message and HTTP status code.
     *
     * @param message the message for the exception
     * @param httpStatus the HTTP status code for the exception
     */
    public SonarApiException(String message, int httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * Create a SonarApiException instance based on the ClientResponse.
     *
     * @param response the JAX-RS response that caused the exception
     */
    public SonarApiException(Response response) {

        super();
        statusInfo = response.getStatusInfo();
        httpStatus = response.getStatus();

        if (response.hasEntity()) {

            try {

                String message = response.readEntity(String.class);
                this.message = message;

                // Determine what is in the content of the response and process it accordingly
                MediaType mediaType = response.getMediaType();
                if (mediaType != null && "json".equals(mediaType.getSubtype())) {

                    JsonNode json = JacksonJson.toJsonNode(message);

                    // First see if it is a "message", if so it is either a simple message,
                    // or a Map<String, List<String>> of validation errors
                    JsonNode jsonMessage = json.get("errors");
                    if (jsonMessage != null) {

                        // If the node is an object, then it is validation errors
                        if (jsonMessage.isObject()) {

                            StringBuilder buf = new StringBuilder();
                            validationErrors = new HashMap<>();
                            Iterator<Map.Entry<String, JsonNode>> fields = jsonMessage.fields();
                            while(fields.hasNext()) {

                                Map.Entry<String, JsonNode> field = fields.next();
                                String fieldName = field.getKey();
                                List<String> values = new ArrayList<>();
                                validationErrors.put(fieldName, values);
                                for (JsonNode value : field.getValue()) {
                                    values.add(value.asText());
                                }

                                if (values.size() > 0) {
                                    buf.append((buf.length() > 0 ? ", " : "")).append(fieldName);
                                }
                            }

                            if (buf.length() > 0) {
                                this.message = "The following fields have validation errors: " + buf.toString();
                            }

                        } else if (jsonMessage.isArray()) {

                            List<String> values = new ArrayList<>();
                            for (JsonNode value : jsonMessage) {
                                JsonNode msg = value.get("msg");
                                if (msg != null) {
                                    values.add(msg.asText());
                                    this.errors.add(msg.asText());
                                }
                            }

                            if (values.size() > 0) {
                                this.message = String.join("\n", values);
                            }

                        } else if (jsonMessage.isTextual()) {
                            this.message = jsonMessage.asText();
                        } else {
                            this.message = jsonMessage.toString();
                        }

                    } else {

                        JsonNode jsonError = json.get("error");
                        if (jsonError != null) {
                            this.message = jsonError.asText();
                        }
                    }
                }

            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Create a SonarApiException instance based on the exception.
     *
     * @param e the Exception to wrap
     */
    public SonarApiException(Exception e) {
        super(e);
        message = e.getMessage();
    }

    /**
     * Get the message associated with the exception.
     *
     * @return the message associated with the exception
     */
    @Override
    public final String getMessage() {
        return (message != null ? message : getReason());
    }

    /**
     * Returns the HTTP status reason message, returns null if the
     * causing error was not an HTTP related exception.
     *
     * @return the HTTP status reason message
     */
    public final String getReason() {
        return (statusInfo != null ? statusInfo.getReasonPhrase() : null);
    }

    /**
     * Returns the HTTP status code that was the cause of the exception. returns 0 if the
     * causing error was not an HTTP related exception
     *
     * @return the HTTP status code, returns 0 if the causing error was not an HTTP related exception
     */
    public final int getHttpStatus() {
        return (httpStatus);
    }

    /**
     * Returns true if this SonarApiException was caused by validation errors on the GitLab server,
     * otherwise returns false.
     *
     * @return true if this SonarApiException was caused by validation errors on the GitLab server,
     * otherwise returns false
     */
    public boolean hasValidationErrors() {
        return (validationErrors != null);
    }

    /**
     * Returns a Map&lt;String, List&lt;String&gt;&gt; instance containing validation errors if this SonarApiException
     * was caused by validation errors on the GitLab server, otherwise returns null.
     *
     * @return a Map&lt;String, List&lt;String&gt;&gt; instance containing validation errors if this SonarApiException
     * was caused by validation errors on the GitLab server, otherwise returns null
     */
    public Map<String, List<String>> getValidationErrors() {
        return (validationErrors);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + httpStatus;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((statusInfo == null) ? 0 : statusInfo.hashCode());
        result = prime * result + ((validationErrors == null) ? 0 : validationErrors.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        SonarApiException other = (SonarApiException) obj;
        if (httpStatus != other.httpStatus) {
            return false;
        }

        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message)) {
            return false;
        }

        if (statusInfo == null) {
            if (other.statusInfo != null)
                return false;
        } else if (!statusInfo.equals(other.statusInfo)) {
            return false;
        }

        if (validationErrors == null) {
            if (other.validationErrors != null)
                return false;
        } else if (!validationErrors.equals(other.validationErrors)) {
            return false;
        }

        return true;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
