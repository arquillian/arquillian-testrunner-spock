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

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spock.ArquillianSpockExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import javax.script.ScriptEngineFactory;

/**
 * Creates testing archive with dependencies required
 * to run Spock Framework tests with Arquillian.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 *
 * @version $Revision: $
 */
public class SpockDeploymentAppender implements AuxiliaryArchiveAppender
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.AuxiliaryArchiveAppender#createAuxiliaryArchive()
    */
   public Archive<?> createAuxiliaryArchive()
   {
      return ShrinkWrap.create(JavaArchive.class, "arquillian-spock.jar")
                       .addPackages(
                               true,
                               Filters.exclude(".*/package-info.*"),
                               "groovy",
                               "groovyjarjarantlr",
                               "groovyjarjarasm.asm",
                               "groovyjarjarcommonscli",
                               "org.codehaus.groovy",
                               "spock",
                               "org.spockframework",
                               "org.objectweb.asm",
                               ArquillianSpockExtension.class.getPackage().getName())
                       .addPackages( // junit
                               true,
                               Filters.includeAll(),
                               "org.junit",
                               "org.hamcrest")
                       .addPackages(true, ArquillianSpockExtension.class.getPackage())
                       .addAsServiceProvider(TestRunner.class, SpockTestRunner.class)
                       .addAsServiceProvider(ScriptEngineFactory.class, GroovyScriptEngineFactory.class)
                       .addClass(SpockSpecificationFilter.class)
                       .addAsManifestResource("META-INF/dgminfo", "dgminfo")
                       .addAsManifestResource("META-INF/groovy-release-info.properties", "groovy-release-info.properties");
   }

}
