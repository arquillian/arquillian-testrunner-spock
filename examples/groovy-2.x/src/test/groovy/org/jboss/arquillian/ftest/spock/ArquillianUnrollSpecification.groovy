package org.jboss.arquillian.ftest.spock

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.container.test.api.OperateOnDeployment
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.runner.RunWith
import spock.lang.Specification
import spock.lang.Unroll

@RunWith(ArquillianSputnik)
class ArquillianUnrollSpecification extends Specification {

  @Deployment(testable = false, name = "NotTestable")
  static WebArchive createNotTestableWarArchive() {
    Class<?> thisClass = new Object() {}.class.enclosingClass
    String thisClassName = thisClass.simpleName

    WebArchive testWar = ShrinkWrap
      .create(WebArchive.class, thisClassName + "NotTestable.war")
      .addClass(thisClass)
    testWar
  }

  @Deployment(testable = true, name = "Testable")
  static WebArchive createTestableWarArchive() {
    Class<?> thisClass = new Object() {}.class.enclosingClass
    String thisClassName = thisClass.simpleName

    WebArchive testWar = ShrinkWrap
      .create(WebArchive.class, thisClassName + "Testable.war")
      .addClass(thisClass)
    testWar
  }

  static int counterNotTestable
  static int counterNotTestableUnroll
  static int counterTestable
  static int counterTestableUnroll

  @Unroll
  @OperateOnDeployment(value = "NotTestable")
  @RunAsClient
  def "NotTestable with Unroll ##test"() {
    expect:
    ++counterNotTestableUnroll == test
    where:
    test << [1, 2, 3]
  }

  @OperateOnDeployment(value = "NotTestable")
  @RunAsClient
  def "NotTestable without Unroll"() {
    expect:
    ++counterNotTestable == test
    where:
    test << [1, 2, 3]
  }

  @Unroll
  @OperateOnDeployment(value = "Testable")
  def "Testable with Unroll ##test"() {
    expect:
    ++counterTestableUnroll == test
    where:
    test << [1, 2, 3]
  }

  @OperateOnDeployment(value = "Testable")
  def "Testable without Unroll"() {
    expect:
    ++counterTestable == test
    where:
    test << [1, 2, 3]
  }

  def cleanup() {
    printCounters "cleanup"
  }

  def cleanupSpec() {
    printCounters "cleanupSpec"
  }

  def printCounters(String heading) {
    println heading
    println "  counterNotTestable       = $counterNotTestable"
    println "  counterNotTestableUnroll = $counterNotTestableUnroll"
    println "  counterTestable          = $counterTestable"
    println "  counterTestableUnroll    = $counterTestableUnroll"
  }

}
