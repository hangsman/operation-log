package cn.hangsman.operationlog.test.spel;

import cn.hangsman.operationlog.spel.SpelFunction;
import cn.hangsman.operationlog.test.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 2022/1/14 17:26
 *
 * @author hangsman
 * @since 1.0
 */
@Component
public class UserNameSpelFunction implements SpelFunction {

    @Autowired
    UserService userService;


    public Object apply(Object value) {
        Integer userID = (Integer) value;
        return userService.getUsernameByID(userID);
    }


    public String functionName() {
        return "getUsername";
    }
}
