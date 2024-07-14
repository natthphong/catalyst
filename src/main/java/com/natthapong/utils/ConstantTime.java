package com.natthapong.utils;

public class ConstantTime {

        public static final String ISO8601_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        public static final String FORMAT_ZONED_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssXXX";
        public static final String FORMAT_LOCAL_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
        public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
        public static final String FORMAT_LOCAL_DATE = "yyyy-MM-dd";
        public static final String FORMAT_ISO_DATE = "yyyyMMdd";
        public static final String FORMAT_LOCAL_TIME = "HH:mm:ss";
        public static final String BANGKOK_ZONE = "Asia/Bangkok";
        public static final String BANGKOK_ICT = "+07:00";


        private ConstantTime() throws IllegalAccessException {
            throw new IllegalAccessException("Constant Class");
        }




}
