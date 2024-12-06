package ibnk.repositories.internet;

import com.crystaldecisions.sdk.occa.report.application.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.data.*;
import com.crystaldecisions.sdk.occa.report.lib.IStrings;
import com.crystaldecisions.sdk.occa.report.lib.PropertyBag;
import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service()
@RequiredArgsConstructor
public class BankRepository {


    @Value("${spring.datasource.rptbanking.username}")
    private String username;
    @Value("${spring.datasource.rptbanking.password}")
    private String password;
    @Value("${spring.datasource.rptbanking.database}")
    private String database;
    @Value("${spring.datasource.rptbanking.jdbcUrl}")
    private String url;
    @Value("${spring.datasource.banking.driver-class-name}")
    private String driver;

    //    @Autowired
    //     public BankRepository(BankingDataSource dataSource) {
    //         this.dataSource = dataSource;
    //     }
    @Qualifier("gb_rptBankingDataSource")
    @Autowired
    private DataSource rptBankingJdbcTemplate;
    public BankRepository(@Qualifier("gb_rptBankingDataSource") DataSource rptBankingJdbcTemplate) {
        this.rptBankingJdbcTemplate = rptBankingJdbcTemplate;
    }

    // Method to execute a SQL query and return the ResultSet
    public ResultSet executeQuery(String sql,String employeeId) throws SQLException {
        // Obtain a connection from the DataSource
        java.sql.Connection conn = DataSourceUtils.getConnection(rptBankingJdbcTemplate);

        // Create a PreparedStatement and execute the query
        PreparedStatement ps = conn.prepareStatement(sql);
        // Set the employeeId parameter in the prepared statement
        ps.setString(1, employeeId);
        return ps.executeQuery();
    }
    public void passResultSet(ReportClientDocument clientDoc, ResultSet rs, String tableAlias, String reportName) throws ReportSDKException {
        if (reportName.equals("")) {
            clientDoc.getDatabaseController().setDataSource(rs, tableAlias, tableAlias);
        } else {
            clientDoc.getSubreportController().getSubreport(reportName).getDatabaseController().setDataSource(rs, tableAlias, tableAlias);
        }

    }

    public void changeDataSource(ReportClientDocument clientDoc, String reportName) throws ReportSDKException {
        PropertyBag propertyBag = null;
        IConnectionInfo connectionInfo = null;
        ITable origTable = null;
        ITable newTable = null;
        String TRUSTED_CONNECTION = "false";
        String SERVER_TYPE = "JDBC (JNDI)";
        String USE_JDBC = "true";
        String DATABASE_DLL = "crdb_jdbc.dll";
        String JNDI_DATASOURCE_NAME = database;
        String CONNECTION_URL = url;
        String DATABASE_CLASS_NAME = driver;
        String DB_USER_NAME = username;
        String DB_PASSWORD = password;
        int subNum;
        if (reportName == null || reportName.equals("")) {
            Tables tables = clientDoc.getDatabaseController().getDatabase().getTables();

            for (subNum = 0; subNum < tables.size(); ++subNum) {
                origTable = tables.getTable(subNum);
                newTable = (ITable) origTable.clone(true);
                newTable.setQualifiedName(origTable.getAlias());
                connectionInfo = newTable.getConnectionInfo();
                propertyBag = new PropertyBag();
                propertyBag.put("Trusted_Connection", TRUSTED_CONNECTION);
                propertyBag.put("Server Type", SERVER_TYPE);
                propertyBag.put("Use JDBC", USE_JDBC);
                propertyBag.put("Database DLL", DATABASE_DLL);
                propertyBag.put("JNDI Datasource Name", JNDI_DATASOURCE_NAME);
                propertyBag.put("Connection URL", CONNECTION_URL);
                propertyBag.put("Database Class Name", DATABASE_CLASS_NAME);
                connectionInfo.setAttributes(propertyBag);
                connectionInfo.setUserName(DB_USER_NAME);
                connectionInfo.setPassword(DB_PASSWORD);
                clientDoc.getDatabaseController().setTableLocation(origTable, newTable);

            }
        }
        if (reportName == null || !reportName.equals("")) {
            IStrings subNames = clientDoc.getSubreportController().getSubreportNames();

            for (subNum = 0; subNum < subNames.size(); ++subNum) {
                Tables tabless = clientDoc.getSubreportController().getSubreport(subNames.getString(subNum)).getDatabaseController().getDatabase().getTables();

                for (int i = 0; i < tabless.size(); ++i) {
                        origTable = tabless.getTable(i);
                        newTable = (ITable) origTable.clone(true);
                        newTable.setQualifiedName(origTable.getAlias());
                        connectionInfo = newTable.getConnectionInfo();
                        propertyBag = new PropertyBag();
                        propertyBag.put("Trusted_Connection", TRUSTED_CONNECTION);
                        propertyBag.put("Server Type", SERVER_TYPE);
                        propertyBag.put("Use JDBC", USE_JDBC);
                        propertyBag.put("Database DLL", DATABASE_DLL);
                        propertyBag.put("JNDI Datasource Name", JNDI_DATASOURCE_NAME);
                        propertyBag.put("Connection URL", CONNECTION_URL);
                        propertyBag.put("Database Class Name", DATABASE_CLASS_NAME);
                        connectionInfo.setAttributes(propertyBag);
                        connectionInfo.setUserName(DB_USER_NAME);
                        connectionInfo.setPassword(DB_PASSWORD);
                        clientDoc.getSubreportController().getSubreport(subNames.getString(subNum)).getDatabaseController().setTableLocation(origTable, newTable);
                }
            }
        }
    }


//    public ReportClientDocument configureReportDataSource2(ReportClientDocument reportClientDocument) {
//        ITable origTable = null;
//        ITable newTable = null;
//        try {
//            // Start the COM thread
//            // Get the database controller
//            // Obtain collection of tables from this database controller.
//            origTable = reportClientDocument.getDatabaseController().getDatabase().getTables().getTable(0);
//            newTable = (ITable) origTable.clone(true);
//            newTable.setQualifiedName(origTable.getAlias());
//
//            // Create connection info
//            // Create a PropertyBag to hold attributes
//            PropertyBag attributes = new PropertyBag();
//            // Set OLE DB connection attributes
//            attributes.put("Trusted_Connection", "false");
//            attributes.put("Server Type", "JDBC (JNDI)");
//            attributes.put("Use JDBC", "true");
//            attributes.put("Database DLL", "crdb_jdbc.dll");
//            attributes.put("JNDI Datasource Name", "Banking");
//            attributes.put("Connection URL", "jdbc:sqlserver://localhost:1433;databaseName=Banking;encrypt=false");
//            attributes.put("Database Class Name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
//            IConnectionInfo connectionInfo = newTable.getConnectionInfo();
//            connectionInfo.setKind(ConnectionInfoKind.SQL);
//            connectionInfo.setUserName(username);
//            connectionInfo.setPassword(password);
//            connectionInfo.setAttributes(attributes);
//            newTable.setConnectionInfo(connectionInfo);
//            //Update old table in the report with the new table.
//            reportClientDocument.getDatabaseController().setTableLocation(origTable, newTable);
//            // Log on to the database
////            databaseController.logon(connectionInfo, username, password);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        return reportClientDocument;
//    }
}
