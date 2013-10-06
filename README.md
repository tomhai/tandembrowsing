tandembrowsing
==============

Tandem Browsing toolkit - www.tandembrowsing.org

This software can be used to run multibrowsable finite state machines. The finite state machines
use State Chart XML (SCXML) syntax and each state contain a multipart page definition. The multipart
pages define how the content should be shown on each of the browser clients. The toolkit uses a 
proxy based architecture, i.e the broxy works as a intermediator between the multibrowsable webpages 
and the client browsers. 

The software is written mainly in Java and JavaSript needs to be deployed as a java web application 
(only tested in Tomcat). The project has no build files currently, but the project can be opened in 
eclipse using the ready made project files and use the export WAR file option of eclipse. All the 
necessary sources and libraries are included. The toolkit uses the SCXML java engine library from 
Apache Commons SCXML project to execute finite state machines. It also uses the Direct Web Remoting 
library to manage the browser sessions via AJAX and COMET. 


