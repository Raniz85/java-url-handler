package se.raneland.urlhandler.aws.s3;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import se.raneland.urlhandler.aws.AwsClientFactory;
import se.raneland.urlhandler.aws.ClientCreationException;
import se.raneland.urlhandler.aws.ClientOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * {@link URLConnection} implementation for Amazon S3 urls.
 * @author Raniz
 */
public class S3UrlConnection extends URLConnection {

    private final AwsClientFactory<? extends AmazonS3> clientFactory;
    private final String bucketName;
    private final String keyName;

    private AmazonS3 s3;
    private ObjectMetadata metadata;

    public S3UrlConnection(AwsClientFactory<? extends AmazonS3> clientFactory, URL url) throws ClientCreationException {
        super(url);
        this.clientFactory = clientFactory;
        this.bucketName = url.getHost().split(".", 2)[0];
        this.keyName = url.getPath();
    }

    @Override
    public void connect() throws IOException {
        ClientOptions options = createClientOptions();
        this.s3 = clientFactory.create(options);
        this.metadata = s3.getObjectMetadata(bucketName, keyName);
    }

    protected ClientOptions createClientOptions() {
        ClientOptions options = new ClientOptions();
        String userInfo = url.getUserInfo();
        if(userInfo.contains(":")) {
            String[] parts = userInfo.split(":", 2);
            options.setAccessKeyId(parts[0]);
            options.setSecretAccessKey(parts[1]);
        } else {
            options.setProfile(userInfo);
        }
        String host = url.getHost();
        if(host.contains(".")) {
            String regionOrEndpoint = host.split(".", 2)[1];
            boolean isRegion = false;
            for (Regions region : Regions.values()) {
                if (region.getName().equalsIgnoreCase(regionOrEndpoint)) {
                    options.setRegion(Region.getRegion(region));
                    isRegion = true;
                    break;
                }
            }
            if (!isRegion) {
                options.setEndpoint(regionOrEndpoint);
            }
        }
        return options;
    }

    // TODO: Implement header fields

    @Override
    public InputStream getInputStream() throws IOException {
        final S3Object object = s3.getObject(bucketName, keyName);
        return object.getObjectContent();
    }
}
