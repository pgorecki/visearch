Crawler przegląda sieć www w poszukiwaniu obrazków. Jeżeli coś znajdzie to zapisuje pomniejszoną kopię pliku na dysku i wpisuje ścieżkę do pliku do tabeli 


cbir_crawler> 

mvn clean compile assembly:single

Instalacja w repo mavena
mvn install assembly:single



aby uruchomić crawlera:

cd target
java -jar visearch.crawler-0.0.1-SNAPSHOT-jar-with-dependencies.jar http://www.uwm.edu.pl
  
