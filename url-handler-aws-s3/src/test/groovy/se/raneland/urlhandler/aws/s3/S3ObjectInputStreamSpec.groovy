package se.raneland.urlhandler.aws.s3

import com.amazonaws.services.s3.model.S3Object
import spock.lang.Specification

/**
 * Created by raniz on 2015-08-26.
 */
class S3ObjectInputStreamSpec extends Specification {

    def "That both the InputStream and S3Object are closed"() {
        given: "An S3ObjectInputStream with an underlying S3Object"
        def stream = Mock(com.amazonaws.services.s3.model.S3ObjectInputStream)
        def object = Mock(S3Object)
        object.getObjectContent() >> stream
        def s3Stream = new S3ObjectInputStream(object)

        when: "The stream is closed"
        s3Stream.close()

        then: "Both the underlying stream and object are closed"
        1 * stream.close()
        1 * object.close()
    }

    def "That methods are delegated to the underlying stream"() {
        given: "An S3ObjectInputStream with an underlying S3Object"
        def stream = Mock(com.amazonaws.services.s3.model.S3ObjectInputStream)
        def object = Mock(S3Object)
        object.getObjectContent() >> stream
        def s3Stream = new S3ObjectInputStream(object)

        when: "A method is called on the stream"
        if(arguments) {
            s3Stream."${method}"(*arguments)
        } else {
            s3Stream."${method}"()
        }

        then: "The same method is called on the underlying object"
        if(arguments) {
            1 * stream."${method}"(*arguments)
        } else {
            1 * stream."${method}"()
        }

        where:
        method << [
            "read",
            "available",
            "read"
        ]
        arguments << [
            null,
            null,
            [new byte[10]]

        ]
    }
}
