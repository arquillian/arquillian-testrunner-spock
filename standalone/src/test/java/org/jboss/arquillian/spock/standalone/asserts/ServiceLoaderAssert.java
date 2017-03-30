/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.jboss.arquillian.spock.standalone.asserts;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.ServiceLoader;

public class ServiceLoaderAssert<S> extends AbstractAssert<ServiceLoaderAssert<S>, ServiceLoader<S>> {

    private final ServiceLoader<S> serviceLoader;

    public ServiceLoaderAssert(ServiceLoader<S> serviceLoader) {
        super(serviceLoader, ServiceLoaderAssert.class);
        this.serviceLoader = serviceLoader;
    }

    public ServiceLoaderAssert<S> containsImplementation(Class<? extends S> serviceImplementation) {
        Assertions.assertThat(hasServiceImplementation(serviceImplementation)).isTrue();
        return this;
    }

    private boolean hasServiceImplementation(Class<? extends S> extensionClass) {
        for (S serviceImplementation : serviceLoader) {
            if (serviceImplementation.getClass().equals(extensionClass)) {
                return true;
            }
        }
        return false;
    }
}
