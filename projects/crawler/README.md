cbir_crawler> 

mvn clean compile assembly:single

Instalacja w repo mavena
mvn install assembly:single



aby uruchomić crawlera:

cd target
java -jar visearch.crawler-0.0.1-SNAPSHOT-jar-with-dependencies.jar http://www.uwm.edu.pl
  
