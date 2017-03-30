/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.spock.container;

import java.lang.reflect.Method;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.spockframework.runtime.Sputnik;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;

/**
 * JUnit filter for Spock spefications.
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @version $Revision: $
 */
final class SpockSpecificationFilter extends Filter {
    private static final MethodInfo NOT_FOUND = new MethodInfo();

    private final Sputnik spockRunner;

    private final String methodName;

    private SpecInfo currentSpec;

    SpockSpecificationFilter(Sputnik spockRunner, String methodName) {
        this.spockRunner = spockRunner;
        this.methodName = methodName;
        obtainCurrentSpecification();
    }

    @Override
    public boolean shouldRun(Description description) {
        final MethodInfo featureMethod = findCorrespondingFeatureMethod(description.getMethodName());
        if (NOT_FOUND.equals(featureMethod)) {
            return false;
        }
        return methodName.equals(featureMethod.getReflection().getName());
    }

    @Override
    public String describe() {
        return "Filter Feature methods for Spock Framework";
    }

    private void obtainCurrentSpecification() {
        try {
            Method method = Sputnik.class.getDeclaredMethod("getSpec");
            method.setAccessible(true);
            currentSpec = (SpecInfo) method.invoke(spockRunner);
        } catch (Exception e) {
            throw new RuntimeException("Could not obtain SpecInfo from Sputnik Runner", e);
        }
    }

    private MethodInfo findCorrespondingFeatureMethod(String featureMethodName) {
        MethodInfo methodInfo = NOT_FOUND;
        for (FeatureInfo feature : currentSpec.getAllFeatures()) {
            MethodInfo featureMethod = feature.getFeatureMethod();
            if (featureMethodName.equals(featureMethod.getName())) {
                methodInfo = featureMethod;
                break;
            }
        }
        return methodInfo;
    }
}