K-means clustering with Mahout


cd /projects/clustering

mvn clean compile assembly:single

Dla każdego obrazka tworzony jest osobny plik SEQ z deskryptorami.



Uruchomienie w trybie SEQUENTIAL (przydaje się do debugowania)
w Run configuration, arguments, VM arguments wpisać: 
-useMapReduce=0
