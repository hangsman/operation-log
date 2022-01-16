package cn.hangsman.operationlog.spel;

/**
 * Created by 2022/1/12 11:17
 *
 * @author hangsman
 * @since 1.0
 */
public interface SpelFunction {
    Object apply(Object value);

    String functionName();
}
