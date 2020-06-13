# DbApp -clitool

This is a cli tool for interacting with a Oracle
database in context of a school programming assignment.

Maven is used for dependency management and for
packaging the application as a single jar file.

## Install

* Check releases page:  
  https://github.com/hjhsalo/hjh-dbapp/releases

## To build

Clone this repository or obtain the source code via
some other means and at the repository root:

* Run `mvn clean compile assembly:single`  
  to build a jar file with all dependencies included.  
  (RECOMMENDED)

* Run `mvn package`  
  to build jar file with just DbApp classes and resources.  
  (Make sure that you have all dependencies in classpath.)

## To run

* Build the application and run with  
  `java -jar ./hjh-dbapp/target/hjh-dbapp-1.0-20200613-jar-with-dependencies.jar`

```
  _____  ____          _____  _____   
|  __ \|  _ \   /\   |  __ \|  __ \ 
| |  | | |_) | /  \  | |__) | |__) |
| |  | |  _ < / /\ \ |  ___/|  ___/ 
| |__| | |_) / ____ \| |    | |     
|_____/|____/_/    \_\_|    |_|     

Usage: dbapp [-hVv] [-c=<connectionUrl>] [-p=<password>] [-t=<timeout>]
             [-u=<user>] [COMMAND]
Interact with library database
  -c, --connection=<connectionUrl>
                            Connection URL to DB.
                            Default value is:
                            (DESCRIPTION= (ADDRESS=(PROTOCOL=TCP)(HOST=toldb2.
                              oulu.fi) (PORT=1521))(CONNECT_DATA=(SID=toldb19)))
  -h, --help                Show this help message and exit.
  -p, --password=<password> Password of the username.
  -t, --timeout=<timeout>   Timeout for database login in seconds.
  -u, --user=<user>         Login to database with this username.
  -v, --verbose             Verbose mode. Helpful for troubleshooting.
                            Specify multiple -v options to increase verbosity.
                            For example, `-v -v` or `-vv`
  -V, --version             Print version information and exit.
Commands:
  list-customers  List all customers.
  list-items      List all items.
```

## Troubleshooting

* Make sure you can physically connect to the database, or change the connection URL to something else you can access.
* Default username and password are provided. Change them as needed.
* Increase verbosity to see stack traces of SQL and connection related errors.

## Attributions

* Implementation would not have been possible without Picocli:  
  https://picocli.info/

* Database connection code is heavily influenced by Oracle provided examples:  
  https://github.com/oracle/oracle-db-examples/tree/master/java/jdbc

* Printing to tables achieved easily with PrettyTable4J:  
  https://github.com/sarojaba/prettytable4j

* Various Stackoverflow posts were used as source of information.  
  No intentional copying of code without attribution has been done.

All source code is licensed under Apache License, Version 2.0.

Copyright Harri Hirvonsalo, 2020.
