package org.example.lab3;

import java.util.Map;

class User
{

    private static final int EMAIL_MAX_LENGTH = 50;
    private static final int PASSWORD_MAX_LENGTH = 12;
    private static final int PASSWORD_MIN_LENGTH = 8;

    private static final Map<String, String> ERRORS = Map.of(
            "INVALID_EMAIL", "Please enter a valid Email as username",
            "TOO_LONG_EMAIL", "Username is too long, try something shorter",
            "INVALID_PASSWORD", "Please enter a valid password",
            "TOO_SHORT_PASSWORD", "Your password is too short, add more characters",
            "TOO_LONG_PASSWORD", "Your password is too long, try a shorter one"
    );

    private static final String ALLOWED_SPECIAL_CHARS_PASSWORD = "!@#$%^&*()_+";
    private static final String ALLOWED_SPECIAL_CHARS_EMAIL = "-+%_.";

    private final String email;
    private final String password;

    public User(String login, String psw)
    {
        check_email(login);
        check_password(psw);
        email = login;
        password = psw;
    }

    private boolean check_email(String str){
        if (str.length()>EMAIL_MAX_LENGTH) throw new IllegalArgumentException(ERRORS.get("TOO_LONG_EMAIL"));
        String[] parts =  str.split("@");
        if (parts.length != 2) throw new IllegalArgumentException(ERRORS.get("INVALID_EMAIL")); // if there are more, than one @ -> email is invalid

        if (parts[0].isEmpty() || parts[1].isEmpty()) throw new IllegalArgumentException(ERRORS.get("INVALID_EMAIL")); // if 1st or 2nd+3rd parts of the email are empty -> email is invalid

        for(int i=0;i<parts[0].length();i++){ // Checking first part
            if(!(((int)parts[0].charAt(i)>=(int)'a' && (int)parts[0].charAt(i)<=(int)'z') || //small letters
                    ((int)parts[0].charAt(i)>=(int)'A' && (int)parts[0].charAt(i)<=(int)'Z') //big letters
                    || ALLOWED_SPECIAL_CHARS_EMAIL.indexOf(parts[0].charAt(i))!=-1)) // special chars
                throw new IllegalArgumentException(ERRORS.get("INVALID_EMAIL"));
        }
        for(int i=0;i<parts[1].length();i++){ // Checking 2nd and 3rd parts
            if(!(((int)parts[1].charAt(i)>=(int)'a' && (int)parts[1].charAt(i)<=(int)'z') || // small letters
                    ((int)parts[1].charAt(i)>=(int)'A' && (int)parts[1].charAt(i) <=(int)'Z') || // big letters
                    parts[1].charAt(i)=='-' || parts[1].charAt(i)=='.' || // special chars
                    ((int)parts[1].charAt(i)>=(int)'0' && (int)parts[1].charAt(i)<=(int)'9'))) // numbers
                throw new IllegalArgumentException(ERRORS.get("INVALID_EMAIL"));
        }

        String [] last_parts = parts[1].split("\\.");
        if (last_parts.length<2 || last_parts[last_parts.length-1].length() < 2) throw new IllegalArgumentException(ERRORS.get("INVALID_EMAIL")); // 3rd part has to be at least 2 chars.
        for (int i=0;i<last_parts[last_parts.length-1].length();i++){ // Checking, that last part has no additional symbols
            char let = last_parts[last_parts.length-1].charAt(i);
            if(!(((int)let>=(int)'a' && (int)let <= (int)'z') ||
                    ((int)let>=(int)'A' && (int)let <= (int)'Z')))
                throw new IllegalArgumentException(ERRORS.get("INVALID_EMAIL"));
        }
        return true;
    }

    private boolean check_password(String str){
        if(str.length()<PASSWORD_MIN_LENGTH) throw new IllegalArgumentException(ERRORS.get("TOO_SHORT_PASSWORD"));
        if(str.length()>PASSWORD_MAX_LENGTH) throw new IllegalArgumentException(ERRORS.get("TOO_LONG_PASSWORD"));
        boolean has_letter = false;
        boolean has_number = false;
        boolean has_special_char = false;

        for(int i=0; i<str.length(); i++){ // checking, that password has at least one letter (big or small), one number (0-9) and one special char.
            if(((int)str.charAt(i)>=(int)'a' && (int)str.charAt(i)<=(int)'z') ||
                    ((int)str.charAt(i)>=(int)'A' && (int)str.charAt(i)<=(int)'Z'))
                has_letter = true;
            else if((int)str.charAt(i)>=(int)'0' && (int)str.charAt(i)<=(int)'9') has_number = true;
            else if(ALLOWED_SPECIAL_CHARS_PASSWORD.indexOf(str.charAt(i))!=-1) has_special_char = true;
            else throw new IllegalArgumentException(ERRORS.get("INVALID_PASSWORD"));; // if password contains foreign symbol -> invalid password
        }

        if(has_letter && has_number && has_special_char) return true;
        else throw new IllegalArgumentException(ERRORS.get("INVALID_PASSWORD")); // if there is no letter / number / special char -> invalid password
    }

    public String getEmail(){
        return email;
    }
    public String getPassword(){
        return password;
    }
}
