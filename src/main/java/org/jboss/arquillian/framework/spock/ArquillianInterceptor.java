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
package org.jboss.arquillian.framework.spock;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestRunnerAdaptor;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

/**
 * Interceptor to call the Arquillian Core
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ArquillianInterceptor extends AbstractMethodInterceptor
{
   private Logger log = Logger.getLogger(ArquillianInterceptor.class.getName());
   
   private TestRunnerAdaptor testRunner;
   
   public ArquillianInterceptor(final TestRunnerAdaptor testRunner)
   {
      this.testRunner = testRunner;
   }
   
   /* (non-Javadoc)
    * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptSetupSpecMethod(org.spockframework.runtime.extension.IMethodInvocation)
    */
   @Override
   public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable
   {
      log.fine("beforeClass " + invocation.getSpec().getReflection().getName());
      testRunner.beforeClass(invocation.getSpec().getReflection());
      invocation.proceed();
   }
   
   /* (non-Javadoc)
    * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptCleanupSpecMethod(org.spockframework.runtime.extension.IMethodInvocation)
    */
   @Override
   public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable
   {
      log.fine("afterClass " + invocation.getSpec().getReflection().getName());
      invocation.proceed();
      testRunner.afterClass(invocation.getSpec().getReflection());
   }
   
   /* (non-Javadoc)
    * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptSetupMethod(org.spockframework.runtime.extension.IMethodInvocation)
    */
   @Override
   public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable
   {
      log.fine("before " + invocation.getFeature().getFeatureMethod().getReflection().getName());
      testRunner.before(invocation.getTarget(), invocation.getFeature().getFeatureMethod().getReflection());
      invocation.proceed();
   }
   
   /* (non-Javadoc)
    * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptCleanupMethod(org.spockframework.runtime.extension.IMethodInvocation)
    */
   @Override
   public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable
   {
      log.fine("after " + invocation.getFeature().getFeatureMethod().getReflection().getName());
      invocation.proceed();
      testRunner.after(invocation.getTarget(), invocation.getFeature().getFeatureMethod().getReflection());
   }
   
   /* (non-Javadoc)
    * @see org.spockframework.runtime.extension.AbstractMethodInterceptor#interceptFeatureMethod(org.spockframework.runtime.extension.IMethodInvocation)
    */
   @Override
   public void interceptFeatureMethod(final IMethodInvocation invocation) throws Throwable
   {
      log.fine("invoke " + invocation.getFeature().getFeatureMethod().getReflection().getName());
      TestResult result = testRunner.test(new TestMethodExecutor()
      {
         public void invoke() throws Throwable
         {
            invocation.proceed();
         }
         
         public Method getMethod()
         {
            return invocation.getMethod().getReflection();
         }
         
         public Object getInstance()
         {
            return invocation.getTarget();
         }
      });
      if(result.getThrowable() != null)
      {
         throw result.getThrowable();
      }
   }
}