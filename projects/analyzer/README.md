Analyzer

Może pracować w dwóch trybach w zależności od parametrów wiersza poleceń.

wyświetlenie opcji możliwe po wywołaniu
analyzer -help


Przetwarzanie obrazów z bazy danych
=============
1. Sporządza listę plików które są w tabeli Images ale nie mają plików z deskryptorami (wg. tabeli ImageDescriptors)
2. Dla każdego pliku na liście:
   - tworzy pliki z deskryptorem w formacie xml
   - wypełnia tabelę ImageDescriptors ścieżkami do plików z deskryptorami


Polecenie:
analyzer -all



Przetwarzenie pojedynczego obrazu
================

Polecenie:
analyzer -img /home/user/plik.jpg -desc /home/user/plik_sift.xml


