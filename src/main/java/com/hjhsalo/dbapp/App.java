package com.hjhsalo.dbapp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "dbapp", subcommandsRepeatable = true, mixinStandardHelpOptions = true, version = "1.0")
class MyApp implements Callable<Integer> {

    @Option(
        names = {"-u", "--user"}, 
        description = "Username" )
    private String user = "STU82";
    
    // @Option(
    //     names = {"-p", "--password"}, 
    //     description = "Password", 
    //     interactive = true )
    private String password = "year2020";

    @Option(
        names = {"-c", "--connection"}, 
        defaultValue = "jdbc:oracle:thin:@(DESCRIPTION= (ADDRESS=(PROTOCOL=TCP)(HOST=toldb2.oulu.fi) (PORT=1521))(CONNECT_DATA=(SID=toldb19)))", 
        description = "Connection URL to DB" )
    private String connectionUrl;

    @Override
    public Integer call() { // business logic
        // OracleConnection is AutoCloseable and closed automatically
        try {
            OracleConnection connection = getConnection();

            // Print some database metadata
            DatabaseMetaData dbmd;
            dbmd = connection.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());

            // Print some connection properties
            System.out.println(
                "Default Row Prefetch Value is: " + 
               connection.getDefaultRowPrefetch());
            System.out.println(
                "Database Username is: " + 
                connection.getUserName());
            System.out.println();
            printCustomers(connection);
        } catch (SQLException e) {

            e.printStackTrace();
        }

        return 0; // exit code
    }

    /*
    * Displays name and email from the employees table.
    */
    public static void printCustomers(Connection connection) throws SQLException {
        // Statement and ResultSet are AutoCloseable and closed automatically. 
        try (Statement statement = connection.createStatement()) {      
            try (ResultSet resultSet = statement
                .executeQuery("select name, email from t_customer")) {
            System.out.println("name" + "  " + "email");
            System.out.println("---------------------");
            while (resultSet.next())
                System.out.println(
                    resultSet.getString(1) + 
                    " " + 
                    resultSet.getString(2) + " ");       
            }
        }
    }

    /**
     * Creates an OracleConnection instance and return it.
     * @return oracleConnection
     * @throws SQLException
     */
    private OracleConnection getConnection() throws SQLException {
        OracleDataSource ods = new OracleDataSource();
        ods.setUser(user);
        ods.setPassword(password);
        ods.setURL(connectionUrl);
        return (OracleConnection)ods.getConnection();
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new MyApp()).execute(args));
    }
}
