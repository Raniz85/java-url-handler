package se.raneland.urlhandler.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import se.raneland.urlhandler.aws.AwsClientFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Created by raniz on 23/08/15.
 */
public class S3UrlStreamHandler extends URLStreamHandler {

    private final AwsClientFactory<? extends AmazonS3> clientFactory;

    public S3UrlStreamHandler(AwsClientFactory<? extends AmazonS3> clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new S3UrlConnection(clientFactory, u);
    }
}
