Basic start of a potentionally malicious spy software created as part of a university project.

If the client is started on a computer it will connect to the controller through a hard coded ip.
The controller can then chose to do anything ranging from adding and retrieving files, creating folders or going through folders, executing files, 
taking and retrieving screenshots etc without the client being visually aware of it.

As the software is not created with the intention of being used maliciously it will not auto start when the computer is turned, move itself to a
different folder or in any other way try to disguise itself. 

Main class for the client is THClient.java 
Main class for the controller is TrojanHorse.java
