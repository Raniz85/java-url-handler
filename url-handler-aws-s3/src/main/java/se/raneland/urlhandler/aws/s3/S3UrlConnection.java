package se.raneland.urlhandler.aws.s3;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.raneland.urlhandler.aws.AwsClientFactory;
import se.raneland.urlhandler.aws.ClientCreationException;
import se.raneland.urlhandler.aws.ClientOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * {@link URLConnection} implementation for Amazon S3 urls.
 *
 * Fetches the {@link ObjectMetadata} when connecting and the {@link S3Object}
 * when calling {@link URLConnection#getInputStream()}.
 *
 * @author Raniz
 * @since 1.0
 */
public class S3UrlConnection extends URLConnection {

    private static StringBuilder pipeJoin(StringBuilder builder, CharSequence c) {
        if(builder.length() > 0 && c.length() > 0) {
            builder.append("|");
        }
        return builder.append(c);
    }

    private static final Pattern BUCKET_REGION_PATTERN = Pattern.compile("^(.+?)(?:\\.("
            + Arrays.stream(Regions.values()).map(Regions::getName).collect(StringBuilder::new, S3UrlConnection::pipeJoin, S3UrlConnection::pipeJoin)
            + "))?$");

    // Format dates according to the HTTP spec
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final AwsClientFactory<? extends AmazonS3> clientFactory;
    private final String bucketName;
    private final String keyName;

    private AmazonS3 s3;
    private ObjectMetadata metadata;

    public S3UrlConnection(AwsClientFactory<? extends AmazonS3> clientFactory, URL url) throws ClientCreationException {
        super(url);
        this.clientFactory = clientFactory;
        Matcher matcher = BUCKET_REGION_PATTERN.matcher(url.getHost());
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid bucket name: " + url.getHost());
        }
        this.bucketName = matcher.group(1);
        this.keyName = url.getPath().replaceAll("^/+", "");
    }

    @Override
    public void connect() throws IOException {
        ClientOptions options = createClientOptions();
        this.s3 = clientFactory.create(options);
        // TODO: Exception translation
        this.metadata = s3.getObjectMetadata(bucketName, keyName);
    }

    /**
     * Create the {@link ClientOptions} that should be use to retrieve the {@link AmazonS3} client from the {@link AwsClientFactory}.
     *
     * @return
     */
    protected ClientOptions createClientOptions() {
        ClientOptions options = new ClientOptions();
        String userInfo = url.getUserInfo();
        if(userInfo != null) {
            if (userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                options.setAccessKeyId(parts[0]);
                options.setSecretAccessKey(parts[1]);
            } else {
                options.setProfile(userInfo);
            }
        }
        Matcher matcher = BUCKET_REGION_PATTERN.matcher(url.getHost());
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid bucket name: " + url.getHost());
        }
        String region = matcher.group(2);
        if(region != null && !region.isEmpty()) {
            options.setRegion(Region.getRegion(Regions.fromName(region)));
        }
        return options;
    }

    @Override
    public long getContentLengthLong() {
        return metadata.getContentLength();
    }

    @Override
    public String getContentType() {
        return metadata.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return metadata.getContentEncoding();
    }

    @Override
    public long getExpiration() {
        Date expires = metadata.getHttpExpiresDate();
        if(expires != null) {
            return expires.toInstant().toEpochMilli();
        }
        return 0;
    }

    @Override
    public long getLastModified() {
        Date lastModified = metadata.getLastModified();
        if(lastModified != null) {
            return lastModified.toInstant().toEpochMilli();
        }
        return 0;
    }

    @Override
    public String getHeaderField(String name) {
        name = name.toLowerCase();
        switch(name) {
            case "cache-control":
                return metadata.getCacheControl();
            case "content-disposition":
                return metadata.getContentDisposition();
            case "content-encoding":
                return metadata.getContentEncoding();
            case "content-length":
                return String.valueOf(metadata.getContentLength());
            case "content-md5":
                return metadata.getContentMD5();
            case "content-type":
                return metadata.getContentType();
            case "etag":
                return metadata.getETag();
            case "last-modified":
                Date lastModified = metadata.getLastModified();
                if(lastModified != null) {
                    return DATE_TIME_FORMATTER.format(lastModified.toInstant());
                }
                return null;
            case "expires":
                Date expires = metadata.getHttpExpiresDate();
                if(expires != null) {
                    return DATE_TIME_FORMATTER.format(expires.toInstant());
                }
                return null;
        }
        return metadata.getUserMetaDataOf(name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        Map<String, List<String>> headers = new HashMap<>();

        // Add custom headers
        for(Map.Entry<String, String> entry : metadata.getUserMetadata().entrySet()) {
            headers.put(entry.getKey(), Collections.singletonList(entry.getValue()));
        }

        // Add standard headers
        headers.put("Cache-Control", Collections.singletonList(metadata.getCacheControl()));
        headers.put("Content-Disposition", Collections.singletonList(metadata.getContentDisposition()));
        headers.put("Content-Encoding", Collections.singletonList(metadata.getContentEncoding()));
        headers.put("Content-Length", Collections.singletonList(String.valueOf(metadata.getContentLength())));
        headers.put("Content-MD5", Collections.singletonList(metadata.getContentMD5()));
        headers.put("Content-Type", Collections.singletonList(metadata.getContentType()));
        headers.put("ETag", Collections.singletonList(metadata.getETag()));
        headers.put("Last-Modified", Collections.singletonList(DATE_TIME_FORMATTER.format(metadata.getLastModified().toInstant())));
        headers.put("Expires", Collections.singletonList(DATE_TIME_FORMATTER.format(metadata.getHttpExpiresDate().toInstant())));

        return headers;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final S3Object object = s3.getObject(bucketName, keyName);
        return new S3ObjectInputStream(object);
    }
}
