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
package org.jboss.arquillian.ftest.spock

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.runner.RunWith
import spock.lang.Specification

import javax.inject.Inject

@RunWith(ArquillianSputnik)
class AccountServiceSpecification extends Specification {

    @Deployment(name = "concrete")
    def static JavaArchive "create deployment"() {
        return ShrinkWrap.create(JavaArchive.class)
                         .addClasses(AccountService.class, Account.class, SecureAccountService.class, TransactionCounter.class, TransferEvent.class)
                         .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    AccountService service

    @Inject
    TransactionCounter transactionCounter

    def setup() {
        transactionCounter.defineLimit(2)
    }

    def "transfer should be possible between two accounts"() {
        given:
            def from = new Account(100)
            def to = new Account(50)
            def amount = 50
        when:
            service.transfer(from, to, amount)

        then:
            from.balance == 50
            to.balance == 100
    }

}
