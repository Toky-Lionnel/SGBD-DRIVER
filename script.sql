CREATE TABLE Cours (ID INT 255,NOM STRING 255,DUREE INT 100,ENSEIGNANT_ID INT 255)
INSERT INTO Cours (ID, NOM, DUREE, ENSEIGNANT_ID) VALUES (1, 'Mathématiques', 40, 101)
INSERT INTO Cours (ID, NOM, DUREE, ENSEIGNANT_ID) VALUES (2, 'Physique', 35, 102)
INSERT INTO Cours (ID, NOM, DUREE, ENSEIGNANT_ID) VALUES (3, 'Informatique', 45, 103)


CREATE TABLE Test1 (ID INT 255,NOM STRING 255)

INSERT INTO Test1 (ID, NOM) VALUES (1, 'Archi')
INSERT INTO Test1 (ID, NOM) VALUES (2, 'Algo')
INSERT INTO Test1 (ID, NOM) VALUES (3, 'Syst')

CREATE TABLE Test2 (ID INT 255,NOM STRING 255)

INSERT INTO Test2 (ID, NOM) VALUES (1, 'Archi')
INSERT INTO Test2 (ID, NOM) VALUES (2, 'Algo')
INSERT INTO Test2 (ID, NOM) VALUES (3, 'Math')