/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.

  This software is distributed WITHOUT ANY WARRANTY; without even
  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
  PURPOSE.

  See LICENSE.txt in the top level directory for details.

=============================================================================*/

package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploaderUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static java.net.HttpURLConnection.*;


/**
 * Base class for all HTTP requests to the GIFT-Cloud REST server
 *
 * All communication with the GIFT-Cloud server should be using objects derived from this type. This ensures proper
 * error handling and automated login after a connection has been lost.
 *
 * @param <T> type of the response that will be returned to the caller if the request succeeds. This is the type
 *           returned by the response processor after processing the server's reply
 */
abstract class HttpRequest<T> {

    private final HttpConnectionWrapper.ConnectionType connectionType;
    protected final String urlString;

    // The response value could be null. As you can't store a null value in an Optional, we use an Optional of Optional.
    // The outer Optional determines if a response has been set. The inner Optional determines whether this response is null or a value
    private Optional<Optional<T>> response = Optional.empty();

    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final HttpResponseProcessor<T> responseProcessor;
    private final MultiUploadReporter reporter;
    private final String userAgentString;

    /**
     * Create a new request object that will connect to the given URL, and whose server reply will be interpreted by the response processor
     *  @param connectionType whether this request call is GET, POST, PUT
     * @param urlString the relative URL of the resource being referred to by the request call (i.e. excluding the server URL)
     * @param responseProcessor the object that will process the server's reply and produce an output of the parameterised type T
     * @param giftCloudProperties
     * @param reporter an object for reporting errors and warnings back to the user and/or program logs
     */
    HttpRequest(final HttpConnectionWrapper.ConnectionType connectionType, final String urlString, final HttpResponseProcessor<T> responseProcessor, GiftCloudProperties giftCloudProperties, final MultiUploadReporter reporter) {
        this.connectionType = connectionType;
        this.urlString = urlString;
        this.responseProcessor = responseProcessor;
        this.reporter = reporter;
        userAgentString = giftCloudProperties.getUserAgentString();
    }

    /**
     * Executes the request and processes the server response to produce an output of the parameterised type T
     *
     * @param connectionFactory used to construct the HTTP connection object
     * @return the result object computed by the response processor based on the response from the server
     * @throws IOException if an error occurs during the server communication
     */
    final T getResponse(final ConnectionFactory connectionFactory) throws IOException {
        if (!response.isPresent()) {
            doRequest(connectionFactory);
        }
        // The value of the response should now be set to an Optional - if this inner opttional is not set, that indicates a null value
        return response.get().orElse(null);
    }

    /**
     * Set the parameters for the connection. A subclass may wish to override this, but should call the base class
     *
     * @param connectionBuilder a builder object used to set the connection parameters in advance of creating the connection
     * @throws IOException not thrown by the base class but might be thrown by subclasses
     */
    // Get the connection parameters. These may be altered by subclasses
    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException
    {
        // Add version
        connectionBuilder.setUserAgent(userAgentString);

        // Accept all media types
        connectionBuilder.setAccept("*/*");

        // Set the type of request
        connectionBuilder.setConnectionType(connectionType);
    }

    /**
     * Subclasses use this method to write to the connection's output stream (if it exists) before performing the request
     *
     * @param connectionWrapper the connection interface to be used to write to the output stream
     * @throws IOException may be thrown by the implementing methods during writing to the output stream
     */
    abstract protected void processOutputStream(final HttpConnectionWrapper connectionWrapper) throws IOException;

    private void doRequest(final ConnectionFactory connectionFactory) throws IOException {

        try {
            final HttpConnectionBuilder connectionBuilder = new HttpConnectionBuilder(urlString);

            prepareConnection(connectionBuilder);

            // Build the connection
            final HttpConnectionWrapper connection = connectionFactory.createConnection(urlString, connectionBuilder);

            // Send data to the connection if required
            processOutputStream(connection);

            try {
                // Explicitly initiate the connection
                connection.connect();

                try {
                    throwIfBadResponse(connection);

                    // Get data from the connection and process. In the case of an error, this will process the error stream
                    response = Optional.of(Optional.ofNullable(responseProcessor.processInputStream(connection)));

                } finally {
                    connection.disconnect();
                }

            } catch (IOException e) {
                reporter.silentLogException(e, "An error occurred while processing request " + connection.getUrlString());
                throwIfBadResponse(connection);
                throw e;
            }
        } finally {
            cleanup();
        }
    }

    /**
     * This method will be called after the request has completed, even if it failed.
     * Subclasses use this method to perform any required cleanup.
     */
    protected void cleanup() {
    }

    private void throwIfBadResponse(final HttpConnectionWrapper connection) throws IOException {

        final String urlString = connection.getUrlString();
        final URL url = connection.getURL();
        final int responseCode = connection.getResponseCode();
        final ErrorDetails errorDetails = HttpRequestErrorMessages.getResponseMessage(responseCode, urlString, url);

        switch (responseCode) {
            case HTTP_ACCEPTED:
            case HTTP_NOT_AUTHORITATIVE:
            case HTTP_NO_CONTENT:
            case HTTP_RESET:
            case HTTP_PARTIAL:
            case HTTP_MOVED_PERM:
                reporter.silentWarning(errorDetails.getTitle() + ":" + errorDetails.getHtmlText() + " Details: request method " + connection.getRequestMethod() + " to URL " + urlString + " returned " + responseCode + " with message " + connection.getResponseMessage());
                return;

            case HTTP_OK:
            case HTTP_CREATED:
                return;

            // Handle 302, at least temporarily: Spring auth redirects to login page,
            // so assume that's what's happened when we see a redirect at this point.
            case HTTP_MOVED_TEMP:
            case HTTP_UNAUTHORIZED:
                reporter.silentWarning(errorDetails.getTitle() + ":" + errorDetails.getHtmlText() + " Details: request method " + connection.getRequestMethod() + " to URL " + urlString + " returned " + responseCode + " with message " + connection.getResponseMessage());
                throw new AuthorisationFailureException(responseCode, url);

            case HTTP_BAD_REQUEST:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_CONFLICT:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_INTERNAL_ERROR:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            case HTTP_NOT_FOUND:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());

            default:
                throw new GiftCloudHttpException(responseCode, errorDetails.getTitle(), MultiUploaderUtils.getErrorEntity(connection.getErrorStream()), errorDetails.getHtmlText());
        }
    }

}


