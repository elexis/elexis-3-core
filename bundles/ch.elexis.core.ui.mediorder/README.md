
# Verwaltung von Medikamentbestellungen durch Patienten




### Technische Details Lagereintrag

Stati eines Mediorders

* (A) ANFORDERUNG ERWARTET - Medikament wurden zur Bestellung freigegeben, aber nicht angefordert
* (B) ANGEFORDERT - Medikament wurden zur Bestellung freigegeben und angefordert
* (C) BESTELLT - Medikament wurde angefordert und bestellt
* (D) AUF LAGER - Medikament ist eingetroffen

	Status											MIN/MAX/IST
	(A) Freigabe für Anforderung						0/2/0
	(B) Bestellanforderung durch MPA oder PEA* 			2/2/0
	(C)	-""-											2/2/0	sowie existierende IOrder
	(D) Bestellung eingegangen und eingebucht			2/2/2


* Aufnahme durch MPA in Elexis erzeugt direkt diesen Zustand


### Offene Punkte

soll Abschliessen und Verrechnen erst möglich sein sobald Bestellung eingegangen?