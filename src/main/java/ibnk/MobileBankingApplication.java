package ibnk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableWebSecurity
public class MobileBankingApplication {

//    @Bean
//    CommandLineRunner lookupTestService(TestService testService) {
//        return args -> {
//
//            // 1、test interface
//            testService.test();
//
//        };
//    }

    public static void main(String[] args) throws Exception {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt =  LocalDateTime.now().atZone(zoneId);
        DateTimeFormatter smp = DateTimeFormatter.ofPattern("YYYY-MM-DD'T'HH:MM:SSZ");
        System.out.println(zdt.toLocalDateTime().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)+"      "+"kkhkhkhkhkhkhkhkhkhkhhkkkkkk");
        SpringApplication.run(MobileBankingApplication.class, args);
        String packageName = "ibnk.webController";
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        for (Class<?> clazz : classes) {
            getMethods(clazz);
//			System.out.println(clazz.getName());
        }
    }
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    public static void getMethods(Class<?> clazz) throws Exception {
        Method[] methods = clazz.getDeclaredMethods();
        Properties props = new Properties();
        InputStream inputStream = MobileBankingApplication.class.getClassLoader().getResourceAsStream("application.properties");
        props.load(inputStream);
        String url = props.getProperty("spring.datasource.jdbcUrl");
        String username = props.getProperty("spring.datasource.username");
        String password = props.getProperty("spring.datasource.password");
        Connection conn = DriverManager.getConnection(url, username, password);
        for (Method method : methods) {
            if (method.isAnnotationPresent(PreAuthorize.class)) {
                PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
                String value = annotation.value();
                if (value.contains("hasRole")) {
                    String role = value.substring(value.indexOf("(") + 2, value.indexOf(")") - 1);
                    insertToDatabase(role, conn);
                }
            }
            conn.close();
        }
    }

    private static void insertToDatabase(String role, Connection conn) throws Exception {
        String query = "SELECT COUNT(*) FROM permissions WHERE name = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, role);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        if (count == 0) {
            String insertQuery = "INSERT INTO permissions (name) VALUES (?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, role);
            insertStmt.executeUpdate();
            insertStmt.close();
            System.out.println(role + " inserted successfully.");
        } else {
            System.out.println(role + " already exists.");
        }

    }

}
