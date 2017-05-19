Tandem Browsing toolkit
=======================

This software allows writing tandem browsable web applications by introducing a finite state machine
based declarative content definition language. The finite state machines use the State Chart XML (SCXML) 
syntax and each state contain a multipart page definition http://www.tandembrowsing.org/multipartpage, 
which allows designing web pages that consist of multiple parts, i.e. virtual screens. The virtual 
screens can be used to control how different user interface elements are distributed on the browsers. 
The content of the virtual screens is just plain web pages, which are referred by their URL.

The toolkit uses a proxy based architecture, i.e the broxy works as a intermediator between the 
tandem browsable webpages and the client browsers. The software is written mainly using Java and 
JavaSript languages and needs to be deployed as a Java web application in a Tomcat server. More 
information is awailable in the project web site http://www.tandembrowsing.org.

## Installation

Quickest way to start is to download the ready made tandembrowsing.war from the export folder and drop 
it in the webapps folder of a Tomcat server (tested with version 5.5.23).

The Tandem Browsing toolkit utilizes a mysql database for fault tolerance. It can also run in 
memory mode also by simply not having the database around. The database setup starts by creating 
a database and tables using the "src /sql / setup.sql" script. The username and password should 
be in synch with the ones defined in "WebContent / META-INF / context.xml" file.

## Building

The project has no build files currently, but the project can be opened e.g. in eclipse and use the 
export WAR file option of the eclipse. All the necessary sources and libraries are included. All 
the libraries are in the "WebContent / WEB-INF / lib" folder and should be refererred as Web App 
Libraries in eclipse. 

## Contact 

Send any comments to tommi.j.heikkinen@gmail.com





