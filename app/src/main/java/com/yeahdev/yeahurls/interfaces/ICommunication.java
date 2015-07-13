package com.yeahdev.yeahurls.interfaces;

import com.yeahdev.yeahurls.model.User;
import com.yeahdev.yeahurls.model.UserCreds;

public interface ICommunication {
    void passUserFromFirebase(UserCreds userCreds, User user);
}
