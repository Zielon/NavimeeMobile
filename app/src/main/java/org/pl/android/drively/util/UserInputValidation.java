package org.pl.android.drively.util;


public class UserInputValidation {

    public static boolean isPasswordValid(String password) {
        return !password.isEmpty() && password.length() >= 6 && password.length() <= 10;
    }

    public static boolean isEmailValid(String email) {
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
