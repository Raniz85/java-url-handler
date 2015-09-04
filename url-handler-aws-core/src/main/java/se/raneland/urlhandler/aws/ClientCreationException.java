package se.raneland.urlhandler.aws;

import java.io.IOException;

/**
 * Exception thrown when an {@link AwsClientFactory} cant't create a client instance.
 *
 * @author Raniz
 * @since 1.0
 */
public class ClientCreationException extends IOException {
    public ClientCreationException() {
    }

    public ClientCreationException(String message) {
        super(message);
    }

    public ClientCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientCreationException(Throwable cause) {
        super(cause);
    }
}
