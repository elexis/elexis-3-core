INSERT INTO ETIKETTEN (`ID`,`lastupdate`,`Image`,`deleted`,`importance`,`Name`,`foreground`,`background`,`classes`) VALUES ('T3811f3656cea91c2080136',1505824603577,'H210ee8c59884cde4040','0','81','verstorben','000000','FFFFFF',NULL);
INSERT INTO ETIKETTEN (`ID`,`lastupdate`,`Image`,`deleted`,`importance`,`Name`,`foreground`,`background`,`classes`) VALUES ('p4ae91d5f9cca98890259',1502353661620,NULL,'0','78','Inkasso','000000','ff0000',NULL);

INSERT INTO ETIKETTEN_OBJCLASS_LINK (`objclass`,`sticker`) VALUES ('ch.elexis.data.Patient','T3811f3656cea91c2080136');
INSERT INTO ETIKETTEN_OBJCLASS_LINK (`objclass`,`sticker`) VALUES ('ch.elexis.data.Patient','p4ae91d5f9cca98890259');

INSERT INTO ETIKETTEN_OBJECT_LINK (`lastupdate`,`obj`,`etikette`) VALUES (1523255371783,'s9b71824bf6b877701111','T3811f3656cea91c2080136');
INSERT INTO ETIKETTEN_OBJECT_LINK (`lastupdate`,`obj`,`etikette`) VALUES (1523255371783,'zd8d46d1b8d44330501105','p4ae91d5f9cca98890259');
INSERT INTO ETIKETTEN_OBJECT_LINK (`lastupdate`,`obj`,`etikette`) VALUES (1523255371783,'zd8d46d1b8d44330501105','T3811f3656cea91c2080136');