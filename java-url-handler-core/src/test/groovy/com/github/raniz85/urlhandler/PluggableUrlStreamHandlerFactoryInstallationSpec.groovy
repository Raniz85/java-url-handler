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
class PluggableUrlStreamHandlerFactoryInstallationSpec extends Specification {

    def cleanup() {
        clearFactory()
    }

    def "That stream handler factory can be installed normally"() {
        given: "A stream handler factory"
        def factory = new PluggableUrlStreamHandlerFactory()

        when: "The factory is installed"
        PluggableUrlStreamHandlerFactory.tryInstallDefault factory

        then: "No exception is thrown and the factory has been installed"
        noExceptionThrown()
        getFactory() == factory
    }

    def "That stream handler factory can be installed via reflection"() {
        given: "An installed URL stream handler and a plugable stream handler factory"
        def originalFactory = [ createURLStreamHandler: { null } ] as URLStreamHandlerFactory
        URL.setURLStreamHandlerFactory originalFactory
        def factory = new PluggableUrlStreamHandlerFactory()

        when: "The factory is installed"
        PluggableUrlStreamHandlerFactory.tryInstallWithReflection factory

        then: "No exception is thrown, the factory has been installed an the original factory is a fallback"
        noExceptionThrown()
        getFactory() == factory
        originalFactory in factory.fallbacks
    }

    def getFactory() { URL.@factory }

    def clearFactory() { URL.@factory = null }

}