package demo.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(exclude = "taskList")
@NoArgsConstructor
@AllArgsConstructor
public class Step implements Serializable {

    // 步骤ID
    private String id;
    // 步骤编号
    private String code;
    // 步骤名称
    private String name;
    private LocalDate stepStartTime; // 整个工序开始时间
    private long executionDays; // 执行周期
    // 当前资源需求组合的资源需求列表，一个资源需求组合可以有多个资源需求组成。例如加工一个任务需要一位操作工 + 一台机器，
    // 则资源需求列表中有两个对象，分别是机台与操作工。
    private List<ResourceRequirement> resourceRequirementList;
    // 任务列表
    @JSONField(serialize = false)
    private List<Task> taskList;

    private List<AssignedTask> assignedTaskList = new ArrayList<>();

    private String productId;

    private List<String> holidayList;

    private Integer totalHours;

    private String isFinished = "1";

}
