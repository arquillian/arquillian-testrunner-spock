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
package org.jboss.arquillian.spock;

import java.util.logging.Logger;

import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.spockframework.runtime.AbstractRunListener;
import org.spockframework.runtime.SpockExecutionException;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.NotThreadSafe;

/**
 * Arquillian extension to the Spock test framework.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @version $Revision: $
 */
@NotThreadSafe
public class ArquillianSpockExtension extends AbstractAnnotationDrivenExtension<ArquillianSpecification>
{
   private TestRunnerAdaptor deployableTest;

   private SpecInfo lastCreatedSpec;

   private Logger log = Logger.getLogger(ArquillianSpockExtension.class.getName());

   @Override
   public void visitSpecAnnotation(ArquillianSpecification annotation, SpecInfo spec)
   {
      initalizeTestAdaptor();
   }

   @Override
   public void visitSpec(SpecInfo spec)
   {

      ArquillianInterceptor interceptor = new ArquillianInterceptor(deployableTest);

      // adding interceptors to all specs, ARQ-1427
      for (SpecInfo s : spec.getSpecsBottomToTop()) {
         // add Interceptors to all feature methods
         for (FeatureInfo feature : s.getAllFeatures())
         {
            feature.getFeatureMethod().addInterceptor(interceptor);
         }
         s.getSetupSpecMethod().addInterceptor(interceptor);
         s.getSetupMethod().addInterceptor(interceptor);
         s.getCleanupMethod().addInterceptor(interceptor);
         s.getCleanupSpecMethod().addInterceptor(interceptor);
      }

      final SpecInfo topSpec = spec.getTopSpec();

      // set the last created Spec, so we can call AfterSuite only when this is done.
      lastCreatedSpec = topSpec;
      topSpec.addListener(new AbstractRunListener()
      {
         @Override
         public void afterSpec(SpecInfo spec)
         {
            if(spec == lastCreatedSpec)
            {
               try
               {
                  log.fine("afterSuite");
                  deployableTest.afterSuite();
                  deployableTest = null;
               }
               catch (Exception e)
               {
                  throw new SpockExecutionException("Unable to add ArquillianSpecification listener", e);
               }
            }
         }
      });
   }

   private void initalizeTestAdaptor()
   {
      if (deployableTest == null)
      {
         final TestRunnerAdaptor adaptor = TestRunnerAdaptorBuilder.build();
         try
         {
            log.fine("beforeSuite");
            // don't set it if beforeSuite fails
            adaptor.beforeSuite();
            deployableTest = adaptor;
         }
         catch (Exception e)
         {
            throw new SpockExecutionException("Unable to hook Arquillian Spock test adaptor", e);
         }
      }
   }

}
