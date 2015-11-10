package se.raneland.urlhandler.aws.s3

import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import se.raneland.urlhandler.aws.AwsClientFactory;
import spock.lang.Specification;

/**
 * Created by raniz on 2015-11-10.
 */
class S3UrlConnectionSpec extends Specification {

    AwsClientFactory<AmazonS3> clientFactory = Mock()

    AmazonS3 client = Mock()

    def "That simple bucket name works"() {
        given: "An URL"
        url = new URL(null, url, {})

        when: "A connection is created and connected"
        def connection = new S3UrlConnection(clientFactory, url)
        connection.connect()

        then: "No explicit region or endpoint is set and the correct key is requested"
        1 * clientFactory.create({ options ->
            assert options.region == (expectedRegion ? Region.getRegion(Regions.fromName(expectedRegion)) : null)
            assert !options.endpoint
            return true
        }) >> client
        1 * client.getObjectMetadata(expectedBucket, expectedKey)

        where:
        url                                     | expectedBucket       | expectedRegion | expectedKey
        "s3://simple/key"                       | "simple"             | null           | "key"
        "s3://bucket.with.dots/key"             | "bucket.with.dots"   | null           | "key"
        "s3://bucket.with.region.eu-west-1/key" | "bucket.with.region" | "eu-west-1"    | "key"
    }
}
