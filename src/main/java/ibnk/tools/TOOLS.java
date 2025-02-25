package ibnk.tools;

import ibnk.models.internet.client.Subscriptions;
import ibnk.security.jwtConfig.MobileJwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
//import oshi.SystemInfo;
//import oshi.hardware.ComputerSystem;
//import oshi.hardware.HardwareAbstractionLayer;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class TOOLS {

    public static String getMotherboardSerialNumber() {
//        SystemInfo si = new SystemInfo();
//        HardwareAbstractionLayer hal = si.getHardware();
//        ComputerSystem cs = hal.getComputerSystem();
        return "";
//                cs.getBaseboard().getSerialNumber()+" "+getComputerName();
    }

//    private static final List<String> TRUSTED_PROXIES = Arrays.asList("192.168.1.10", "127.0.0.1");

    public static String getClientIp(HttpServletRequest request, List<String> TRUSTED_PROXIES) {
        String proxyIp = request.getRemoteAddr();
        if (TRUSTED_PROXIES.contains(proxyIp)) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }
        }
        return proxyIp; // Fallback to proxy IP if not trusted
    }
//    public String getClientIp(HttpServletRequest request) {
//        String ipAddress = request.getHeader("X-Forwarded-For");
//        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
//            ipAddress = request.getRemoteAddr();
//        } else {
//            // X-Forwarded-For can contain multiple IPs (comma-separated), use the first one
//            ipAddress = ipAddress.split(",")[0].trim();
//        }
//        return ipAddress;
//    }

    public static String getClientid(String accountid){
         return accountid.substring(3, accountid.length() - 2);
    }
    public static String getComputerName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown Host";
        }
    }


    public static String convertStreamToString(BufferedInputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        // StandardCharsets.UTF_8.name() > JDK 7
        return result.toString(StandardCharsets.US_ASCII.name());
    }
    public static String convertBinaryToHexString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            while ((bytesRead = bis.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }

        return hexString.toString();
    }

    public static LocalDateTime Expiration() {
        LocalDateTime now = LocalDateTime.now();
        // add 10 minutes to the current date and time
        return now.plusMinutes(3);

    }
   public static ResponseEntity<Object> getObjectResponseEntity(String mimiType, byte[] pdfBytes) {
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        HttpHeaders headers = new HttpHeaders();
        if (mimiType.equalsIgnoreCase("application/pdf")) {
            headers.setContentType(MediaType.valueOf("application/pdf"));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated.pdf");
        } else {
            headers.setContentType(MediaType.valueOf("application/excel"));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated.xls");
        }
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

//    public static CompletableFuture<String> simpleEmail(String to, String subject, String text){
//        return sendSimpleMessage(to,subject,text);
//    }

    public static String generateAccessToken(Subscriptions client) throws Exception {
        MobileJwt jwt = new MobileJwt();
        return jwt.generateJwtToken(client);
    }

    public static void addError(List<Map<String, String>> errors, String key, String value) {
        Map<String, String> error = new HashMap<>();
        error.put(key, value);
        errors.add(error);
    }

    public static LocalDate firstDayOfThisMonth() {
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Get the first date of the current month
        LocalDate firstDateOfMonth = currentDate.withDayOfMonth(1);

        System.out.println("First date of the current month: " + firstDateOfMonth);
        return firstDateOfMonth;
    }

    public static LocalDate firstDayOfPreviousMonth() {
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth previousYearMonth = currentYearMonth.minusMonths(1);
        LocalDate firstDateOfPreviousMonth = previousYearMonth.atDay(1);

        System.out.println("First date of the previous month: " + firstDateOfPreviousMonth);
        return firstDateOfPreviousMonth;
    }

    public static LocalDate lastDayOfPreviousMonth() {
        // Get the current YearMonth
        YearMonth currentYearMonth = YearMonth.now();
        // Subtract one month to get the previous YearMonth
        YearMonth previousYearMonth = currentYearMonth.minusMonths(1);
        // Get the last date of the previous month
        LocalDate lastDateOfPreviousMonth = previousYearMonth.atEndOfMonth();
        System.out.println("Last date of the previous month: " + lastDateOfPreviousMonth);
        return lastDateOfPreviousMonth;
    }

    public static LocalDate firstDayOfPreviousQuarterMonth() {
        LocalDate currentDate = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(currentDate);

        // Calculate the first day of the current quarter
        LocalDate firstDayOfCurrentQuarter = currentYearMonth
                .atDay(1)
                .with(TemporalAdjusters.firstDayOfMonth());

        // Calculate the first day of the previous quarter
        LocalDate firstDayOfPreviousQuarter = firstDayOfCurrentQuarter
                .minusMonths(3);

        System.out.println("First day of the previous quarter: " + firstDayOfPreviousQuarter);
        return firstDayOfPreviousQuarter;
    }

    public static LocalDate lastDayOfPreviousQuarterMonth() {
        LocalDate currentDate = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(currentDate);

        // Calculate the first day of the current quarter
        LocalDate firstDayOfCurrentQuarter = currentYearMonth
                .atDay(1)
                .with(TemporalAdjusters.firstDayOfMonth());

        // Calculate the last day of the previous quarter
        LocalDate lastDayOfPreviousQuarter = firstDayOfCurrentQuarter
                .minusMonths(1) // Go back one month to reach the previous quarter
                .with(TemporalAdjusters.lastDayOfMonth());

        System.out.println("Last day of the previous quarter: " + lastDayOfPreviousQuarter);
        return lastDayOfPreviousQuarter;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            return fileName.substring(dotIndex + 1);
        } else {
            return ""; // No extension found
        }
    }
    @Data
    static class DateRangeDto {
        private String from;
        private String to;
    }
}
