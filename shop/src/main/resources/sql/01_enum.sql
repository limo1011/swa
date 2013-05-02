-- ===============================================================================
-- Jede SQL-Anweisung muss in genau 1 Zeile
-- Kommentare durch -- am Zeilenanfang
-- ===============================================================================

-- ===============================================================================
-- Tabellen fuer Enum-Werte *einmalig* anlegen und jeweils Werte einfuegen
-- Beim ALLERERSTEN Aufruf die Zeilen mit "DROP TABLE ..." durch -- auskommentieren
-- ===============================================================================
DROP TABLE geschlecht;
CREATE TABLE geschlecht(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(10) NOT NULL UNIQUE) CACHE;
INSERT INTO geschlecht VALUES (0, 'MAENNLICH');
INSERT INTO geschlecht VALUES (1, 'WEIBLICH');

DROP TABLE familienstand;
CREATE TABLE familienstand(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(12) NOT NULL UNIQUE) CACHE;
INSERT INTO familienstand VALUES(0, 'LEDIG');
INSERT INTO familienstand VALUES(1, 'VERHEIRATET');
INSERT INTO familienstand VALUES(2, 'GESCHIEDEN');
INSERT INTO familienstand VALUES(3, 'VERWITWET');

DROP TABLE hobby;
CREATE TABLE hobby(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(16) NOT NULL UNIQUE) CACHE;
INSERT INTO hobby VALUES (0, 'SPORT');
INSERT INTO hobby VALUES (1, 'LESEN');
INSERT INTO hobby VALUES (2, 'REISEN');

DROP TABLE transport_art;
CREATE TABLE transport_art(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(8) NOT NULL UNIQUE) CACHE;
INSERT INTO transport_art VALUES (0, 'STRASSE');
INSERT INTO transport_art VALUES (1, 'SCHIENE');
INSERT INTO transport_art VALUES (2, 'LUFT');
INSERT INTO transport_art VALUES (3, 'WASSER');

DROP TABLE mimetype;
CREATE TABLE mimetype(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(32) NOT NULL UNIQUE) CACHE;
INSERT INTO mimetype VALUES (0, 'image/gif');
INSERT INTO mimetype VALUES (1, 'image/jpeg');
INSERT INTO mimetype VALUES (2, 'image/pjpeg');
INSERT INTO mimetype VALUES (3, 'image/png');
INSERT INTO mimetype VALUES (4, 'video/mp4');
INSERT INTO mimetype VALUES (5, 'audio/wav');

DROP TABLE multimedia_type;
CREATE TABLE multimedia_type(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(8) NOT NULL UNIQUE) CACHE;
INSERT INTO multimedia_type VALUES (0, 'IMAGE');
INSERT INTO multimedia_type VALUES (1, 'VIDEO');
INSERT INTO multimedia_type VALUES (2, 'AUDIO');

DROP TABLE rolle;
CREATE TABLE rolle(id NUMBER(1) NOT NULL PRIMARY KEY, name VARCHAR2(32) NOT NULL) CACHE;
INSERT INTO rolle VALUES (0, 'admin');
INSERT INTO rolle VALUES (1, 'mitarbeiter');
INSERT INTO rolle VALUES (2, 'abteilungsleiter');
INSERT INTO rolle VALUES (3, 'kunde');


-- ===============================================================================
-- Fremdschluessel in den bereits *generierten* Tabellen auf die obigen "Enum-Tabellen" anlegen
-- ===============================================================================
--ALTER TABLE kunde ADD CONSTRAINT kunde__geschlecht_fk FOREIGN KEY (geschlecht_fk) REFERENCES geschlecht;
ALTER TABLE kunde ADD CONSTRAINT kunde__familienstand_fk FOREIGN KEY (familienstand_fk) REFERENCES familienstand;
ALTER TABLE kunde_hobby ADD CONSTRAINT kunde_hobby__hobby_fk FOREIGN KEY (hobby_fk) REFERENCES hobby;
ALTER TABLE file_tbl ADD CONSTRAINT multimedia__type_fk FOREIGN KEY (multimedia_type_fk) REFERENCES multimedia_type(id);
ALTER TABLE kunde_rolle ADD CONSTRAINT kunde_rolle__rolle_fk FOREIGN KEY (rolle_fk) REFERENCES rolle;
ALTER TABLE lieferung ADD CONSTRAINT lieferung__transport_art_fk FOREIGN KEY (transport_art_fk) REFERENCES transport_art;
