--comment default password is 'administrator'
--comment http://java.dzone.com/articles/secure-password-storage-lots
--comment DEFAULT password IS 'user'
INSERT INTO `KONTAKT` VALUES ('h2c1172107ce2df95065',1470809122983,'0','0','1','0','1','1','0',NULL,NULL,'m','Dr.',NULL,'Nachname','Hauptanwender',NULL,'Beispielstrasse 15','6840','Beispielstrasse 15','0190 222 222',NULL,'0190 222 224',NULL,'haupt@anwender.ch',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'504B0304140008080800F8400A4900000000000000000000000004000000686173685BF39681B5B88841382BB12C51AFB4243347CF23B138A324312927557837BFAAA2D7931DCC0C4C6E0C5C39F989296E89C925F9459E0C9C251945A9C519F939291505F60E0C20C051CE0124B98198B18481DD37312F2531AFA48441CC2F3139232F313755C123B1B4A02431AF3C352F25B5A80200504B070806C4629A7400000077000000504B01021400140008080800F8400A4906C4629A740000007700000004000000000000000000000000000000000068617368504B0506000000000100010032000000A60000000000','0', null);
INSERT INTO USER_ (ID, IS_ADMINISTRATOR, SALT, HASHED_PASSWORD, KONTAKT_ID) 
	VALUES ('user', '0', 'd204f347a59112b2', 'bb4f9b2bc53c23544408ddeaadb1e0f4a1fb0612', 'h2c1172107ce2df95065');