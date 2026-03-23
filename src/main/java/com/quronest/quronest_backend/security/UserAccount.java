package com.quronest.quronest_backend.security;

import com.quronest.quronest_backend.model.table.User;

public interface UserAccount {
    boolean isEmailVerified();

    boolean isProfileComplete();

    User getUser();
}
