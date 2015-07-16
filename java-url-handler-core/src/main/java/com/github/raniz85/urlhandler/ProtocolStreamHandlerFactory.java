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

package com.github.raniz85.urlhandler;

import java.net.URLStreamHandler;

/**
 * Interface for factories that can create {@link URLStreamHandler}s for one or more protocols.
 * Created by raniz on 15/07/16.
 */
public interface ProtocolStreamHandlerFactory {

    /**
     * Return all supported protocols for this factory.
     * @return
     */
    String[] getSupportedProtocols();

    /**
     * Create a new {@link URLStreamHandler}
     * @param protocol The protocol to create the handler for
     * @return
     */
    URLStreamHandler createStreamHandler(String protocol);
}
