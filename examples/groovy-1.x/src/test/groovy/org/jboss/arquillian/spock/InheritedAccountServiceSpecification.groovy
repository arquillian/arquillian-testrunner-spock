package org.jboss.arquillian.spock

import javax.inject.Inject

import org.jboss.arquillian.spock.Account
import org.jboss.arquillian.spock.AccountService
import org.jboss.arquillian.spock.common.AbstractCommonSpecification;
import org.junit.runner.RunWith;

@ArquillianSpecification
@RunWith(ArquillianSputnik.class)
class InheritedAccountServiceSpecification extends AbstractCommonSpecification {

    @Inject
    AccountService service

    def setup() {
        assert service != null
    }

    def "transfer should be possible between two accounts"() {
        when:
        service.transfer(from, to, amount)

        then:
        from.balance == fromBalance
        to.balance == toBalance

        where:
        from           << [
            new Account(100),
            new Account(10)
        ]
        to             << [
            new Account(50),
            new Account(90)
        ]
        amount         << [50, 10]
        fromBalance    << [50, 0]
        toBalance      << [100, 100]
    }

    def "transferring between accounts should result in account withdrawal and deposit"() {
        when:
        service.transfer(from, to, amount)

        then:
        from.balance == fromBalance
        to.balance == toBalance

        where:
        from           << [
            new Account(100),
            new Account(10)
        ]
        to             << [
            new Account(50),
            new Account(90)
        ]
        amount         << [50, 10]
        fromBalance    << [50, 0]
        toBalance      << [100, 100]
    }
}
