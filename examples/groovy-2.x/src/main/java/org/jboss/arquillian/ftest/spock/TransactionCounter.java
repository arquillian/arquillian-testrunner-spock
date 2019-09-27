package org.jboss.arquillian.ftest.spock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class TransactionCounter {

    private int counter = 0;

    private int limit = 0;

    public void observe(@Observes TransferEvent transferOccurred) {
        counter++;
    }

    public void defineLimit(int limit) {
        this.limit = limit;
    }

    public int getCounter() {
        return counter;
    }

    public void belowLimit() {
        if (counter > limit) {
            throw new AssertionError("Expected total of " + limit + " transactions, but already have " + counter);
        }
    }
}

