package se.raneland.urlhandler.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import lombok.Data;

/**
 * Options for creating an Amazon Web Services client implementation.
 *
 * @author Raniz
 * @since 1.0
 */
@Data
public class ClientOptions {

    /**
     * The AWS profile name to use.
     *
     * @param profile The AWS profile name to use
     * @return The AWS profile name to use
     */
    private String profile;

    /**
     * The ID of the AWS access key to use.
     *
     * @param accessKeyId The ID of the AWS access key to use
     * @return The ID of the AWS access key to use
     */
    private String accessKeyId;

    /**
     * The AWS secret access key to use.
     *
     * @param secretAccessKey The AWS secret access key to use
     * @return The AWS secret access key to use
     */
    private String secretAccessKey;

    /**
     * The AWS region to connect to
     *
     * @param region The AWS region to connect to
     * @return The AWS region to connect to
     */
    private Region region;

    /**
     * The endpoint to connect to
     *
     * @param endpoint The endpoint to connect to
     * @return The endpoint to connect to
     */
    private String endpoint;
}
