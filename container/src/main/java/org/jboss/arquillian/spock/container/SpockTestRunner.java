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
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.spockframework.runtime.Sputnik;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.SpecInfo;

/**
 * SpockTestRunner
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class SpockTestRunner implements TestRunner
{

   private static final MethodInfo NOT_FOUND = new MethodInfo();

   /**
    * Overwrite to provide additional run listeners.
    */
   protected List<RunListener> getRunListeners()
   {
      return Collections.emptyList();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.TestRunner#execute(java.lang.Class, java.lang.String)
    */
   public TestResult execute(final Class<?> testClass, final String methodName)
   {

      final Sputnik runner = new Sputnik(testClass);
      try
      {
         runner.filter(new Filter()
         {

            private SpecInfo currentSpec;

            {
               try
               {
                  Method method = Sputnik.class.getDeclaredMethod("getSpec");
                  method.setAccessible(true);
                  currentSpec = (SpecInfo) method.invoke(runner);
               }
               catch (Exception e)
               {
                  throw new RuntimeException("Could not get SpecInfo from Sputnik Runner", e);
               }
            }

            @Override
            public boolean shouldRun(Description description)
            {
               MethodInfo featureMethod = findCorrespondingFeatureMethod(description.getMethodName());
               if (NOT_FOUND.equals(featureMethod))
               {
                  return false;
               }
               return methodName.equals(featureMethod.getReflection().getName());
            }

            @Override
            public String describe()
            {
               return "Filter Feature methods for Spock Framework";
            }

            private MethodInfo findCorrespondingFeatureMethod(String featureMethodName)
            {
               MethodInfo methodInfo = NOT_FOUND;
               for (FeatureInfo feature : currentSpec.getAllFeatures())
               {
                  MethodInfo featureMethod = feature.getFeatureMethod();
                  if (featureMethodName.equals(featureMethod.getName()))
                  {
                     methodInfo = featureMethod;
                     break;
                  }
               }
               return methodInfo;
            }

         });
      }
      catch (Exception e)
      {
         return new TestResult(Status.FAILED, e);
      }

      Result testResult = new Result();

      RunNotifier notifier = new RunNotifier();
      notifier.addFirstListener(testResult.createListener());

      for (RunListener listener : getRunListeners())
      {
         notifier.addListener(listener);
      }

      runner.run(notifier);

      return convertToTestResult(testResult);
   }

   /**
    * Convert a JUnit Result object to Arquillian TestResult
    *
    * @param result JUnit Test Run Result
    * @return The TestResult representation of the JUnit Result
    */
   private TestResult convertToTestResult(Result result)
   {
      Status status = Status.PASSED;
      Throwable throwable = null;

      if (result.getFailureCount() > 0)
      {
         status = Status.FAILED;
         throwable = result.getFailures().get(0).getException();
      }

      if (result.getIgnoreCount() > 0)
      {
         status = Status.SKIPPED;
      }

      return new TestResult(status, throwable);
   }
}
