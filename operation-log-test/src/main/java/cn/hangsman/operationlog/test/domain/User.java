package cn.hangsman.operationlog.test.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.annotation.Order;

/**
 * Created by 2022/1/14 17:23
 *
 * @author hangsman
 * @since 1.0
 */
@Data
@Builder
public class User {

    private Integer id;
    private String username;
    private String password;

}
