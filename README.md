# Spock Arquillian Extension 

## BDD Testing in the container!

### What is it?

Arquillian is testing framework, developed at JBoss.org, that empowers
developers to write integration tests for business objects that are executed
inside of an embedded or remote container--options include a servlet
container, a Java EE application server or a Java SE CDI environment.

Spock is a testing and specification framework for Java and Groovy applications. 
What makes it stand out from the crowd is its beautiful and highly expressive specification language. 
Thanks to its JUnit runner, Spock is compatible with most IDEs, build tools, and continuous integration servers. 
Spock is inspired from JUnit, jMock, RSpec, Groovy, Scala, Vulcans, and other fascinating life forms.

The Spock Arquillian Extension opens up for the beauty of Spock tests running in-container using Arquillian with 
full EJB, Resource and CDI injection.


### Example
```
 @Deployment
 def static JavaArchive "create deployment"() {
     return ShrinkWrap.create(JavaArchive.class)
             .addClasses(AccountService.class, Account.class, SecureAccountService.class)
             .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
 }
 
 @Inject 
 AccountService service
        
 def "transferring between accounts should result in account withdrawal and deposit"() {
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
```
### Usage

The Spock Arquillian Extension supports both Groovy major versions supported
by Spock Framework. In order to select proper versions, you need to put
following dependencies into your <dependencies> section:
```
 <dependency>
     <groupId>org.jboss.arquillian.spock</groupId>
     <!-- replace * with standalone or container, according to your needs -->
     <artifactId>arquillian-spock-*</artifactId>
     <version>${project.version}</version>
     <scope>test</scope>
 </dependency>

 <!-- External Projects -->
 <dependency>
     <groupId>org.spockframework</groupId>
     <artifactId>spock-core</artifactId>
     <version>${version.spock}</version>
     <scope>test</scope>
 </dependency>

 <dependency>
     <groupId>org.codehaus.groovy</groupId>
     <artifactId>groovy-all</artifactId>
     <version>${version.groovy}</version>
     <scope>test</scope>
 </dependency>
```
For Groovy 2.x, use spock 0.7-groovy-2.0 or later and Groovy 2.1.4 or later

You must annotate the JUnit Runner with the ArquillianSputnik runner.
```
 @RunWith(ArquillianSputnik.class)
```

### Build

Regular `mvn clean install` will run tests against Wildfly 8.0.0.Final. Others containers can be used by defining `container` variable, for example
```
mvn clean install -Dcontainer="JBoss AS:7.1.1.Final:managed"
```

This flexibility and is provided by [Arquillian Chameleon](https://github.com/arquillian/arquillian-container-chameleon). For more available containers see [default list](https://github.com/arquillian/arquillian-container-chameleon/blob/1.0.0.Alpha6/src/main/resources/chameleon/default/containers.yaml).

### Contents of repository

 `core/`
	The Spock Extension itself.
   
 `standalone/`
 	Standalone Arquillian test executor.
 
 `container/`
 	Container extension which bundles all Spock-related dependencies required
 	while running Spock specifications using Arquillian.
 	
 `examples/`
	Sample tests written using Spock BDD framework.

### Licensing
 
 This distribution, as a whole, is licensed under the terms of the Apache
 License, Version 2.0 (see license.txt).
 

### More info

[Spock](http://spockframework.github.io/spock/docs/)
[Arquillian](http://arquillian.org/)
                             
