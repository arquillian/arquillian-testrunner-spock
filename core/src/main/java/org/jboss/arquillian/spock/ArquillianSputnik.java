/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
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
 *
 * Copyright 2009 the original author or authors.
 *
 */

package org.jboss.arquillian.spock;

import java.util.Collection;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.spockframework.runtime.JUnitDescriptionGenerator;
import org.spockframework.runtime.JUnitFilterAdapter;
import org.spockframework.runtime.JUnitSorterAdapter;
import org.spockframework.runtime.RunContext;
import org.spockframework.runtime.SpecInfoBuilder;
import org.spockframework.runtime.Sputnik;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author Peter Niederwieser
 *
 * Extension to Sputnik class that allows to mimic Before and After Suite events.
 * The original runner is copied as we need access to getSpec() method, which is private in original Sputnik class
 *
 */
public class ArquillianSputnik extends Sputnik
{

   private final Class<?> clazz;

   private SpecInfo spec;

   private boolean extensionsRun = false;

   private boolean descriptionAggregated = false;

   public ArquillianSputnik(Class<?> clazz) throws InitializationError
   {
      super(clazz);
      // clazz is private field, we're actually shading it
      this.clazz=clazz;
      State.runnerStarted();
   }

   @Override
   public void run(RunNotifier notifier)
   {
      // first time we're being initialized
      if (!State.hasTestAdaptor())
      {
         // no, initialization has been attempted before and failed, refuse to do anything else
         if (State.hasInitializationException())
         {
            // failed on suite level, ignore children
            // notifier.fireTestIgnored(getDescription());
            notifier.fireTestFailure(new Failure(getDescription(), new RuntimeException(
                  "Arquillian has previously been attempted initialized, but failed. See cause for previous exception",
                  State.getInitializationException())));
         }
         else
         {
            final TestRunnerAdaptor adaptor = TestRunnerAdaptorBuilder.build();
            try
            {
               // don't set it if beforeSuite fails
               adaptor.beforeSuite();
               State.testAdaptor(adaptor);
            }
            catch (Exception e)
            {
               // caught exception during BeforeSuite, mark this as failed
               State.caughtInitializationException(e);
               notifier.fireTestFailure(new Failure(getDescription(), e));
            }
         }
      }

      notifier.addListener(new RunListener()
      {
         @Override
         public void testRunFinished(Result result) throws Exception
         {
            State.runnerFinished();
            shutdown();
         }

         private void shutdown()
         {
            try
            {
               if (State.isLastRunner())
               {
                  try
                  {
                     if (State.hasTestAdaptor())
                     {
                        TestRunnerAdaptor adaptor = State.getTestAdaptor();
                        adaptor.afterSuite();
                        adaptor.shutdown();
                     }
                  }
                  finally
                  {
                     State.clean();
                  }
               }
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not run @AfterSuite", e);
            }
         }
      });

      // initialization ok, run children
      if (State.hasTestAdaptor())
      {
          runExtensionsIfNecessary();
          aggregateDescriptionIfNecessary();
          RunContext.get().createSpecRunner(getSpec(), notifier).run();
      }
   }

   @Override
   public Description getDescription()
   {
      runExtensionsIfNecessary();
      aggregateDescriptionIfNecessary();
      return getSpec().getDescription();
   }

   @Override
   public void filter(Filter filter) throws NoTestsRemainException
   {
      getSpec().filterFeatures(new JUnitFilterAdapter(filter));
      if (allFeaturesExcluded())
      {
         throw new NoTestsRemainException();
      }
   }

   @Override
   public void sort(Sorter sorter)
   {
      getSpec().sortFeatures(new JUnitSorterAdapter(sorter));
   }

   private SpecInfo getSpec()
   {
     if (spec == null)
     {
       spec = new SpecInfoBuilder(clazz).build();
       new JUnitDescriptionGenerator(spec).attach();
       enrichSpecWithArquillian(spec);
     }
     return spec;
   }

   private void runExtensionsIfNecessary()
   {
     if (extensionsRun) return;
     RunContext.get().createExtensionRunner(getSpec()).run();
     extensionsRun = true;
   }

   private void aggregateDescriptionIfNecessary() {
     if (descriptionAggregated) return;
     new JUnitDescriptionGenerator(getSpec()).aggregate();
     descriptionAggregated = true;
   }

   private boolean allFeaturesExcluded() {
     for (FeatureInfo feature: getSpec().getAllFeatures())
       if (!feature.isExcluded()) return false;

     return true;
   }

   private void enrichSpecWithArquillian(SpecInfo spec)
   {
      final ArquillianInterceptor interceptor = new ArquillianInterceptor();
      for (SpecInfo s : spec.getSpecsBottomToTop())
      {
         interceptLifecycleMethods(s, interceptor);
         interceptAllFeatures(s.getAllFeatures(), interceptor);
      }
   }

   private void interceptLifecycleMethods(final SpecInfo specInfo, final ArquillianInterceptor interceptor)
   {
      specInfo.getSetupSpecMethod().addInterceptor(interceptor);
      specInfo.getSetupMethod().addInterceptor(interceptor);
      specInfo.getCleanupMethod().addInterceptor(interceptor);
      specInfo.getCleanupSpecMethod().addInterceptor(interceptor);
   }

   private void interceptAllFeatures(final Collection<FeatureInfo> features, final ArquillianInterceptor interceptor)
   {
      for (FeatureInfo feature : features)
      {
         feature.getFeatureMethod().addInterceptor(interceptor);
      }
   }
}
