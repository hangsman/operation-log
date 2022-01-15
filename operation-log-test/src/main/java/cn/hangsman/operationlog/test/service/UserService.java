package cn.hangsman.operationlog.test.service;


import cn.hangsman.operationlog.annotation.OperationLog;
import cn.hangsman.operationlog.test.domain.User;
import org.springframework.stereotype.Service;

/**
 * Created by 2022/1/15 11:49
 *
 * @author hangsman
 * @since 1.0
 */
@Service
public class UserService implements IUserService {

    @Override
    @OperationLog(category = "创建用户",
            content = "添加了一个用户名为 {#user.username} 的用户",
            fail = "用户添加失败：{#_errorMsg}",
            detail = "{#_ret != null ? $json(#_ret) : ''}")
    public User createUser(User user) {
        user.setId(10000);
        return user;
    }

    @Override
    @OperationLog(category = "修改用户名",
            before = {"oldName={$getUsername(#id)}"},
            content = "将用户名从 {#oldName} 修改为 {#newUsername}",
            fail = "用户名修改失败：{#_errorMsg}")
    public void updateUsername(Integer id, String newUsername) {

    }

    @Override
    public String getUsernameByID(Integer userID) {
        return "威廉";
    }
}
