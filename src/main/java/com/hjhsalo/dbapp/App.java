package com.hjhsalo.dbapp;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

import com.sarojaba.prettytable4j.PrettyTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.simple.SimpleLogger;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;


/**
 * Main command for dbapp cli tool
 */
@Command(
    name = "dbapp",
    subcommandsRepeatable = true,
    mixinStandardHelpOptions = true,
    version = "1.0-20200613",
    description = "Interact with library database",
    header = {
        "@|green _____  ____          _____  _____   |@",
        "@|green |  __ \\|  _ \\   /\\   |  __ \\|  __ \\ |@",
        "@|green | |  | | |_) | /  \\  | |__) | |__) ||@",
        "@|green | |  | |  _ < / /\\ \\ |  ___/|  ___/ |@",
        "@|green | |__| | |_) / ____ \\| |    | |     |@",
        "@|green |_____/|____/_/    \\_\\_|    |_|     |@",
        ""}
    )
class DbApp implements Callable<Integer> {

    // Picocli Option declarations

    private boolean[] verbosity = new boolean[0];

    @Option(
        names = { "-v", "--verbose" },
        description = {
            "Verbose mode. Helpful for troubleshooting.",
            "Specify multiple -v options to increase verbosity.",
            "For example, `-v -v` or `-vv`"}
        )
    public void setVerbose(final boolean[] verbosity) {
         this.verbosity = verbosity;
        }

    @Option(
        names = {"-u", "--user"},
        description = "Login to database with this username.",
        defaultValue = "STU82"
        )
    private String user = "STU82";

    // NOTE: In real life this would be always asked from user.
    @Option(
        names = {"-p", "--password"},
        description = "Password of the username.",
        defaultValue = "year2020"
        )
    private String password = "year2020";

    @Option(
        names = {"-c", "--connection"},
        defaultValue = "jdbc:oracle:thin:@(DESCRIPTION= (ADDRESS=(PROTOCOL=TCP)(HOST=toldb2.oulu.fi) (PORT=1521))(CONNECT_DATA=(SID=toldb19)))",
        description = { 
            "Connection URL to DB.",
            "Default value is: ",
            "(DESCRIPTION= (ADDRESS=(PROTOCOL=TCP)(HOST=toldb2.oulu.fi) (PORT=1521))(CONNECT_DATA=(SID=toldb19)))"}
        )
    private String connectionUrl;

    @Option(
        names = {"-t", "--timeout"},
        description = "Timeout for database login in seconds.",
        defaultValue = "5"
        )
    private int timeout = 5;

    @Spec
    CommandSpec spec;

    // Non Picocli related declarations
    private Logger logger;


    /**
    * Configures loglevel based on Picocli Option flags and returns a logger.
    * @return Logger
    */
    private final Logger getLogger() {
        System.setProperty(SimpleLogger.LOG_FILE_KEY, "System.out");
        if (verbosity.length >= 2) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "trace");
        } else if (verbosity.length == 1) {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "debug");
        } else {
            System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "info");
        }
        return LoggerFactory.getLogger(DbApp.class);
    }

    /**
     * Creates and opens an OracleConnection instance and returns it.
     * @return OracleConnection
     * @throws SQLException
     */
    private OracleConnection getConnection() throws SQLException {
        final OracleDataSource ods = new OracleDataSource();
        ods.setLoginTimeout(timeout);
        ods.setUser(user);
        ods.setPassword(password);
        ods.setURL(connectionUrl);

        final OracleConnection connection = (OracleConnection)ods.getConnection();

        // Log some database metadata
        DatabaseMetaData dbmd;
        dbmd = connection.getMetaData();
        logger.debug(
            String.format("Driver Name: %s", dbmd.getDriverName()));
        logger.debug(
            String.format("Driver Version: %s", dbmd.getDriverVersion()));

        // Log some database metadata
        logger.debug(
            String.format(
                "Default Row Prefetch Value is: %d", 
                connection.getDefaultRowPrefetch()));
        logger.debug(
            String.format(
                "Database Username is: %s", 
                connection.getUserName()));
        return connection;
    }

    /**
    * Display ID and Title each library item
    */
    @Command(
        name = "list-items",
        description = "List all items."
        )
    private void listItems() { 
        logger.info("Listing all items");
        
        final String sqlclause = "select id, title from t_item";

        PrettyTable pt = PrettyTable.fieldNames("ID", "Title");

        // According to Oracle documentation Statement, ResultSet 
        // and OracleConnection returned by getConnection() implement
        // AutoCloseable and as such all are closed automatically
        // at the end of try-catch.
        try (Statement statement = getConnection().createStatement()) {
            logger.debug(String.format("SQL> %s;", sqlclause));
            try (ResultSet resultSet = statement.executeQuery(sqlclause)) {
                    int i = 0;
                    String id = "";
                    String title = "";
                    while (resultSet.next()) {
                        i = i+1;
                        // NOTE: During testing it was seen that ResultSet
                        //       contained an actual Java null value if 
                        //       attribute in database didn't have a value.
                        //       This shouldn't be the case but a null 
                        //       check was added for safety nevertheless.
                        id = resultSet.getString(1);
                        if (resultSet.wasNull()) { id = "no id defined"; }
                        title = resultSet.getString(2);
                        if (resultSet.wasNull()) { title = "no title defined"; }
                        logger.debug(MessageFormat.format(
                            "row: {0}, value: [ {1}, {2} ]",
                            i, id, title));
                        pt.addRow(id, title);
                    }
                    System.out.println("");
                    System.out.println(pt);
                    System.out.println("");
                    System.out.println(String.format("%d items in total.", i));
                    System.out.println("");
            }
        } catch (final SQLException e) {
            logger.info("SQL or database connection related error occured.");
            logger.debug(e.getMessage(), e);
        }
    }

    /*
    * Display Name and email address of each library customer
    */
    @Command(
        name = "list-customers",
        description = "List all customers."
        )
    private void listCustomers() { 
        logger.info("Listing all customers");
        
        final String sqlclause = "select name, email from t_customer";

        PrettyTable pt = PrettyTable.fieldNames("Name", "Email");

        // According to Oracle documentation Statement, ResultSet 
        // and OracleConnection returned by getConnection() implement
        // AutoCloseable and as such all are closed automatically
        // at the end of try-catch.
        try (Statement statement = getConnection().createStatement()) {
            logger.debug(String.format("SQL> %s;", sqlclause));
            try (ResultSet resultSet = statement.executeQuery(sqlclause)) {
                    int i = 0;
                    String name = "";
                    String email = "";
                    while (resultSet.next()) {
                        i = i+1;
                        // NOTE: During testing it was seen that ResultSet
                        //       contained an actual Java null value if 
                        //       attribute in database didn't have a value.
                        //       This shouldn't be the case but a null 
                        //       check was added for safety nevertheless.
                        name = resultSet.getString(1);
                        if (resultSet.wasNull()) { name = "no name defined"; }
                        email = resultSet.getString(2);
                        if (resultSet.wasNull()) { email = "no email defined"; }
                        logger.debug(MessageFormat.format(
                            "row: {0}, value: [ {1}, {2} ]",
                            i, name, email));
                        pt.addRow(name, email);
                    }
                    System.out.println("");
                    System.out.println(pt);
                    System.out.println("");
                    System.out.println(String.format("%d customers in total.", i));
                    System.out.println("");
            }
        } catch (final SQLException e) {
            logger.info("SQL or database connection related error occured.");
            logger.debug(e.getMessage(), e);
        }
    }
    
    /**
     * Init application logger
     */
    private void initLogger() {
        logger = getLogger();
    }

    /**
     * Main business logic of the application.
     */
    @Override
    public Integer call() { 
        // Without subcommands dbapp just shows help.
        spec.commandLine().usage(System.out);
        return 0;
    }

    /**
     * Custom execution strategy which guarantees that any init -methods
     * will be run before any commands or subcommands
     * and then delegates to the default execution strategy, RunLast().
     */
    private int executionStrategy(ParseResult parseResult) {
        initLogger();
        return new CommandLine.RunLast().execute(parseResult);
    }

    /**
     * Main entrypoint for the application.
     * Subcommands are added via @Option decorators.
     * @param args
     */
    public static void main(String[] args) {
        DbApp dbapp = new DbApp();
        CommandLine commandLine = new CommandLine(dbapp);
        commandLine.setUnmatchedArgumentsAllowed(true);
        commandLine.setExecutionStrategy(dbapp::executionStrategy);
        System.exit(commandLine.execute(args));
    }
}
