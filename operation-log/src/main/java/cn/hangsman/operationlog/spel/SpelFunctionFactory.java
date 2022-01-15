package cn.hangsman.operationlog.spel;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 2022/1/12 11:16
 *
 * @author hangsman
 * @since 1.0
 */
public class SpelFunctionFactory {

    private final Map<String, SpelFunction> functionMap = new HashMap<>();

    public SpelFunctionFactory(List<SpelFunction> functions) {
        if (!CollectionUtils.isEmpty(functions)) {
            for (SpelFunction parseFunction : functions) {
                String functionName = parseFunction.functionName();
                Assert.hasLength(functionName, "functionName can not be empty！");
                functionMap.put(functionName, parseFunction);
            }
        }
    }

    public SpelFunction getFunction(String functionName) {
        return this.functionMap.get(functionName);
    }


}
