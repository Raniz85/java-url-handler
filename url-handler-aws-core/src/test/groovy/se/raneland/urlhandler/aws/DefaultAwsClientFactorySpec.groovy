package se.raneland.urlhandler.aws

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.simpledb.AmazonSimpleDB
import com.amazonaws.services.simpledb.AmazonSimpleDBClient
import spock.lang.Specification

/**
 * Created by raniz on 2015-08-25.
 */
class DefaultAwsClientFactorySpec extends Specification {

    def "That a default client can be created"() {
        given: "A client factory"
        def factory = new DefaultAwsClientFactory<>(AmazonSimpleDBClient.class)

        when: "A client is requested without options"
        def client = factory.create(null)

        then: "A client is created"
        assert client
    }

    def "That a client with a specific profile can be created"() {
        given: "A client factory and a configuration with a profile"
        def profileName = "profile"
        def options = new ClientOptions(profile: profileName)
        def factory = new DefaultAwsClientFactory<>(AmazonSimpleDBClient.class)

        when: "A client is requested"
        def client = factory.create(options)

        then: "A client is created with the correct profile set"
        assert client
        assert client.@awsCredentialsProvider instanceof ProfileCredentialsProvider
        assert client.@awsCredentialsProvider.profileName == profileName
    }

    def "That a client with a specific access key can be created"() {
        given: "A client factory and a configuration with an access key"
        def accessKeyId = "keyId"
        def secretAccessKey = "accessKey"
        def options = new ClientOptions(accessKeyId: accessKeyId, secretAccessKey: secretAccessKey)
        def factory = new DefaultAwsClientFactory<>(AmazonSimpleDBClient.class)

        when: "A client is requested"
        def client = factory.create(options)

        then: "A client is created with the correct profile set"
        assert client
        assert client.@awsCredentialsProvider instanceof StaticCredentialsProvider
        assert client.@awsCredentialsProvider.credentials instanceof BasicAWSCredentials
        assert client.@awsCredentialsProvider.credentials.accessKey == accessKeyId
        assert client.@awsCredentialsProvider.credentials.secretKey == secretAccessKey
    }

    def "That a client with a specific region can be created"() {
        given: "A client factory and a configuration with a region"
        def region = Region.getRegion(Regions.EU_WEST_1)
        def options = new ClientOptions(region: region)
        def factory = new DefaultAwsClientFactory<>(AmazonSimpleDBClient.class)

        when: "A client is requested without options"
        def client = factory.create(options)

        then: "A client is created with the correct region set"
        assert client
        assert client.@endpoint.toString().endsWith(region.getServiceEndpoint("sdb"))
    }

    def "That a client with a specific endpoint can be created"() {
        given: "A client factory and a configuration with a region"
        def endpoint = "http://sdb.test.com"
        def options = new ClientOptions(endpoint: endpoint)
        def factory = new DefaultAwsClientFactory<>(AmazonSimpleDBClient.class)

        when: "A client is requested without options"
        def client = factory.create(options)

        then: "A client is created with the correct region set"
        assert client
        assert client.@endpoint.toString().endsWith(endpoint)
    }
}
