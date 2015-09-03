# Java URL Handler [![Build Status](https://travis-ci.org/Raniz85/java-url-handler.svg?branch=master)](https://travis-ci.org/Raniz85/java-url-handler)

Library for managing URL handlers in Java

Currently only URLs for Amazon S3 are supported on the format: `s3://bucket/path/to/key`.

Region or endpoint can be specified by adding it after the bucket like so: `s3://bucket.region-or.endpoint/path/to/key`.

Authorization will use the system default but can be overriden by specifying either profile name or key pair:

* `s3://profile@bucket/path/to/key`
* `s3://accessKey:secretKey@bucket/path/to/key`


## Usage:

### Gradle:

```groovy
repositories {
    jcenter
}
    
dependencies {
    compile "se.raneland.java-url-handler:java-url-handler-aws-s3:1.0.RELEASE"
}
```

### Java:

```java
public static void main(String[] args) {
    PluggableUrlStreamHandlerFactory.tryInstall(PluggableUrlStreamHandlerFactory.factories(Arrays.asList(
            new S3ProtocolStreamHandlerFactory()
    )));
    new URL("s3://my-bucket/path/to/key").openStream();
}
```
