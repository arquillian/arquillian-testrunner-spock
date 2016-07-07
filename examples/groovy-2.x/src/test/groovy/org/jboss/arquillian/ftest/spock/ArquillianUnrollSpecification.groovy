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
  public static WebArchive createNotTestableWarArchive() {
    Class<?> thisClass = new Object() {}.class.enclosingClass
    String thisClassName = thisClass.simpleName

    WebArchive testWar = ShrinkWrap
      .create(WebArchive.class, thisClassName + "NotTestable.war")
      .addClass(thisClass)
    println testWar.toString(true)
    testWar
  }

  @Deployment(testable = true, name = "Testable")
  public static WebArchive createTestableWarArchive() {
    Class<?> thisClass = new Object() {}.class.enclosingClass
    String thisClassName = thisClass.simpleName

    WebArchive testWar = ShrinkWrap
      .create(WebArchive.class, thisClassName + "Testable.war")
      .addClass(thisClass)
    println testWar.toString(true)
    testWar
  }

  static int counterNotTestable
  static int counterNotTestableUnroll
  static int counterTestable
  static int counterTestableUnroll

  @Unroll
  @OperateOnDeployment(value = "NotTestable")
  def "NotTestable with Unroll ##test"() {
    expect:
    ++counterNotTestableUnroll == test
    println "++counterNotTestableUnroll = $counterNotTestableUnroll"
    where:
    test << [1, 2, 3]
  }

  @OperateOnDeployment(value = "NotTestable")
  def "NotTestable without Unroll"() {
    expect:
    ++counterNotTestable == test
    println "++counterNotTestable = $counterNotTestable"
    where:
    test << [1, 2, 3]
  }

  @Unroll
  @OperateOnDeployment(value = "Testable")
  def "Testable with Unroll ##test"() {
    expect:
    ++counterTestableUnroll == test
    println "++counterTestableUnroll = $counterTestableUnroll"
    where:
    test << [1, 2, 3]
  }

  @OperateOnDeployment(value = "Testable")
  def "Testable without Unroll"() {
    expect:
    ++counterTestable == test
    println "++counterTestable = $counterTestable"
    where:
    test << [1, 2, 3]
  }

  @RunAsClient
  def cleanup() {
    printCounters "cleanup"
  }

  @RunAsClient
  def cleanupSpec() {
    printCounters "cleanupSpec"
  }

  @RunAsClient
  def printCounters(String heading) {
    println heading
    println "  counterNotTestable       = $counterNotTestable"
    println "  counterNotTestableUnroll = $counterNotTestableUnroll"
    println "  counterTestable          = $counterTestable"
    println "  counterTestableUnroll    = $counterTestableUnroll"
  }

}
