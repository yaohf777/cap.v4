package com.sap.rc.sample;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.exception.HystrixRuntimeException;

@Component // defines a Spring Bean with name "userServiceClient"
public class UserServiceClient {
    private static final String PATH = "api/v1.0/users";

    private String userServiceRoute = "https://opensapcp5userservice.cfapps.us10.hana.ondemand.com";
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectFactory<GetUserCommand> objectFactory;

    @Inject
    public UserServiceClient(ObjectFactory<GetUserCommand> objectFactory) {
        this.objectFactory = objectFactory;
    }

    public boolean isPremiumUser(String id) throws RuntimeException {
        String url = userServiceRoute + "/" + PATH + "/" + id;
        boolean isPremiumUser = false;
        try {
            // creates a new one, as it is declared as "Prototype"
            GetUserCommand getUserCommand = objectFactory.getObject();
            getUserCommand.setUrl(url);
            User user = getUserCommand.execute();
            isPremiumUser = user.premiumUser;
        } catch (HystrixRuntimeException ex) {
            logger.warn("[HystrixFailure:" + ex.getFailureType().toString() + "] " + ex.getMessage());
        }
        return isPremiumUser;
    }

    public static class User {
        // public, so that Jackson can access the field
        public boolean premiumUser;
    }
}
