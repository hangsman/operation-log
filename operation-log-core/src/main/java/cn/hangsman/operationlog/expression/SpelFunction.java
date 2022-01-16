package cn.hangsman.operationlog.expression;

/**
 * Created by 2022/1/16 9:50
 *
 * @author hangsman
 * @since 1.0
 */
public interface SpelFunction {

    Object apply(Object value);

    String functionName();

}
