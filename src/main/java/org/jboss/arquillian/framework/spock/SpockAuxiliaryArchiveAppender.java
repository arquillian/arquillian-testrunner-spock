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

import groovy.beans.Bindable;
import groovy.grape.Grape;
import groovy.inspect.Inspector;
import groovy.io.FileType;
import groovy.jmx.builder.JmxBeanExportFactory;
import groovy.lang.GroovyObject;
import groovy.mock.interceptor.CallSpec;
import groovy.model.ClosureModel;
import groovy.security.GroovyCodeSourcePermission;
import groovy.servlet.AbstractHttpServlet;
import groovy.sql.DataSet;
import groovy.swing.SwingBuilder;
import groovy.text.Template;
import groovy.time.Duration;
import groovy.util.AbstractFactory;
import groovy.xml.DOMBuilder;
import groovyjarjarantlr.build.ANTLR;
import groovyjarjarasm.asm.Type;
import groovyjarjarcommonscli.BasicParser;

import javax.script.ScriptEngineFactory;

import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.hamcrest.BaseDescription;
import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.TestRunner;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.spockframework.builder.BuilderHelper;
import org.spockframework.compiler.SourceLookup;
import org.spockframework.experimental.RunStatus2;
import org.spockframework.gentyref.CaptureType;
import org.spockframework.mock.IMockFactory;
import org.spockframework.runtime.Sputnik;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.util.SpockReleaseInfo;

import spock.config.ConfigurationException;
import spock.lang.Ignore;
import spock.util.concurrent.AsyncConditions;

/**
 * SpockAuxiliaryArchiveAppender
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class SpockAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.AuxiliaryArchiveAppender#createAuxiliaryArchive()
    */
   @Override
   public Archive<?> createAuxiliaryArchive()
   {
      return ShrinkWrap.create(JavaArchive.class, "arquillian-spock.jar")
                .addPackages(
                     true,
                     Filters.exclude(".*/package-info.*"),
                     "org.spockframework",
                     "spock",
                     "groovy",
                     "org.codehaus.groovy",
                     "groovyjarjarantlr",
                     "groovyjarjarasm.asm",
                     "groovyjarjarcommonscli")
                .addPackages( // junit
                      true,
                      Filters.includeAll(),
                      "org.junit",
                      "org.hamcrest")
               .addServiceProvider(ScriptEngineFactory.class, GroovyScriptEngineFactory.class)
               .addServiceProvider(TestRunner.class, SpockTestRunner.class)
               .addServiceProvider(IGlobalExtension.class, ArquillianExtension.class)
               .addClasses(SpockTestRunner.class, ArquillianExtension.class, ArquillianInterceptor.class)
               .addManifestResource("META-INF/dgminfo", "dgminfo")
               .addManifestResource("META-INF/groovy-release-info.properties", "groovy-release-info.properties");
   }

}
