{ pkgs, lib, config, inputs, ... }:

let
  DUMP_PREFIX = "/opt/bruno/dumps/bruno_";
  OLD_VERSION = "2_1";
  NEW_VERSION = "3_4";
  XXX = lib.toInt(lib.replaceStrings [ "_" ] [ "" ] NEW_VERSION);
  MYSQL_PORT = 60000 + XXX;
  SQL_DUMP = "" + DUMP_PREFIX + OLD_VERSION + ".sql";

in {
  # https://devenv.sh/basics/
  env.LANG= "fr_CH.UTF-8";
  env.LC_MESSAGES= "fr_CH.UTF-8";
  env.LC_ALL= "fr_CH.UTF-8";
  env.MYSQL_TCP_PORT = MYSQL_PORT;

  # https://devenv.sh/packages/
  packages = [ pkgs.git pkgs.maven pkgs.xvfb-run pkgs.gtk4 ];
  # Helpers for migrating old elexis mysql DB and dump
  services.mysql.enable = true;
  services.mysql.ensureUsers = [
    {
      name = "elexis";
      password = "elexisTest";
      ensurePermissions = {
        "elexis.*" = "ALL PRIVILEGES";
      };
    }
  ];
  services.mysql.initialDatabases = [
    { name = "elexis_$NEW_VERSION";
    }
  ];
  services.mysql.settings = {
    mysqld = {
      lower_case_table_names = "1";
    };
  };
  scripts.dumpe_new_db.exec = ''
    echo elexis_$NEW_VERSION
    echo elexis $NEW_VERSION
    echo mysqldump --user=elexis --password=elexisTest --single-transaction  elexis_$NEW_VERSION >   elexis_$NEW_VERSION.sql
  '';
  scripts.load_old_db.exec = ''
    echo new elexis_$NEW_VERSION
    echo old elexis_$OLD_VERSION old
    echo cat /opt/bruno_$OLD_VERSION.sql
    echo mysql elexis_$NEW_VERSION
  '';

  enterShell = ''
    git --version
    mvn --version
    echo old DB: elexis_${OLD_VERSION} to new elexis_${NEW_VERSION}
    echo mysql port to load into
    echo File for old sql_dump ${SQL_DUMP} must exist
    ls -l ${SQL_DUMP}
  '';

  # https://devenv.sh/tests/
  enterTest = ''
    echo "Running tests"
    git --version | grep --color=auto "${pkgs.git.version}"
    xvfb-run mvn -V clean verify  -Dtycho.localArtifacts=ignore
  '';

  # See full reference at https://devenv.sh/reference/options/
}
