/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.spock.ArquillianSputnik;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.spockframework.runtime.Sputnik;

import java.util.Collections;
import java.util.List;

import static org.jboss.arquillian.test.spi.TestResult.failed;

/**
 * Spock TestRunner
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @version $Revision: $
 */
public class SpockTestRunner implements TestRunner
{

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
   @Override
   public TestResult execute(final Class<?> testClass, final String methodName)
   {
      final Result testResult = new Result();

      try
      {
         final Sputnik spockRunner = new ArquillianSputnik(testClass, true);
         spockRunner.filter(new SpockSpecificationFilter(spockRunner, methodName));
         runTest(spockRunner, testResult);
      }
      catch (Exception e)
      {
         return failed(e);
      }

      return convertToTestResult(testResult);
   }

   public void runTest(final Sputnik spockRunner, final Result testResult)
   {
      final RunNotifier notifier = new RunNotifier();
      notifier.addFirstListener(testResult.createListener());

      for (RunListener listener : getRunListeners())
      {
         notifier.addListener(listener);
      }

      spockRunner.run(notifier);
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
