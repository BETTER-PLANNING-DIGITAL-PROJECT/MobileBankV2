package ibnk.tools.security;

import ibnk.tools.error.UnauthorizedUserException;
import ibnk.tools.error.ValidationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordConstraintValidator {

    // ()~`-=_+[]{}|:\";',./<>
    public static final String SPECIAL_CHARACTERS = "!@#$%^&*?";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 20;

    public static boolean isAcceptablePassword(String password) throws  ValidationException {

        if (password.length() >= 8) {
            Pattern letter = Pattern.compile("[a-zA-z]");
            Pattern digit = Pattern.compile("[0-9]");
//            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
            // Pattern eight = Pattern.compile (".{8}");

            Matcher hasLetter = letter.matcher(password);
            Matcher hasDigit = digit.matcher(password);
//            Matcher hasSpecial = special.matcher(password);

            if (!hasLetter.find()) {
                throw new ValidationException("password must contain Letter Character.");

            }
            if (!hasDigit.find()) {
                throw new ValidationException("password must contain number Character.");

            }
//            else if (!hasSpecial.find()) {
//                throw new ValidationException("password must contain Special Character.");
//
//            }
            return true;

        } else
            throw new ValidationException("password size, it must have at least 8 characters and less than 20.");

        // return false;

    }

    public static boolean isValidCameroonPhoneNumber(String phoneNumber) {
        // Regular expression for validating Cameroon phone numbers
        String cameroonPhoneNumberRegex = "^(?:\\+?237|0)(6[5-9]|2[1-3]|7[067]|3[0-9]|9[6-9])[0-9]{6}$";
        // Test the phone number against the regex pattern
        return !Pattern.matches(cameroonPhoneNumberRegex, phoneNumber);
    }

    public static boolean isValidEmail(String email) {
        // Regular expression for validating email addresses
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        // Test the email against the regex pattern
        return Pattern.matches(emailRegex,email);
    }

    public static boolean isAcceptableTelephone(String tel) throws UnauthorizedUserException {

        Pattern compile = Pattern.compile("^\\d{9}$");

        Matcher m = compile.matcher(tel);
        if (!m.find()) {
            throw new UnauthorizedUserException("Telephone size, it must have at least 9.");
        }
        return true;
    }
}
