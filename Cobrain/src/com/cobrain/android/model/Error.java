package com.cobrain.android.model;

import com.cobrain.android.service.web.WebRequest;

public class Error {
    public static final class Codes {
        public static final String INVALID_PHONE_NUMBER = "Invalid Phone Number";
        public static final String INVALID_ARGUMENT = "InvalidArgument";
    }

    public String code;
    public String message;
    public transient WebRequest request;
}
