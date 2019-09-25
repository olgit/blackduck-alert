package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.UserModel;

public interface UserAccessor {

    List<UserModel> getUsers();

    Optional<UserModel> getUser(String username);

    UserModel addOrUpdateUser(final UserModel user);

    UserModel addOrUpdateUser(UserModel user, boolean passwordEncoded);
    
    UserModel addUser(String userName, String password, String emailAddress);

    boolean assignRoles(String username, Set<Long> roleIds);

    boolean changeUserPassword(String username, String newPassword);

    boolean changeUserEmailAddress(String username, String emailAddress);

    void deleteUser(String userName);
}
