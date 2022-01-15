package cn.hangsman.operationlog.test;

import cn.hangsman.operationlog.spring.boot.annotation.EnableOperationLog;
import cn.hangsman.operationlog.test.domain.User;
import cn.hangsman.operationlog.test.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by 2022/1/15 12:53
 *
 * @author hangsman
 * @since 1.0
 */
@SpringBootTest(classes = TestApplication.class)
@RunWith(SpringRunner.class)
public class OperationLogTest {

    @Autowired
    IUserService userService;

    @Test
    public void testCreateUser() {
        User user = User.builder().username("鲍勃").password("123123").build();
        userService.createUser(user);
    }

    @Test
    public void testUpdateUsername() {
        userService.updateUsername(10000, "鲍勃");
    }

}
