package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.model.table.User;

public class AbstractUserAccount implements UserAccount {
    User user;

    @Override
    public boolean isEmailVerified() {
        return this.user.isEmailVerified();
    }

    @Override
    public boolean isProfileComplete() {
        return this.user.isProfileComplete();
    }

    @Override
    public User getUser() {
        return user;
    }
}
