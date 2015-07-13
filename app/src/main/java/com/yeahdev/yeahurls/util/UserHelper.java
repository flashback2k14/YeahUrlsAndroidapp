package com.yeahdev.yeahurls.util;

import com.yeahdev.yeahurls.model.User;
import com.yeahdev.yeahurls.model.UserCreds;

import java.util.Date;

public class UserHelper {

    public static User createUserObject(String email, String prov) {
        User u = new User();
        u.setEmailAddress(email);
        u.setProvider(prov);
        return u;
    }

    public static UserCreds createUserCredsObject(String id, long exDate) {
        UserCreds uc = new UserCreds();
        uc.setUserId(id);
        uc.setExpireDate(exDate);
        return uc;
    }

    public static boolean userStillLoggedIn(long userExpireDate) {
        return userExpireDate != 0 && userExpireDate != Math.round(new Date().getTime() / 1000.0);
    }
}
