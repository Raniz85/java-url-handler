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

package se.raneland.urlhandler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link URLStreamHandlerFactory} that supports adding and removing both {@link ProtocolStreamHandlerFactory}s and
 * fallback {@code URLStreamHandlerFactory}s during runtime.
 *
 * Created by raniz on 15/07/16.
 */
public class PluggableUrlStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * Try to install a {@link PluggableUrlStreamHandlerFactory} on the JVM.
     * First attempts to install it using Tomcat's URLStreamHandlerFactory, then
     * by using {@link URL#setURLStreamHandlerFactory(URLStreamHandlerFactory)}, and if that fails
     * by replacing the currently installed factory using reflection and adding the existing factory as a
     * fallback.
     *
     * @param factory The factory to install
     * @return If installation was successful or not
     */
    public static boolean tryInstall(PluggableUrlStreamHandlerFactory factory) {
        if(tryInstallWithTomcat(factory)) {
            return true;
        }
        if(tryInstallDefault(factory)) {
            return true;
        }
        if(tryInstallWithReflection(factory)) {
            return true;
        }
        return false;
    }

    /**
     * Try to install a {@link PluggableUrlStreamHandlerFactory} using the url stream handler factory in the Tomcat
     * server.
     *
     * Tomcat requires it's own {@code TomcatURLStreamHandlerFactory} to be installed as the system stream handler
     * factory. It does support user stream handler factories though so we can install ourselves as one of those.
     *
     * This method does nothing if the {@code TomcatURLStreamHandlerFactory} class can't be loaded.
     *
     * @param factory The factory to install
     *
     * @return If installation was successful or not
     */
    public static boolean tryInstallWithTomcat(PluggableUrlStreamHandlerFactory factory) {
        try {
            // Check if the tomcat stream handler factory is on the classpath
            Class<?> cls = Class.forName("org.apache.catalina.webresources.TomcatURLStreamHandlerFactory");

            // Get the singleton instance
            Method getInstance = cls.getDeclaredMethod("getInstance");
            Object tomcatStreamHandler = getInstance.invoke(null);

            // Install the factory on the tomcat stream handler factory as a user factory
            Method addUserFactory = cls.getDeclaredMethod("getUserFactory", URLStreamHandlerFactory.class);
            addUserFactory.invoke(tomcatStreamHandler, factory);
            return true;
        } catch(ClassNotFoundException e) {
            // Tomcat is not present, this is OK
        } catch (NoSuchMethodException e) {
            // Tomcat is defunct
            // TODO: Warn the user in some way
        } catch (InvocationTargetException e) {
            // Tomcat is defunct
        } catch (IllegalAccessException e) {
            // Tomcat is defunct
        }
        return false;
    }

    /**
     * Try to install a {@link PluggableUrlStreamHandlerFactory} using
     * {@link URL#setURLStreamHandlerFactory(URLStreamHandlerFactory)}.
     *
     * This method catches the {@link Error} thrown if the stream handler factory is already set. To not catch
     * really fatal error (the stream handler factory already being set really isn't an unrecoverable error) and checks
     * the message. If the message is one of the expected ones false is returned, otherwise the error is propagated.
     *
     * Supported errors messages:
     * <ul>
     *     <li>factory already defined</li>
     * </ul>
     * If you happen upon another error message you will have to catch the {@link Error} yourself. If you do so, please
     * file an issue so it can be added here.
     *
     * @param factory The factory to install
     * @return If installation was successful or not
     */
    public static boolean tryInstallDefault(PluggableUrlStreamHandlerFactory factory) {
        try {
            URL.setURLStreamHandlerFactory(factory);
            return true;
        } catch(Error e) {
            if("factory already defined".equals(e.getMessage())) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Try to install a {@link PluggableUrlStreamHandlerFactory} using reflection to directly access the private
     * {@code factory} field of {@link URL}.
     *
     * This method retrieves the current value of {@code URL.factory} and then replaces it with the provided factory.
     * If the current stream handler factory is non-null it is added as a fallback.
     *
     * @param factory The factory to install
     * @return If installation was successful or not
     */
    public static boolean tryInstallWithReflection(PluggableUrlStreamHandlerFactory factory) {
        try {
            Field factoryField = URL.class.getDeclaredField("factory");
            factoryField.setAccessible(true);
            URLStreamHandlerFactory oldFactory = (URLStreamHandlerFactory) factoryField.get(null);
            factoryField.set(null, factory);
            if(oldFactory != null) {
                factory.addFallback(oldFactory);
            }
            return true;
        } catch (NoSuchFieldException e) {
            // URL doesn't have that field
        } catch (IllegalAccessException e) {
            // We're not permitted to mess with the field
        }
        return false;
    }

    private final ConcurrentHashMap<String, ProtocolStreamHandlerFactory> factories;

    private final List<URLStreamHandlerFactory> fallbacks;

    /**
     * Create a new factory with a set of fallbacks and no factories.
     *
     * @param fallbacks The {@link URLStreamHandlerFactory}s to use
     */
    public static PluggableUrlStreamHandlerFactory fallbacks(Collection<URLStreamHandlerFactory> fallbacks) {
        return new PluggableUrlStreamHandlerFactory(null, fallbacks);
    }

    /**
     * Create a new factory with a set of factories and no fallbacks.
     *
     * @param factories The {@link ProtocolStreamHandlerFactory}s to use
     */
    public static PluggableUrlStreamHandlerFactory factories(Collection<ProtocolStreamHandlerFactory> factories) {
        return new PluggableUrlStreamHandlerFactory(factories, null);
    }

    /**
     * Create a new factory with no protocol factories or fallbacks.
     * Don't forget to add protocol factories and fallbacks with
     * {@link PluggableUrlStreamHandlerFactory#addFactory(ProtocolStreamHandlerFactory)}
     * and {@link PluggableUrlStreamHandlerFactory#addFallback(URLStreamHandlerFactory)}.
     *
     */
    public PluggableUrlStreamHandlerFactory() {
        this(null, null);
    }

    /**
     * Create a new factory with a set of factories and fallbacks.
     *
     * @param factories The {@link ProtocolStreamHandlerFactory}s to use
     * @param fallbacks The {@link URLStreamHandlerFactory}s to fallback to when no {@link ProtocolStreamHandlerFactory} applies
     */
    public PluggableUrlStreamHandlerFactory(Collection<ProtocolStreamHandlerFactory> factories, Collection<URLStreamHandlerFactory> fallbacks) {
        this.factories = new ConcurrentHashMap<String, ProtocolStreamHandlerFactory>();
        this.fallbacks = new ArrayList<URLStreamHandlerFactory>();

        // Add all factories
        if(factories != null) {
            for (ProtocolStreamHandlerFactory factory : factories) {
                if(factory == null) {
                    throw new IllegalArgumentException("Factory may not be null");
                }
                for (String protocol : factory.getSupportedProtocols()) {
                    this.factories.put(protocol.toLowerCase(), factory);
                }
            }
        }

        // Add all fallbacks
        if(fallbacks != null) {
            for(URLStreamHandlerFactory fallback : fallbacks) {
                if(fallback == null) {
                    throw new IllegalArgumentException("Fallback may not be null");
                }
                this.fallbacks.add(fallback);
            }
        }
    }

    /**
     * Add a {@link ProtocolStreamHandlerFactory}.
     * @param factory
     */
    void addFactory(ProtocolStreamHandlerFactory factory) {
        if(factory == null) {
            throw new IllegalArgumentException("Factory may not be null");
        }
        for (String protocol : factory.getSupportedProtocols()) {
            this.factories.put(protocol.toLowerCase(), factory);
        }
    }

    /**
     * Add a {@link URLStreamHandlerFactory} as a fallback.
     * @param fallback
     */
    void addFallback(URLStreamHandlerFactory fallback) {
        if(fallback == null) {
            throw new IllegalArgumentException("Fallback may not be null");
        }
        this.fallbacks.add(fallback);
    }

    /**
     * Checks if any added {@link ProtocolStreamHandlerFactory} supports the requested protocol
     * and if so delegates to it, otherwise goes through all fallback {@link URLStreamHandlerFactory}s and
     * returns the first non-null {@link URLStreamHandler}.
     *
     * @param protocol The protocol to create a stream handler for
     * @return A stream handler or null if none could be created
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        ProtocolStreamHandlerFactory factory = factories.get(protocol.toLowerCase());
        if(factory != null) {
            return factory.createStreamHandler(protocol.toLowerCase());
        }
        for(URLStreamHandlerFactory fallback : fallbacks) {
            URLStreamHandler handler = fallback.createURLStreamHandler(protocol);
            if(handler != null) {
                return handler;
            }
        }
        return null;
    }

    public HashMap<String, ProtocolStreamHandlerFactory> getFactories() {
        return new HashMap<String, ProtocolStreamHandlerFactory>(factories);
    }

    public List<URLStreamHandlerFactory> getFallbacks() {
        return new ArrayList<URLStreamHandlerFactory>(fallbacks);
    }
}
