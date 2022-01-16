package cn.hangsman.operationlog.annotation;

import java.lang.annotation.*;

/**
 * Created by 2022/1/11 11:39
 *
 * @author hangsman
 * @since 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OperationLog {

    /**
     * 日志正文
     * 支持 spel 表达式 自定义增强方法请使用 $json() 格式
     *
     * @return String
     */
    String content();

    /**
     * 失败正文
     * 格式同上
     *
     * @return String
     */
    String fail() default "";

    /**
     * 详细信息
     * 格式同上
     *
     * @return String
     */
    String detail() default "";

    /**
     * 是否开启记录
     * 格式同上 表达式最终返回应为布尔类型
     *
     * @return String
     */
    String condition() default "";

    /**
     * 类别
     *
     * @return String
     */
    String category() default "";

    /**
     * 前置处理 格式{"变量名={spel表达式}"}
     * 之后在其他模板中可以使用 #变量名 获取表达式返回值
     *
     * @return string[]
     */
    String[] before() default "";

    /**
     * 格式同 before
     * @return string[]
     */
    String[] additional() default "";
}
