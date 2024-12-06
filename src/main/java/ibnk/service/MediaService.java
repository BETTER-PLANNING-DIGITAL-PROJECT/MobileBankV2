package ibnk.service;

import com.crystaldecisions.sdk.occa.report.application.*;
import com.crystaldecisions.sdk.occa.report.data.*;
import com.crystaldecisions.sdk.occa.report.exportoptions.DataOnlyExcelExportFormatOptions;
import com.crystaldecisions.sdk.occa.report.exportoptions.ExportOptions;
import com.crystaldecisions.sdk.occa.report.exportoptions.ReportExportFormat;
import com.crystaldecisions.sdk.occa.report.lib.*;
import ibnk.models.banking.InstitutionEntity;
import ibnk.models.internet.Media;
import ibnk.models.rptBanking.RptLogoEntity;
import ibnk.repositories.banking.InstitutionRepository;
import ibnk.repositories.internet.BankRepository;
import ibnk.repositories.internet.MediaRepository;
import ibnk.repositories.rptBanking.RptLogoRepository;
import ibnk.tools.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final BankRepository info;
    private static final   String REPORT_PATH = "C:/Reports";
//    @Qualifier("rptBankingJdbcTemplate")
//    private final JdbcTemplate bankingJdbcTemplate;
    private final InstitutionRepository institutionRepository;
    private final RptLogoRepository rptLogoRepository;

    public Media save(Media media) {
        return mediaRepository.save(media);
    }

    public Optional<Media> findByUuid(String uuid) {
        return mediaRepository.findByUuid(uuid);
    }

    // TODO BOTH Methods addDiscreteParameterValue and setParameterValue set Crystal Report Parameter value
    private void setParameterValue(ReportClientDocument reportClientDocument,
                                   Map.Entry<String, Object> item) throws Exception {
        ParameterFieldController parameterFieldController;
        parameterFieldController = reportClientDocument.getDataDefController().getParameterFieldController();

        String name = item.getKey();
        var parameters = reportClientDocument.getDataDefController().getDataDefinition().getParameterFields();
        for (var field : parameters) {
            try {
                if (field.getParameterType() != ParameterFieldType.reportParameter) {
                    continue;
                }
                if (!field.getName().equalsIgnoreCase(name)) {
                    continue;
                }

                System.out.println("hi " + field.getName() + " " + field.getType().toVariantTypeString() + " " + field.getType());
                System.out.println(name);

                field.getType();

                Object val = item.getValue();
                String theValue = "";
                switch (field.getType().toVariantTypeString().toLowerCase()) {
                    case "string" -> {
                        theValue = val.toString();
                        if (theValue == null || theValue.isEmpty()) {
                            theValue = "";
                        }
                        parameterFieldController.setCurrentValue("", name, theValue);
                    }
                    case "boolean", "i1" -> {
                        if (val.toString() == null || val.toString().isEmpty()) {
                            theValue = "false";
                        } else {
                            theValue = val.toString();
                        }
                        if (Objects.equals(theValue, "0")) {
                            theValue = "false";
                        } else if (Objects.equals(theValue, "1")) {
                            theValue = "true";
                        }
                        boolean toSetValue = Boolean.parseBoolean(theValue);
                        parameterFieldController.setCurrentValue("", name, toSetValue);
                    }
                    case "number", "decimal" -> {
                        if (val.toString() == null || val.toString().isEmpty()) {
                            theValue = "0";
                        } else {
                            theValue = val.toString();
                        }
                        try {
                            int toSetValueInt = Integer.parseInt(theValue);
                            parameterFieldController.setCurrentValue("", name, toSetValueInt);
                        } catch (Exception e) {
                            // TODO: should this be a BigDecimal?
                            double toSetDouble = Double.parseDouble(theValue);
                            parameterFieldController.setCurrentValue("", name, toSetDouble);
                        }
                    }
                    case "date", "datetime" -> {
                        Date dateValue;
                        if (val.toString() == null || val.toString().isEmpty()) {
                            dateValue = new Date();
                        } else {

                            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                    .toFormatter();
                            dateValue = Date.from(LocalDateTime.parse(val.toString(), formatter).toInstant(ZoneOffset.ofHours(0)));
                        }

                        //parameterFieldController.setCurrentValue("", name, Date..parseDouble(theValue));
                        parameterFieldController.setCurrentValue("", name, dateValue);
                    }
                    default -> {
                        theValue = val.toString();
                        if (val.toString() == null || val.toString().isEmpty()) {
                            theValue = "";
                        }
                        parameterFieldController.setCurrentValue("", name, theValue);
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new Exception((ex.getMessage()));
            }
        }
    }

    public static void addDiscreteParameterValue(ReportClientDocument clientDoc, String reportName, String parameterName, Object newValue) throws ReportSDKException {
        DataDefController dataDefController = null;
        if (reportName.equals("")) {
            dataDefController = clientDoc.getDataDefController();
        } else {
            dataDefController = clientDoc.getSubreportController().getSubreport(reportName).getDataDefController();
        }

        ParameterFieldDiscreteValue newDiscValue = new ParameterFieldDiscreteValue();
        newDiscValue.setValue(newValue);
        ParameterField paramField = (ParameterField) dataDefController.getDataDefinition().getParameterFields().findField(parameterName, FieldDisplayNameType.fieldName, Locale.getDefault());
        System.out.println(paramField.getName());
        boolean multiValue = paramField.getAllowMultiValue();
        if (multiValue) {
            Values newVals = (Values) paramField.getCurrentValues().clone(true);
            newVals.add(newDiscValue);
            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue(reportName, parameterName, newVals);
        } else {
            clientDoc.getDataDefController().getParameterFieldController().setCurrentValue(reportName, parameterName, newValue);
        }

    }
    public byte[] exportReport(ReportClientDocument reportClientDocument) throws ReportSDKExceptionBase, IOException {
        try {
            // Open the report
            // Save changes to the report
            reportClientDocument.save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Export the report to PDF
//        crJavaHelper.exportExcelDataOnly()
        System.out.println("Second DEBUG");

        ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) reportClientDocument.getPrintOutputController().export(ReportExportFormat.PDF);
        //Use the Java I/O libraries to write the exported content to the file system.
        byte[] byteArray = new byte[byteArrayInputStream.available()];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteArrayInputStream.available());
        int x = byteArrayInputStream.read(byteArray, 0, byteArrayInputStream.available());
        byteArrayOutputStream.write(byteArray, 0, x);
        //Close streams.
        byteArrayInputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] exportExcelDataOnly(ReportClientDocument clientDoc) throws ReportSDKExceptionBase, IOException {
        DataOnlyExcelExportFormatOptions excelOptions = new DataOnlyExcelExportFormatOptions();
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExportFormatType(ReportExportFormat.recordToMSExcel);
        exportOptions.setFormatOptions(excelOptions);

        try {
            // Open the report
            // Save changes to the report
            clientDoc.save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // Export the report to PDF
//        crJavaHelper.exportExcelDataOnly()
        ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) clientDoc.getPrintOutputController().export(exportOptions);
        //Use the Java I/O libraries to write the exported content to the file system.
        byte[] byteArray = new byte[byteArrayInputStream.available()];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byteArrayInputStream.available());
        int x = byteArrayInputStream.read(byteArray, 0, byteArrayInputStream.available());
        byteArrayOutputStream.write(byteArray, 0, x);
        //Close streams.
        byteArrayInputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] stmtReport(String BankName, String Boite, String RepTitle, String Telephone, String Ville, String Employeeid, String ExportType) throws  ResourceNotFoundException {

        ReportClientDocument reportClientDocument = null;
        String reportFilePath = REPORT_PATH+"/StmtTest.rpt";
        String sql = "SELECT * FROM dbo.PrintStmt \n" +
                "WHERE employeeid = ? \n" +
                "ORDER BY DateOperation ASC, id ASC";
        try{
               // Check if file is readable
           if(Files.isWritable(Path.of(REPORT_PATH))){
               System.out.println("File is writable");
           } else {
               System.out.println("File is not writable");
           }
               if (Files.isReadable(Path.of(REPORT_PATH))) {
                   System.out.println("File is readable");
               } else {
                   System.out.println("File is not readable");
               }
            reportClientDocument = new ReportClientDocument();
            reportClientDocument.setReportAppServer(ReportClientDocument.inprocConnectionString);
            reportClientDocument.open(reportFilePath, OpenReportOptions._openAsReadOnly);
            info.changeDataSource(reportClientDocument, "");
            info.passResultSet(reportClientDocument, info.executeQuery(sql,Employeeid), "PrintStmt", "");
            addDiscreteParameterValue(reportClientDocument, "", "BankName", BankName);
            addDiscreteParameterValue(reportClientDocument, "", "Boite", Boite);
            addDiscreteParameterValue(reportClientDocument, "", "RepTitle", RepTitle);
            addDiscreteParameterValue(reportClientDocument, "", "Telephone", Telephone);
            addDiscreteParameterValue(reportClientDocument, "", "Ville", Ville);
            addDiscreteParameterValue(reportClientDocument, "", "employeeid", Employeeid);
            if (ExportType.equalsIgnoreCase("application/excel")) {
                return exportExcelDataOnly(reportClientDocument);
            } else {
                return exportReport(reportClientDocument);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new ResourceNotFoundException(e.getMessage());
        }
        // Handle exceptions
//        } finally {
//            // Close resources if needed
//            if (reportClientDocument != null) {
//                reportClientDocument.close();
//            }
//        }
    }public byte[] rptacctopeninfoMoReport(String account,String ExportType) throws ReportSDKExceptionBase, IOException, ResourceNotFoundException {
        ReportClientDocument reportClientDocument = null;
        String reportFilePath = REPORT_PATH+"/rptacctopeninfoMo.rpt";
       try{
               // Check if file is readable
           if(Files.isWritable(Path.of(REPORT_PATH))){
               System.out.println("File is writable");
           } else {
               System.out.println("File is not writable");
           }
               if (Files.isReadable(Path.of(REPORT_PATH))) {
                   System.out.println("File is readable");
               } else {
                   System.out.println("File is not readable");
               }
            reportClientDocument = new ReportClientDocument();
            reportClientDocument.setReportAppServer(ReportClientDocument.inprocConnectionString);
            reportClientDocument.open(reportFilePath, OpenReportOptions._openAsReadOnly);
            info.changeDataSource(reportClientDocument, "");
            addDiscreteParameterValue(reportClientDocument, "", "account", account);
           if (ExportType.equalsIgnoreCase("application/excel")) {
                return exportExcelDataOnly(reportClientDocument);
            } else {
                return exportReport(reportClientDocument);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new ResourceNotFoundException(e.getMessage());
        }


    }

    public InstitutionEntity logoSwitch()throws ResourceNotFoundException {
        InstitutionEntity instLogo = institutionRepository.findAll().stream().findFirst()
                  .orElseThrow(() -> new ResourceNotFoundException("Institution Logo"));
        RptLogoEntity rptLogo = rptLogoRepository.findAll().stream().findFirst()
                  .orElseThrow(() -> new ResourceNotFoundException("Institution Logo"));
        rptLogo.setLogo(instLogo.getLogo());
        rptLogoRepository.save(rptLogo);
        return instLogo;
      }
}


