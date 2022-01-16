package cn.hangsman.operationlog.test.domain;

import lombok.Builder;
import lombok.Data;

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
