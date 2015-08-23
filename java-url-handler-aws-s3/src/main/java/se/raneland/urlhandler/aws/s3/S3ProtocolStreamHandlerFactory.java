package se.raneland.urlhandler.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import se.raneland.urlhandler.ProtocolStreamHandlerFactory;
import se.raneland.urlhandler.aws.AwsClientFactory;
import se.raneland.urlhandler.aws.DefaultAwsClientFactory;

import java.net.URLStreamHandler;

/**
 * Created by raniz on 23/08/15.
 */
public class S3ProtocolStreamHandlerFactory implements ProtocolStreamHandlerFactory {
    private final AwsClientFactory<? extends AmazonS3> clientFactory;

    public S3ProtocolStreamHandlerFactory() {
        this(new DefaultAwsClientFactory<>(AmazonS3Client.class));
    }

    public S3ProtocolStreamHandlerFactory(AwsClientFactory<? extends AmazonS3> clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public String[] getSupportedProtocols() {
        return new String[]{ "s3" };
    }

    @Override
    public URLStreamHandler createStreamHandler(String protocol) {
        return new S3UrlStreamHandler(clientFactory);
    }
}
