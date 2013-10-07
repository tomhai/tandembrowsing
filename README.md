tandembrowsing
==============

Tandem Browsing toolkit - www.tandembrowsing.org

This software can be used to browse web content synhronously using multiple devices. The web pages 
can be standard web pages, which are listed ina specific finite state machines. The finite state 
machines contain one or more states, which declaratively define how the web pages should be viewed
on each device. The state machines use the State Chart XML (SCXML) syntax and each state contain 
a multipart page definition www.tandembrowsing.org/multipartpage. 

The toolkit uses a proxy based architecture, i.e the broxy works as a intermediator between the 
tandem browsable webpages and the client browsers. The software is written mainly using Java and 
JavaSript languages and needs to be deployed as a java web application on a Tomcat server. The 
project has no build files currently, but the project can be opened in eclipse using the ready made 
project files and use the export WAR file option of eclipse. All the necessary sources and libraries 
are included. The toolkit uses the SCXML java engine library from Apache Commons SCXML project to 
execute SCXML state machines. For managing the client sessions, it uses the Direct Web Remoting 
library. 


