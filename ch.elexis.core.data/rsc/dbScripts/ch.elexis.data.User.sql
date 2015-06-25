#
# SQL init for ch.elexis.data.Anwender
# MEDEVIT <office@medevit.at>
#
# See https://redmine.medelexis.ch/issues/2112
#
CREATE TABLE USER_ (
  ID VARCHAR(25) NOT NULL,				# the username is the id
  DELETED CHAR(1) DEFAULT '0',
  KONTAKT_ID VARCHAR(25),				# foreign key to contact table
  LASTUPDATE BIGINT,
  HASHED_PASSWORD VARCHAR(64),			# Currently SHA-256
  SALT VARCHAR(64),						# The SALT used for SHA256_PASSWORD hash
  IS_ACTIVE CHAR(1) DEFAULT '1', 		# Is this account currently active? If not, any log-in is to be prohibited.
  IS_ADMINISTRATOR CHAR(1) DEFAULT '0',	# User is an administrator
  KEYSTORE TEXT,						# For key based authentication or signature purposes
  EXTINFO BLOB,
  PRIMARY KEY (ID)
);

# default password is 'administrator'
# http://java.dzone.com/articles/secure-password-storage-lots
INSERT INTO USER_ (ID, IS_ADMINISTRATOR, SALT, HASHED_PASSWORD) 
	VALUES ('Administrator', '1', '1254bb9a05856b9e', 'b94a0b6fc7be97e0a1585ac85e814d3852668968');

# DROP TABLE USER_;