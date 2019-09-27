/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
class ParametrizedAccountServiceSpecification extends Specification {

    @Deployment(name = "abstract")
    static JavaArchive "create deployment"() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(AccountService.class, Account.class, SecureAccountService.class, TransactionCounter.class, TransferEvent.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
    }

    @Inject
    AccountService service

    @Inject
    TransactionCounter globalTransactionCounter

    def setup() {
        // This has been introduce to ensure we execute the tests once and only once, as previously it was n^2
        // @see https://github.com/arquillian/arquillian-testrunner-spock/issues/17
        globalTransactionCounter.defineLimit(4)
    }

    def "transfer should be possible between two accounts"() {
        when:
        service.transfer(from, to, amount)

        then:
        from.balance == fromBalance
        to.balance == toBalance
        globalTransactionCounter.belowLimit()

        where:
        from << [
            new Account(100),
            new Account(10)
        ]
        to << [
            new Account(50),
            new Account(90)
        ]
        amount << [50, 10]
        fromBalance << [50, 0]
        toBalance << [100, 100]
    }

    def "transferring between accounts should result in account withdrawal and deposit"() {
        when:
        service.transfer(from, to, amount)

        then:
        from.balance == fromBalance
        to.balance == toBalance
        globalTransactionCounter.belowLimit()

        where:
        from << [
            new Account(100),
            new Account(10)
        ]
        to << [
            new Account(50),
            new Account(90)
        ]
        amount << [100, 5]
        fromBalance << [0, 5]
        toBalance << [150, 95]
    }


}
