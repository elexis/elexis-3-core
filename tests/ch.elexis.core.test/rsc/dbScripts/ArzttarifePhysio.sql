CREATE TABLE IF NOT EXISTS `ch_elexis_arzttarife_ch_physio` (
  `ID` varchar(25) NOT NULL,
  `lastupdate` bigint(20) DEFAULT NULL,
  `deleted` char(1) DEFAULT '0',
  `validFrom` char(8) DEFAULT NULL,
  `validUntil` char(8) DEFAULT NULL,
  `TP` char(8) DEFAULT NULL,
  `ziffer` varchar(6) DEFAULT NULL,
  `titel` varchar(255) DEFAULT NULL,
  `description` longtext,
  PRIMARY KEY (`ID`)
) ;

INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('VERSION',1360652444282,'0',NULL,NULL,NULL,'0.0.1',NULL,NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('I5b10d179fd362c2b04095',1456991721904,'0','19700101','20380118','4800','7301','Sitzungspauschale für allgemeine Physiotherapie (z.B. Bewegungstherapie, Massage und Kombinationen mit Therapien der Ziffer 7320)',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('O9f0dd108a45f2c2504097',1456991721906,'0','19700101','20380118','7700','7311','Sitzungspauschale für aufwendige Bewegungstherapie',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('P5e5c57c3c194a42404099',1456991721907,'0','19700101','20380118','7700','7312','Sitzungspauschale für manuelle Lymphdrainage',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('wc1c22d79d15a542404101',1456991721909,'0','19700101','20380118','7700','7313','Sitzungspauschale für Hippotherapie',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('rc391a4ef0f06dfe504103',1456991721911,'0','19700101','20380118','1000','7320','Sitzungspauschale für Elektro- und Thermotherapie / Instruktion bei Gerätevermietung',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('z7a083382bf7e2fe404105',1456991721913,'0','19700101','20380118','2500','7330','Sitzungspauschale für Gruppentherapie',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('o89b0af61190c73e404107',1456991721914,'0','19700101','20380118','2200','7340','Sitzungspauschale für Medizinische Trainingstherapie (MTT)',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('pcd96382563af57e704109',1456991721916,'0','19700101','20380118','2400','7350','Zuschlagsposition für die erste Behandlung eines Patienten',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('scfb4c537dc6513e704111',1456991721918,'0','19700101','20380118','3000','7351','Zuschlagsposition für die Behandlung chronisch behinderter Kinder',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('facb45d540ec3bfe604113',1456991721919,'0','19700101','20380118','1900','7352','Zuschlagsposition für die Benutzung des Gehbads / Schwimmbads',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('h729bf5de4438ebe604115',1456991721921,'0','19700101','20380118','6700','7353','Zuschlagsposition für die Benutzung der Infrastruktur bei Hippotherapie',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('k72c8f9c1bace63e604117',1456991721923,'0','19700101','20380118','3400','7354','Pauschale für die Weg- / Zeitentschädigung ',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('y5c18791d690f7fe604119',1456991721924,'0','19700101','20380118','2400','7401','Besitzstandwahrer: Sitzungspauschale für allgemeine Physiotherapie z.B. Bewegungstherapie, Massage und / oder Kombinationen mit Therapien der Ziffer 7420',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('cc2cf004a00d6fbe904121',1456991721926,'0','19700101','20380118','3800','7412','Besitzstandwahrer: Sitzungspauschale für Lymphdrainage (entsprechende Zusatzausbildung wird vorausgesetzt)',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('b806c174208f3cbe904123',1456991721928,'0','19700101','20380118','1000','7420','Besitzstandwahrer: Sitzungspauschale für Elektro- und Thermotherapie / Instruktion bei Gerätevermietung',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('m8e3668cbd8a157e904125',1456991721930,'0','19700101','20380118','1900','7452','Besitzstandwahrer: Zuschlagsposition für die Benutzung des Gehbads / Schwimmbads',NULL);
INSERT INTO `ch_elexis_arzttarife_ch_physio` VALUES ('Aa9ed917552c85be904127',1456991721932,'0','19700101','20380118','2300','7454','Besitzstandwahrer: Pauschale für Weg- / Zeitentschädigung ',NULL);
