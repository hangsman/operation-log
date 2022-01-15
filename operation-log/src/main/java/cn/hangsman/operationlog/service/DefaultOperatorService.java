package cn.hangsman.operationlog.service;

import cn.hangsman.operationlog.Operator;

/**
 * Created by 2022/1/11 14:19
 *
 * @author hangsman
 * @since 1.0
 */
public class DefaultOperatorService implements OperatorService {

    private final DefaultOperator defaultOperator = new DefaultOperator();

    @Override
    public Operator getOperator() {
        return defaultOperator;
    }

    private static class DefaultOperator implements Operator {

    }
}
