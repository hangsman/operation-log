package cn.hangsman.operationlog;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Created by 2022/1/11 14:09
 *
 * @author hangsman
 * @since 1.0
 */
@Data
@Builder
public class OperationLog {

    private Operator operator;
    private String content;
    private String fail;
    private String detail;
    private String category;
    private Date operationTime;
    private Map<String, Object> additional;

}
