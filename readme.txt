                           Spock Arquillian Extension 

                             Test in the container!

 What is it?
 ============

 Arquillian is testing framework, developed at JBoss.org, that empowers
 developers to write integration tests for business objects that are executed
 inside of an embedded or remote container--options include a servlet
 container, a Java EE application server or a Java SE CDI environment.
 
 Spock is a testing and specification framework for Java and Groovy applications. 
 What makes it stand out from the crowd is its beautiful and highly expressive specification language. 
 Thanks to its JUnit runner, Spock is compatible with most IDEs, build tools, and continuous integration servers. 
 Spock is inspired from JUnit, jMock, RSpec, Groovy, Scala, Vulcans, and other fascinating life forms.
 
 The Spock Arquillian Extension opens up for the beauty of Spock tests running in-contianer using Arquillian with 
 full EJB, Resource and CDI injection.


 Example
 ========

 @Deployment
 def static JavaArchive "create deployment"() {
     return ShrinkWrap.create(JavaArchive.class)
             .addClasses(AccountService.class, Account.class, SecureAccountService.class)
             .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
 }
 
 @Inject 
 AccountService service
        
 def "transferring between accounts should result in account withdrawl and diposit"() {
      when:
      service.transfer(from, to, amount)
        
      then:
      from.balance == fromBalance
      to.balance == toBalance
        
      where:
      from <<         [new Account(100),  new Account(10)]
      to <<           [new Account(50),   new Account(90)]
      amount <<       [50,                10]
      fromBalance <<  [50,                0]
      toBalance <<    [100,               100]
 }

 Contents of distribution
 ========================

 core/
	The Spock Extension.
   
 standalone/
 	Standalone Arquillian test executor.
 
 container/
 	Container extension which bundles all Spock-related dependencies required
 	while running spock specifications using Arquillian.
 	
examples/
	Sample tests written using Spock BDD framework.

 Licensing
 =========
 
 This distribution, as a whole, is licensed under the terms of the Apache
 License, Version 2.0 (see license.txt).
 

 URLs
 ===============

 Spock:      http://spockframework.org/
 Arquillian: http://jboss.org/arquillian/
                             
