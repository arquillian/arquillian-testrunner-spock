/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.spock;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

/**
 * Interceptor to call the Arquillian Core
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianInterceptor extends AbstractMethodInterceptor {

    private final Logger log = Logger.getLogger(ArquillianInterceptor.class.getName());
    private TestRunnerAdaptor testRunner;

    ArquillianInterceptor() {}

    /* (non-Javadoc)
     * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptSetupSpecMethod(org.spockframework.runtime.extension.IMethodInvocation)
     */
    @Override
    public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
        if (invocation.getSpec().isBottomSpec()) {
            final Class<?> specClass = invocation.getSpec().getReflection();
            getTestRunner().beforeClass(specClass, new InvocationExecutor(invocation));
        }
    }

    /* (non-Javadoc)
     * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptCleanupSpecMethod(org.spockframework.runtime.extension.IMethodInvocation)
     */
    @Override
    public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        if (invocation.getSpec().isBottomSpec()) {
            final Class<?> specClass = invocation.getSpec().getReflection();
            getTestRunner().afterClass(specClass, new InvocationExecutor(invocation));
        }
    }

    /* (non-Javadoc)
     * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptSetupMethod(org.spockframework.runtime.extension.IMethodInvocation)
     */
    @Override
    public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        log.fine("before " + invocation.getFeature().getFeatureMethod().getReflection().getName());
        getTestRunner().before(invocation.getTarget(), invocation.getFeature().getFeatureMethod().getReflection(),
            new InvocationExecutor(invocation));
    }

    /* (non-Javadoc)
     * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptCleanupMethod(org.spockframework.runtime.extension.IMethodInvocation)
     */
    @Override
    public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        log.fine("after " + invocation.getFeature().getFeatureMethod().getReflection().getName());
        getTestRunner().after(invocation.getTarget(), invocation.getFeature().getFeatureMethod().getReflection(),
            new InvocationExecutor(invocation));
    }

    /* (non-Javadoc)
     * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptFeatureMethod(org.spockframework.runtime.extension.IMethodInvocation)
     */
    @Override
    public void interceptFeatureMethod(final IMethodInvocation invocation) throws Throwable {
        TestResult result = getTestRunner().test(new TestMethodExecutor() {
            @Override
            public Method getMethod() {
                return invocation.getFeature().getFeatureMethod().getReflection();
            }

            @Override
            public Object getInstance() {
                return invocation.getTarget();
            }

            @Override
            public void invoke(Object... parameters) throws Throwable {
                invocation.proceed();
            }
        });

        if (result.getThrowable() != null) {
            throw result.getThrowable();
        }
    }

    private static class InvocationExecutor implements LifecycleMethodExecutor {

        private IMethodInvocation invocation;

        public InvocationExecutor(IMethodInvocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public void invoke() throws Throwable {
            invocation.proceed();
        }
    }

    private TestRunnerAdaptor getTestRunner() {
        if (this.testRunner == null) {
            this.testRunner = State.getTestAdaptor();
        }

        if (this.testRunner == null) {
            throw new IllegalStateException(
                "Unable to run Arquillian Spock test without TestRunnerAdaptor instantiated. Likely you forgot to annotate the specification with @RunWith(ArquillianSputnik)");
        }

        return testRunner;
    }
}
