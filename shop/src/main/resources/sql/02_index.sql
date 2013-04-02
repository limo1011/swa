-- ===============================================================================
-- Jede SQL-Anweisung muss in genau 1 Zeile
-- Kommentare durch -- am Zeilenanfang
-- ===============================================================================

-- ===============================================================================
-- Indexe in den *generierten* Tabellen anlegen
-- ===============================================================================
CREATE INDEX adresse__kunde_index ON adresse(kunde_fk);
CREATE INDEX kunde_hobby__kunde_index ON kunde_hobby(kunde_fk);
CREATE INDEX kunde__file_index ON kunde(file_fk);
CREATE INDEX kunde_rolle__kunde_index ON kunde_rolle(kunde_fk);
CREATE INDEX wartungsvertrag__kunde_index ON wartungsvertrag(kunde_fk);
CREATE INDEX bestellung__kunde_index ON bestellung(kunde_fk);
CREATE INDEX bestpos__bestellung_index ON bestellposition(bestellung_fk);
CREATE INDEX bestpos__artikel_index ON bestellposition(artikel_fk);
