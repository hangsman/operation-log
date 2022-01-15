package cn.hangsman.operationlog.test.service;

import cn.hangsman.operationlog.annotation.OperationLog;
import cn.hangsman.operationlog.test.domain.User;

/**
 * Created by 2022/1/15 16:30
 *
 * @author hangsman
 * @since 1.0
 */
public interface IUserService {

    User createUser(User user);

    void updateUsername(Integer id, String newUsername);

    String getUsernameByID(Integer userID);
}
