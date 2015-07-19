/*
 *    Copyright 2015 Daniel Raniz Raneland
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.raniz85.urlhandler

import spock.lang.Specification

/**
 * Created by raniz on 15/07/19.
 */
class PluggableUrlStreamHandlerFactorySpec extends Specification {

    def "That an unsupported protocol returns null"() {
        given: "No protocol factories"
        def factory = new PluggableUrlStreamHandlerFactory(null, null)

        when: "A stream handler for protocol 1 is requested"
        def createdHandler = factory.createURLStreamHandler("p")

        then: "Null is returned"
        createdHandler == null
    }

    def "That the correct protocol factory is used"() {
        given: "Two protocol factories"
        def protocol1 = Mock(ProtocolStreamHandlerFactory)
        def protocol2 = Mock(ProtocolStreamHandlerFactory)
        protocol1.getSupportedProtocols() >> ([ "p1" ] as String[])
        protocol2.getSupportedProtocols() >> ([ "p2" ] as String[])
        def handler1 = [ openConnection: { null } ] as URLStreamHandler
        def handler2 = [ openConnection: { null } ] as URLStreamHandler
        def factory = PluggableUrlStreamHandlerFactory.factories([protocol1, protocol2])

        when: "A stream handler for protocol 1 is requested"
        def createdHandler1 = factory.createURLStreamHandler("p1")
        def createdHandler2 = factory.createURLStreamHandler("p2")

        then: "The correct protocol factory is asked to create a stream handler"
        1 * protocol1.createStreamHandler("p1") >> handler1
        1 * protocol2.createStreamHandler("p2") >> handler2
        createdHandler1.is handler1
        createdHandler2.is handler2
    }

    def "That the fallback stream factory is used when no protocol matches"() {
        given: "One protocol factory and a fallback"
        def protocol1 = Mock(ProtocolStreamHandlerFactory)
        def fallback = Mock(URLStreamHandlerFactory)
        protocol1.getSupportedProtocols() >> ([ "p1" ] as String[])
        def handler1 = [ openConnection: { null } ] as URLStreamHandler
        def handler2 = [ openConnection: { null } ] as URLStreamHandler
        def factory = new PluggableUrlStreamHandlerFactory([protocol1], [fallback])

        when: "A stream handler for protocol 1 is requested"
        def createdHandler1 = factory.createURLStreamHandler("p1")
        def createdHandler2 = factory.createURLStreamHandler("p2")

        then: "The correct protocol factory is asked to create a stream handler"
        1 * protocol1.createStreamHandler("p1") >> handler1
        1 * fallback.createURLStreamHandler("p2") >> handler2
        createdHandler1.is handler1
        createdHandler2.is handler2
    }

    def "That multiple fallback stream factories are tried in order"() {
        given: "Two fallbacks"
        def fallback1 = Mock(URLStreamHandlerFactory)
        def fallback2 = Mock(URLStreamHandlerFactory)
        def handler = [ openConnection: { null } ] as URLStreamHandler
        def factory = PluggableUrlStreamHandlerFactory.fallbacks([fallback1, fallback2])

        when: "A stream handler for a protocol supported by only the second fallback is requested"
        def createdHandler = factory.createURLStreamHandler("p")

        then: "The correct protocol factory is asked to create a stream handler"
        1 * fallback1.createURLStreamHandler("p") >> null
        1 * fallback2.createURLStreamHandler("p") >> handler
        createdHandler.is handler
    }

    def "That adding a protocol factory works as expected"() {
        given: "One protocol factory that has not been added"
        def protocol = Mock(ProtocolStreamHandlerFactory)
        protocol.getSupportedProtocols() >> ([ "p" ] as String[])
        def handler = [ openConnection: { null } ] as URLStreamHandler
        def factory = new PluggableUrlStreamHandlerFactory()

        when: "The protocol factory is added and then a stream handler is requested"
        factory.addFactory protocol
        def createdHandler = factory.createURLStreamHandler("p")

        then: "The correct protocol factory is asked to create a stream handler"
        1 * protocol.createStreamHandler("p") >> handler
        createdHandler.is handler
    }

    def "That adding a fallback works as expected"() {
        given: "A fallback that has not been added"
        def fallback = Mock(URLStreamHandlerFactory)
        def handler = [ openConnection: { null } ] as URLStreamHandler
        def factory = new PluggableUrlStreamHandlerFactory()

        when: "The fallback is added and then a stream handler is requested"
        factory.addFallback fallback
        def createdHandler = factory.createURLStreamHandler("p")

        then: "The fallback is asked to created a stream handler"
        1 * fallback.createURLStreamHandler("p") >> handler
        createdHandler.is handler
    }
}
