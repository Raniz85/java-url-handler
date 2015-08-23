package se.raneland.urlhandler.aws;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by raniz on 23/08/15.
 */
public class DefaultAwsClientFactory<C extends AmazonWebServiceClient> implements AwsClientFactory<C> {

    private final Class<C> clientClass;
    private final String serviceName;
    private final ConcurrentHashMap<ClientOptions, C> instances;

    public DefaultAwsClientFactory(Class<C> clientClass) {
        this(clientClass, clientClass.getSimpleName().replaceAll("^Amazon(.+)Client$", "$1").toLowerCase());
    }

    public DefaultAwsClientFactory(Class<C> clientClass, String serviceName) {
        this.clientClass = clientClass;
        this.serviceName = serviceName;
        this.instances = new ConcurrentHashMap<ClientOptions, C>();
    }

    @Override
    public C create(ClientOptions options) throws ClientCreationException {
        if(instances.containsKey(options)) {
            return instances.get(options);
        }
        AWSCredentialsProvider credentialsProvider = createCredentials(options);
        ClientConfiguration configuration = createConfiguration(options);
        C client = createClient(credentialsProvider, configuration);
        if(options.getEndpoint() != null) {
            client.setEndpoint(options.getEndpoint());
        } else if (options.getRegion() != null) {
            client.setRegion(options.getRegion());
        }
        instances.put(options, client);
        return null;
    }

    protected C createClient(AWSCredentialsProvider credentialsProvider, ClientConfiguration configuration) throws ClientCreationException {
        try {
            Constructor<C> constructor = clientClass.getConstructor(AWSCredentialsProvider.class, ClientConfiguration.class);
            return constructor.newInstance(credentialsProvider, configuration);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ClientCreationException("Can not instantiate client: " + e.getMessage(), e);
        }
    }

    protected AWSCredentialsProvider createCredentials(ClientOptions options) {
        if(options.getProfile() != null) {
            return new ProfileCredentialsProvider(options.getProfile());
        }
        if(options.getAccessKeyId() != null && options.getSecretAccessKey() !=  null) {
            return new StaticCredentialsProvider(new BasicAWSCredentials(options.getAccessKeyId(), options.getSecretAccessKey()));
        }
        return createDefaultCredentials();
    }

    protected AWSCredentialsProvider createDefaultCredentials() {
        // Try system properties
        String accessKeyId = System.getProperty("aws." + serviceName + ".accessKeyId");
        String secretAccessKey = System.getProperty("aws." + serviceName + ".secretAccessKey");
        if(accessKeyId != null && secretAccessKey != null) {
            return new StaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
        }

        // Try environment properties
        accessKeyId = System.getenv("AWS_" + serviceName.toUpperCase() + "_ACCESS_KEy_ID");
        secretAccessKey = System.getenv("AWS_" + serviceName.toUpperCase() + "_SECRET_ACCESS_KEY");
        if(accessKeyId != null && secretAccessKey != null) {
            return new StaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
        }

        // Try global system properties
        accessKeyId = System.getProperty("aws.accessKeyId");
        secretAccessKey = System.getProperty("aws.secretAccessKey");
        if(accessKeyId != null && secretAccessKey != null) {
            return new StaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
        }

        // Try global environment properties
        accessKeyId = System.getenv("AWS_ACCESS_KEy_ID");
        secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        if(accessKeyId != null && secretAccessKey != null) {
            return new StaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
        }

        // Use the default chain
        return new DefaultAWSCredentialsProviderChain();
    }

    protected ClientConfiguration createConfiguration(ClientOptions options) {
        ClientConfiguration configuration = new ClientConfiguration();

        // Set proxy configuration
        configuration.setProxyDomain(System.getProperty("https.proxyDomain"));
        configuration.setProxyWorkstation("https.proxyWorkstation");
        configuration.setProxyHost(System.getProperty("https.proxyHost"));
        String port = System.getProperty("https.proxyPort");
        if(port != null && !port.isEmpty()) {
            configuration.setProxyPort(Integer.valueOf(port));
        }
        configuration.setProxyUsername("https.proxyUsername");
        configuration.setProxyPassword("https.proxyPassword");

        return configuration;
    }
}
