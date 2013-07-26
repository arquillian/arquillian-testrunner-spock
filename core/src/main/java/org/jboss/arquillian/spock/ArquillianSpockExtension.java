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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

   private final Set<SpecInfo> arquillainEnabledSpecifications = Collections.synchronizedSet(new HashSet<SpecInfo>());

   @Override
   public void visitSpecAnnotation(ArquillianSpecification annotation, SpecInfo spec)
   {
      arquillainEnabledSpecifications.add(spec);
   }

   @Override
   public void visitSpec(SpecInfo spec)
   {

      if (!arquillainEnabledSpecifications.contains(spec))
      {
         // ignore spec if it is not annotated to run with @ArquillianSpecification
         return;
      }

      if (!State.hasTestAdaptor())
      {
         throw new IllegalStateException(
               "Unable to run Arquillian Spock test without TestRunnerAdaptor instantiated. Likely you forgot to annotate the specification with @RunWith(ArquillianSputnik.class)");
      }

      final ArquillianInterceptor interceptor = new ArquillianInterceptor(State.getTestAdaptor());
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
