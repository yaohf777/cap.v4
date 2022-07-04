package com.sap.rc.main.multitenancy;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.springframework.web.context.WebApplicationContext;

public class TenantContext {

    public static final String TENANT_ID = "tenantId";

    private static final String DEFAULT_TENANT = "public";

    private static ThreadLocal<LinkedHashMap<String, Object>> threadSession = new ThreadLocal<LinkedHashMap<String, Object>>();
    private static ThreadLocal<String> currentTenant = ThreadLocal.withInitial(() -> DEFAULT_TENANT);

    private TenantContext() {
        throw new IllegalStateException("Utility class");
    }

    // to be called as part of TenantFilter only
    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setUser(String userName) {
        initThreadSession();
        threadSession.get().put("user", userName);
    }

    public static String getUser() {
        if (threadSession.get() == null) {
            return null;
        }
        return (String) (threadSession.get().get("user"));
    }

    public static void setUserType(String userType) {
        initThreadSession();
        threadSession.get().put("userType", userType);
    }

    public static String getUserType() {
        if (threadSession.get() == null) {
            return null;
        }
        return (String) (threadSession.get().get("userType"));
    }

    public static void setLanguage(String language) {
        initThreadSession();
        threadSession.get().put("langu", language);
    }

    public static String getLanguage() {
        if (threadSession.get() == null) {
            return null;
        }
        return (String) (threadSession.get().get("langu"));
    }

    public static void setHttpSession(HttpSession httpSession) {
        initThreadSession();
        threadSession.get().put("httpSession", httpSession);
    }

    public static HttpSession getHttpSession() {
        if (threadSession.get() == null) {
            return null;
        }
        return (HttpSession)threadSession.get().get("httpSession");
    }

    public static void setAppContext(WebApplicationContext context) {
        initThreadSession();
        threadSession.get().put("appContext", context);
    }

    public static WebApplicationContext getAppContext() {
        if (threadSession.get() == null) {
            return null;
        }
        return (WebApplicationContext)threadSession.get().get("appContext");
    }

    public static DataSource getDatasource() {
        if (getAppContext() == null) {
            return null;
        }
        return (DataSource)getAppContext().getBean("dataSource");
    }

    
    public static void setObject(String key, Object object) {

        initThreadSession();
        threadSession.get().put(key, object);
    }

    public static Object getObject(String key) {
        if (threadSession.get() == null) {
            return null;
        }
        return threadSession.get().get(key);
    }

    // to be called as part of TenantFilter only to release the memory
    public static void clear() {
        currentTenant.remove();
        threadSession.set(null);
    }

    private static void initThreadSession() {
        if (threadSession.get() == null) {
            LinkedHashMap<String, Object> paramterMap = new LinkedHashMap<String, Object>();
            threadSession.set(paramterMap);
        }
    }
}
