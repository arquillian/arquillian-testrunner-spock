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

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 *
 * @version $Revision: $
 */
public class SpockDeploymentAppenderTestCase
{

   @Test
   public void shouldAddSpockTestRunnerAsServiceProvider() throws Exception
   {
      // given
      SpockDeploymentAppender spockDeploymentAppender = new SpockDeploymentAppender();
      ArchivePath testRunnerSPI = ArchivePaths.create("/META-INF/services/org.jboss.arquillian.container.test.spi.TestRunner");

      // when
      Archive<?> archive = spockDeploymentAppender.createAuxiliaryArchive();

      // then
      Assert.assertTrue("Should have added Test Runner",
            archive.contains(testRunnerSPI));

      Assert.assertEquals("Should have registered Spock Test Runner as Arquillian Test Runner",
            "org.jboss.arquillian.spock.container.SpockTestRunner",
            getResourceContent(archive, testRunnerSPI));
   }

   @Test
   public void shouldAddGroovyObjectClass() throws Exception
   {
      // given
      SpockDeploymentAppender spockDeploymentAppender = new SpockDeploymentAppender();
      ArchivePath groovyObject = ArchivePaths.create("/groovy/lang/GroovyObject.class");

      // when
      Archive<?> archive = spockDeploymentAppender.createAuxiliaryArchive();

      // then
      Assert.assertTrue("Should have added GroovyObject",
            archive.contains(groovyObject));
   }

   // Private utility classes

   private String getResourceContent(Archive<?> archive, ArchivePath path)
   {
      final InputStream openStream = archive.get(path).getAsset().openStream();
      String content = "";
      try
      {
         content = new Scanner(openStream).useDelimiter("\\A").next();
         return content.trim();
      } 
      finally
      {
         try
         {
            openStream.close();
         } 
         catch (IOException ignore)
         {
            // NOOP
         }
      }
   }

}
