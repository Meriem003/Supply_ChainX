package com.supplychainx.logging;

import org.slf4j.MDC;


public class LoggingContext {

    private static final String USER_ID = "user_id";
    private static final String USER_ROLE = "user_role";
    private static final String BUSINESS_ID = "business_id";
    private static final String ENDPOINT = "endpoint";
    private static final String HTTP_STATUS = "http_status";
    private static final String LOG_TYPE = "log_type";

    public static void setUserId(Long userId) {
        if (userId != null) {
            MDC.put(USER_ID, userId.toString());
        }
    }

    public static void setUserRole(String role) {
        if (role != null) {
            MDC.put(USER_ROLE, role);
        }
    }

    public static void setBusinessId(String businessId) {
        if (businessId != null) {
            MDC.put(BUSINESS_ID, businessId);
        }
    }

    public static void setEndpoint(String endpoint) {
        if (endpoint != null) {
            MDC.put(ENDPOINT, endpoint);
        }
    }


    public static void setHttpStatus(Integer status) {
        if (status != null) {
            MDC.put(HTTP_STATUS, status.toString());
        }
    }

    public static void setLogType(LogType logType) {
        if (logType != null) {
            MDC.put(LOG_TYPE, logType.name());
        }
    }


    public static void clear() {
        MDC.clear();
    }


    public static void clearBusinessId() {
        MDC.remove(BUSINESS_ID);
    }

    public enum LogType {
        APPLICATION,  
        SECURITY,     
        BUSINESS     
    }
}

