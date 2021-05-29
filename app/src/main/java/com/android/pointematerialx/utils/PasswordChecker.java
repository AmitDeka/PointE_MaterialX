package com.android.pointematerialx.utils;

import java.util.regex.Pattern;

public class PasswordChecker {
    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])"
                    + "(?=.*[a-z])(?=.*[A-Z])"
                    + "(?=.*[@#$%^&+=])"
                    + "(?=\\S+$).{8,20}$");
}
