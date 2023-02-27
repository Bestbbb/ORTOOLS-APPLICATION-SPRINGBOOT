package demo.vo;

import lombok.Data;

import java.util.HashMap;

@Data
public class ResourceVo {
    private String id;

    private String resourceId;

    private String startTime;

    private long executionDays;

    private String endTime;


}
