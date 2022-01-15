package cn.hangsman.operationlog.test;

import cn.hangsman.operationlog.spring.boot.annotation.EnableOperationLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by 2022/1/15 16:31
 *
 * @author hangsman
 * @since 1.0
 */
@SpringBootApplication
@EnableOperationLog
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
