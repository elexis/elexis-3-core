Running the unittests
---------------------

You need to grant access to the mysql and postgresql databases unittest.

Have look at the .travis.yml (in the root directory) to see how it is used in our CI environment and with which versions
the databases it is tested.

For postgres run the following commands

    sudo -u postgres psql -c "create user elexisTest with UNENCRYPTED password 'elexisTest';"
    sudo -u postgres psql -c "create database unittests";
    sudo -u postgres psql -c "grant all privileges on database unittests to elexisTest;"
    export PGPASSWORD=elexisTest # to avoid being prompted for a password

Now check whether you can connect to it using

	psql --user=elexistest --host=localhost  unittests

For mysql run the following commands\
(note: There seem to be issues with these Tests and MariaDB. So if they
keep failing, make sure to use Mysql Server)

    mysql --host=localhost --user root
    create database unittests;
    grant all on unittests.* to elexisTest@localhost identified by 'elexisTest';

Now check whether you can connect to it using

	mysql -u elexisTest --host=localhost --password=elexisTest unittests

Then, right-click on DataAllTests.launch and select "Run as" or "Debug as" DataAllTests.
