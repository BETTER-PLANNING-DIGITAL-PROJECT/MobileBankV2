package ibnk.tools;

import com.crystaldecisions.sdk.occa.report.application.DataDefController;
import com.crystaldecisions.sdk.occa.report.application.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.data.*;
import com.crystaldecisions.sdk.occa.report.document.PaperSize;
import com.crystaldecisions.sdk.occa.report.document.PaperSource;
import com.crystaldecisions.sdk.occa.report.document.PrintReportOptions;
import com.crystaldecisions.sdk.occa.report.document.PrinterDuplex;
import com.crystaldecisions.sdk.occa.report.lib.IStrings;
import com.crystaldecisions.sdk.occa.report.lib.PropertyBag;
import com.crystaldecisions.sdk.occa.report.exportoptions.*;
import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;
import com.crystaldecisions.sdk.occa.report.lib.ReportSDKExceptionBase;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Locale;
// TODO This Class is for Reference on how to Manipulate certain Crystal Report Dependency
public class CRJavaHelper {

    public CRJavaHelper() {
    }

    /**
     * @param clientDoc The reportClientDocument representing the report being used
     * @param username  The DB logon user name
     * @param password  The DB logon password
     * @throws ReportSDKException
     */
    public static void logonDataSource(ReportClientDocument clientDoc, String username, String password) throws ReportSDKException {
        clientDoc.getDatabaseController().logon(username, password);
    }

    /**
     * Changes the DataSource for each Table
     * @param clientDoc The reportClientDocument representing the report being used
     * @param username The DB logon user name
     * @param password The DB logon password
     * @param connectionURL The connection URL
     * @param driverName The driver Name
     * @param jndiName The JNDI name
     * @throws ReportSDKException
     */
    public static void changeDataSource(ReportClientDocument clientDoc, String username, String password, String connectionURL, String driverName, String jndiName) throws ReportSDKException {
        changeDataSource(clientDoc, (String)null, (String)null, username, password, connectionURL, driverName, jndiName);
    }

    /**
     * Changes the DataSource for a specific Table
     * @param clientDoc The reportClientDocument representing the report being used
     * @param reportName    "" for main report, name of subreport for subreport, null for all reports
     * @param tableName        name of table to change.  null for all tables.
     * @param username  The DB logon user name
     * @param password  The DB logon password
     * @param connectionURL  The connection URL
     * @param driverName    The driver Name
     * @param jndiName        The JNDI name
     * @throws ReportSDKException
     */
    public static void changeDataSource(ReportClientDocument clientDoc, String reportName, String tableName, String username, String password, String connectionURL, String driverName, String jndiName) throws ReportSDKException {
        PropertyBag propertyBag = null;
        IConnectionInfo connectionInfo = null;
        ITable origTable = null;
        ITable newTable = null;
        String TRUSTED_CONNECTION = "false";
        String SERVER_TYPE = "JDBC (JNDI)";
        String USE_JDBC = "true";
        String DATABASE_DLL = "crdb_jdbc.dll";
        String JNDI_DATASOURCE_NAME = jndiName;
        String CONNECTION_URL = connectionURL;
        String DATABASE_CLASS_NAME = driverName;
        String DB_USER_NAME = username;
        String DB_PASSWORD = password;
        int subNum;
        if (reportName == null || reportName.equals("")) {
            Tables tables = clientDoc.getDatabaseController().getDatabase().getTables();

            for(subNum = 0; subNum < tables.size(); ++subNum) {
                origTable = tables.getTable(subNum);
                if (tableName == null || origTable.getName().equals(tableName)) {
                    newTable = (ITable)origTable.clone(true);
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
        }

        if (reportName == null || !reportName.equals("")) {
            IStrings subNames = clientDoc.getSubreportController().getSubreportNames();

            for(subNum = 0; subNum < subNames.size(); ++subNum) {
                Tables tables = clientDoc.getSubreportController().getSubreport(subNames.getString(subNum)).getDatabaseController().getDatabase().getTables();

                for(int i = 0; i < tables.size(); ++i) {
                    origTable = tables.getTable(i);
                    if (tableName == null || origTable.getName().equals(tableName)) {
                        newTable = (ITable)origTable.clone(true);
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

    }

    /**
     * Passes a populated java.sql.Resultset object to a Table object
     * @param clientDoc The reportClientDocument representing the report being used
     * @param rs The java.sql.Resultset used to populate the Table
     * @param tableAlias The alias of the table
     * @param reportName The name of the subreport.  If tables in the main report
     *      *                         is to be used, "" should be passed
     * @throws ReportSDKException
     */
    public static void passResultSet(ReportClientDocument clientDoc, ResultSet rs, String tableAlias, String reportName) throws ReportSDKException {
        if (reportName.equals("")) {
            clientDoc.getDatabaseController().setDataSource(rs, tableAlias, tableAlias);
        } else {
            clientDoc.getSubreportController().getSubreport(reportName).getDatabaseController().setDataSource(rs, tableAlias, tableAlias);
        }

    }

    /**
     * Passes a populated collection of a Java class to a Table object
     *
     * @param clientDoc     The reportClientDocument representing the report being used
     * @param dataSet        The java.sql.Resultset used to populate the Table
     * @param className        The fully-qualified class name of the POJO objects being passed
     * @param tableAlias        The alias of the table
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @throws ReportSDKException
     */
    public static void passPOJO(ReportClientDocument clientDoc, Collection dataSet, String className, String tableAlias, String reportName) throws ReportSDKException, ClassNotFoundException {
        if (reportName.equals("")) {
            clientDoc.getDatabaseController().setDataSource(dataSet, Class.forName(className), tableAlias, tableAlias);
        } else {
            clientDoc.getSubreportController().getSubreport(reportName).getDatabaseController().setDataSource(dataSet, Class.forName(className), tableAlias, tableAlias);
        }

    }

    /**
     * Passes a single discrete parameter value to a report parameter
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @param parameterName    The name of the parameter
     * @param newValue        The new value of the parameter
     * @throws ReportSDKException
     */
    public static void addDiscreteParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object newValue) throws ReportSDKException {
        DataDefController dataDefController = null;
        if (reportName.equals("")) {
            dataDefController = clientDoc.getDataDefController();
        } else {
            dataDefController = clientDoc.getSubreportController().getSubreport(reportName).getDataDefController();
        }

        ParameterFieldDiscreteValue newDiscValue = new ParameterFieldDiscreteValue();
        newDiscValue.setValue(newValue);
        ParameterField paramField = (ParameterField)dataDefController.getDataDefinition().getParameterFields().findField(parameterName, FieldDisplayNameType.fieldName, Locale.getDefault());
        System.out.println(paramField.getName());
        boolean multiValue = paramField.getAllowMultiValue();
        if (multiValue) {
            Values newVals = (Values)paramField.getCurrentValues().clone(true);
            newVals.add(newDiscValue);
            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue(reportName, parameterName, newVals);
        } else {
            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue(reportName, parameterName, newValue);
        }

    }
    /**
     * Passes multiple discrete parameter values to a report parameter
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @param parameterName    The name of the parameter
     * @param newValues        An array of new values to get set on the parameter
     * @throws ReportSDKException
     */

    public static void addDiscreteParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object[] newValues) throws ReportSDKException {
        clientDoc.getDataDefController().getParameterFieldController().setCurrentValues(reportName, parameterName, newValues);
    }

    /**
     * Passes a single range parameter value to a report parameter.  The range is assumed to
     * be inclusive on beginning and end.
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @param parameterName    The name of the parameter
     * @param beginValue    The value of the beginning of the range
     * @param endValue        The value of the end of the range
     * @throws ReportSDKException
     */

    public static void addRangeParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object beginValue, Object endValue) throws ReportSDKException {
        addRangeParameterValue(clientDoc, reportName, parameterName, beginValue, RangeValueBoundType.inclusive, endValue, RangeValueBoundType.inclusive);
    }
    /**
     * Passes multiple range parameter values to a report parameter.
     *
     * This overload of the addRangeParameterValue will only work if the
     * parameter is setup to accept multiple values.
     *
     * If the Parameter does not accept multiple values then it is expected that
     * this version of the method will return an error
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @param parameterName    The name of the parameter
     * @param beginValues    Array of beginning values.  Must be same length as endValues.
     * @param endValues        Array of ending values.  Must be same length as beginValues.
     * @throws ReportSDKException
     */

    public static void addRangeParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object[] beginValues, Object[] endValues) throws ReportSDKException {
        addRangeParameterValue(clientDoc, reportName, parameterName, beginValues, RangeValueBoundType.inclusive, endValues, RangeValueBoundType.inclusive);
    }

    /**
     * Passes a single range parameter value to a report parameter
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @param parameterName    The name of the parameter
     * @param beginValue    The value of the beginning of the range
     * @param lowerBoundType    The inclusion/exclusion range of the start of range.
     * @param endValue        The value of the end of the range
     * @param upperBoundType    The inclusion/exclusion range of the end of range.
     * @throws ReportSDKException
     */
    public static void addRangeParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object beginValue, RangeValueBoundType lowerBoundType, Object endValue, RangeValueBoundType upperBoundType) throws ReportSDKException {
        DataDefController dataDefController = null;
        if (reportName.equals("")) {
            dataDefController = clientDoc.getDataDefController();
        } else {
            dataDefController = clientDoc.getSubreportController().getSubreport(reportName).getDataDefController();
        }

        ParameterFieldRangeValue newRangeValue = new ParameterFieldRangeValue();
        newRangeValue.setBeginValue(beginValue);
        newRangeValue.setLowerBoundType(lowerBoundType);
        newRangeValue.setEndValue(endValue);
        newRangeValue.setUpperBoundType(upperBoundType);
        ParameterField paramField = (ParameterField)dataDefController.getDataDefinition().getParameterFields().findField(parameterName, FieldDisplayNameType.fieldName, Locale.getDefault());
        boolean multiValue = paramField.getAllowMultiValue();
        if (multiValue) {
            Values newVals = (Values)paramField.getCurrentValues().clone(true);
            newVals.add(newRangeValue);
            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue(reportName, parameterName, newVals);
        } else {
            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue(reportName, parameterName, newRangeValue);
        }

    }

    /**
     * Passes multiple range parameter values to a report parameter.
     *
     * This overload of the addRangeParameterValue will only work if the
     * parameter is setup to accept multiple values.
     *
     * If the Parameter does not accept multiple values then it is expected that
     * this version of the method will return an error
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param reportName    The name of the subreport.  If tables in the main report
     *                         is to be used, "" should be passed
     * @param parameterName    The name of the parameter
     * @param beginValues    Array of beginning values.  Must be same length as endValues.
     * @param lowerBoundType    The inclusion/exclusion range of the start of range.
     * @param endValues        Array of ending values.  Must be same length as beginValues.
     * @param upperBoundType    The inclusion/exclusion range of the end of range.
     *
     * @throws ReportSDKException
     */
    public static void addRangeParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object[] beginValues, RangeValueBoundType lowerBoundType, Object[] endValues, RangeValueBoundType upperBoundType) throws ReportSDKException {
        ParameterFieldRangeValue[] newRangeValues = new ParameterFieldRangeValue[beginValues.length];

        for(int i = 0; i < beginValues.length; ++i) {
            newRangeValues[i] = new ParameterFieldRangeValue();
            newRangeValues[i].setBeginValue(beginValues[i]);
            newRangeValues[i].setLowerBoundType(lowerBoundType);
            newRangeValues[i].setEndValue(endValues[i]);
            newRangeValues[i].setUpperBoundType(upperBoundType);
        }

        clientDoc.getDataDefController().getParameterFieldController().setCurrentValues(reportName, parameterName, newRangeValues);
    }

    /**
     * Exports a report to PDF
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param response        The HttpServletResponse object
     * @param attachment    true to prompts for open or save; false opens the report
     *                         in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportPDF(ReportClientDocument clientDoc, HttpServletResponse response, boolean attachment) throws ReportSDKExceptionBase, IOException {
        PDFExportFormatOptions pdfOptions = new PDFExportFormatOptions();
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.PDF);
        exportOptions.setFormatOptions(pdfOptions);
        export(clientDoc, exportOptions, response, attachment, "application/pdf", "pdf");
    }
    /**
     * Exports a report to PDF for a range of pages
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param response        The HttpServletResponse object
     * @param startPage        Starting page
     * @param endPage        Ending page
     * @param attachment    true to prompts for open or save; false opens the report
     *                         in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportPDF(ReportClientDocument clientDoc, HttpServletResponse response, ServletContext context, int startPage, int endPage, boolean attachment) throws ReportSDKExceptionBase, IOException {
        PDFExportFormatOptions pdfOptions = new PDFExportFormatOptions();
        pdfOptions.setStartPageNumber(startPage);
        pdfOptions.setEndPageNumber(endPage);
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.PDF);
        exportOptions.setFormatOptions(pdfOptions);
        export(clientDoc, exportOptions, response, attachment, "application/pdf", "pdf");
    }

    /**
     * Exports a report to RTF
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param response        The HttpServletResponse object
     * @param attachment    true to prompts for open or save; false opens the report
     *                         in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportRTF(ReportClientDocument clientDoc, HttpServletResponse response, boolean attachment) throws ReportSDKExceptionBase, IOException {
        RTFWordExportFormatOptions rtfOptions = new RTFWordExportFormatOptions();
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.RTF);
        exportOptions.setFormatOptions(rtfOptions);
        export(clientDoc, exportOptions, response, attachment, "text/rtf", "rtf");
    }
    /**
     * Exports a report to RTF for a range of pages
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param response        The HttpServletResponse object
     * @param startPage        Starting page
     * @param endPage        Ending page.
     * @param attachment    true to prompts for open or save; false opens the report
     *                         in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportRTF(ReportClientDocument clientDoc, HttpServletResponse response, ServletContext context, int startPage, int endPage, boolean attachment) throws ReportSDKExceptionBase, IOException {
        RTFWordExportFormatOptions rtfOptions = new RTFWordExportFormatOptions();
        rtfOptions.setStartPageNumber(startPage);
        rtfOptions.setEndPageNumber(endPage);
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.RTF);
        exportOptions.setFormatOptions(rtfOptions);
        export(clientDoc, exportOptions, response, attachment, "text/rtf", "rtf");
    }
    /**
     * Exports a report to RTF
     *
     * @param clientDoc     The reportClientDocument representing the report being used
     * @param response      The HttpServletResponse object
     * @param attachment    true to prompts for open or save; false opens the report
     *                      in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportRTFEditable(ReportClientDocument clientDoc, HttpServletResponse response, boolean attachment) throws ReportSDKExceptionBase, IOException {
        EditableRTFExportFormatOptions rtfOptions = new EditableRTFExportFormatOptions();
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.editableRTF);
        exportOptions.setFormatOptions(rtfOptions);
        export(clientDoc, exportOptions, response, attachment, "text/rtf", "rtf");
    }
    /**
     * Exports a report to RTF for a range of pages
     *
     * @param clientDoc     The reportClientDocument representing the report being used
     * @param response      The HttpServletResponse object
     * @param startPage     Starting page
     * @param endPage       Ending page.
     * @param attachment    true to prompts for open or save; false opens the report
     *                      in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportRTFEditable(ReportClientDocument clientDoc, HttpServletResponse response, ServletContext context, int startPage, int endPage, boolean attachment) throws ReportSDKExceptionBase, IOException {
        EditableRTFExportFormatOptions rtfOptions = new EditableRTFExportFormatOptions();
        rtfOptions.setStartPageNumber(startPage);
        rtfOptions.setEndPageNumber(endPage);
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.editableRTF);
        exportOptions.setFormatOptions(rtfOptions);
        export(clientDoc, exportOptions, response, attachment, "text/rtf", "rtf");
    }
    /**
     * Exports a report to Excel (Data Only)
     *
     * @param clientDoc     The reportClientDocument representing the report being used
     * @param response      The HttpServletResponse object
     * @param attachment    true to prompts for open or save; false opens the report
     *                      in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportExcelDataOnly(ReportClientDocument clientDoc, HttpServletResponse response, boolean attachment) throws ReportSDKExceptionBase, IOException {
        DataOnlyExcelExportFormatOptions excelOptions = new DataOnlyExcelExportFormatOptions();
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.recordToMSExcel);
        exportOptions.setFormatOptions(excelOptions);
        export(clientDoc, exportOptions, response, attachment, "application/excel", "xls");
    }
    /**
     * Exports a report to CSV
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param response        The HttpServletResponse object
     * @param attachment    true to prompts for open or save; false opens the report
     *                         in the specified format after exporting.
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    public static void exportCSV(ReportClientDocument clientDoc, HttpServletResponse response, boolean attachment) throws ReportSDKExceptionBase, IOException {
        CharacterSeparatedValuesExportFormatOptions csvOptions = new CharacterSeparatedValuesExportFormatOptions();
        csvOptions.setSeparator(",");
        csvOptions.setDelimiter("\n");
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.characterSeparatedValues);
        exportOptions.setFormatOptions(csvOptions);
        export(clientDoc, exportOptions, response, attachment, "text/csv", "csv");
    }
    /**
     * Exports a report to a specified format
     *
     * @param clientDoc       The reportClientDocument representing the report being used
     * @param exportOptions   Export options
     * @param response        The response object to write to
     * @param attachment      True to prompts for open or save; false opens the report
     *                        in the specified format after exporting.
     * @param mimeType        MIME type of the format being exported
     * @param extension       file extension of the format (e.g., "pdf" for Acrobat)
     * @throws ReportSDKExceptionBase
     * @throws IOException
     */
    private static void export(ReportClientDocument clientDoc, ExportOptions exportOptions, HttpServletResponse response, boolean attachment, String mimeType, String extension) throws ReportSDKExceptionBase, IOException {
        InputStream is = null;

        try {
            is = new BufferedInputStream(clientDoc.getPrintOutputController().export(exportOptions));
            byte[] data = new byte[1024];
            response.setContentType(mimeType);
            if (attachment) {
                String name = clientDoc.getReportSource().getReportTitle();
                if (name == null) {
                    name = "CrystalReportViewer";
                } else {
                    name = name.replaceAll("\"", "");
                }

                response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "." + extension + "\"");
            }

            OutputStream os = response.getOutputStream();

            while(is.read(data) > -1) {
                os.write(data);
            }
        } finally {
            if (is != null) {
                is.close();
            }

        }

    }
    /**
     * Prints to the server printer
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param printerName    Name of printer used to print the report
     * @throws ReportSDKException
     */
    public static void printToServer(ReportClientDocument clientDoc, String printerName) throws ReportSDKException {
        PrintReportOptions printOptions = new PrintReportOptions();
        printOptions.setPrinterName(printerName);
        printOptions.setJobTitle("Sample Print Job from Crystal Reports.");
        printOptions.setPrinterDuplex(PrinterDuplex.useDefault);
        printOptions.setPaperSource(PaperSource.auto);
        printOptions.setPaperSize(PaperSize.paperLetter);
        printOptions.setNumberOfCopies(1);
        printOptions.setCollated(false);
        clientDoc.getPrintOutputController().printReport(printOptions);
    }
    /**
     * Prints a range of pages to the server printer
     *
     * @param clientDoc        The reportClientDocument representing the report being used
     * @param printerName    Name of printer used to print the report
     * @param startPage        Starting page
     * @param endPage        Ending page.
     * @throws ReportSDKException
     */
    public static void printToServer(ReportClientDocument clientDoc, String printerName, int startPage, int endPage) throws ReportSDKException {
        PrintReportOptions printOptions = new PrintReportOptions();
        printOptions.setPrinterName(printerName);
        printOptions.setJobTitle("Sample Print Job from Crystal Reports.");
        printOptions.setPrinterDuplex(PrinterDuplex.useDefault);
        printOptions.setPaperSource(PaperSource.auto);
        printOptions.setPaperSize(PaperSize.paperLetter);
        printOptions.setNumberOfCopies(1);
        printOptions.setCollated(false);
        PrintReportOptions.PageRange printPageRange = new PrintReportOptions.PageRange(startPage, endPage);
        printOptions.addPrinterPageRange(printPageRange);
        clientDoc.getPrintOutputController().printReport(printOptions);
    }
}
