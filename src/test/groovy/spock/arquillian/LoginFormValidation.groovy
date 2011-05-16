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
package spock.arquillian

import spock.lang.*
import javax.inject.Inject
import org.jboss.arquillian.api.Deployment
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.arquillian.framework.spock.SecureAccountService
import org.jboss.arquillian.framework.spock.AccountService
import org.jboss.arquillian.framework.spock.Account

class LoginFormValidation extends Specification {

    @Deployment
    def static JavaArchive "create deployment"() {
        return ShrinkWrap.create(WebArchive.class)......;
    }

    By LOGGED_IN = By.xpath("//li[contains(text(),'Welcome')]");
    By LOGGED_OUT = By.xpath("//li[contains(text(),'Goodbye')]");
 
    By USERNAME_FIELD = By.id("loginForm:username");
    By PASSWORD_FIELD = By.id("loginForm:password");
 
    By LOGIN_BUTTON = By.id("loginForm:login");
    By LOGOUT_BUTTON = By.id("loginForm:logout");
    
    @Selenium
    WebDriver driver; 
        
    def "should be possible to login"() {
        when:
        driver.get("http://localhost:8080/weld-login/home.jsf");

        driver.findElement(USERNAME_FIELD).sendKeys(username);
        driver.findElement(PASSWORD_FIELD).sendKeys(password);
        driver.findElement(LOGIN_BUTTON).submit();
        
        then:
        checkElementPresence(LOGGED_IN, "User should be logged in!");
        
        where:
        username <<         ["aslak",   "dan"]
        password <<         ["xxx",     "xxx"]
    }
}