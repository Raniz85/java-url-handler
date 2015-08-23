package se.raneland.urlhandler.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import lombok.Data;

/**
 * Created by raniz on 23/08/15.
 */
@Data
public class ClientOptions {

    private String profile;
    private String accessKeyId;
    private String secretAccessKey;

    private Region region;
    private String endpoint;

    private ClientConfiguration configuration;
}
