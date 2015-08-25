package se.raneland.urlhandler.aws.s3;

import com.amazonaws.services.s3.model.S3Object;
import lombok.experimental.Delegate;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} implementation that cleans up all S3 resources.
 *
 * @author Raniz
 * @since 1.0
 */
public class S3ObjectInputStream extends InputStream {

    private final S3Object object;

    @Delegate(excludes = Closeable.class)
    private final InputStream inputStream;

    public S3ObjectInputStream(S3Object object) {
        this.object = object;
        this.inputStream = object.getObjectContent();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        object.close();
    }
}
