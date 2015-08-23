package se.raneland.urlhandler.aws;

import com.amazonaws.AmazonWebServiceClient;

/**
 * Created by raniz on 23/08/15.
 */
public interface AwsClientFactory<C> {

    C create(ClientOptions options) throws ClientCreationException;

}
