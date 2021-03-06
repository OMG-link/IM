package com.omg_link.im.core.user_manager;

import java.util.Collection;

abstract public class UserManager {
    abstract public Collection<User> getUserList();
    abstract public void onUsernameChanged(User user);
}
