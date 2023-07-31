package demo.domain;

//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PhaseTwoAssignedTask extends AssignedTask implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String originalId;
    private Integer subId;
    private String code;
    private Integer speed;
    private Integer unit;
    private Integer layerNum;
    private Integer taskType;
    private String relatedLayer;
    private Integer amount;
    private LocalDate runTime;
    private Integer schedule;

    private String stepId;

    private Integer start;

    private Integer hoursDuration;

    private Integer end;

    private Integer stepIndex;

    private Integer orderIndex;

    private Integer quantity;

    private String relatedOrderId;//相关联的父订单id

    private String orderId;

    private String nextTaskId;

    private String requiredResourceId;

    private Integer minutesDuration;

    public PhaseTwoAssignedTask(String originalId, Integer start, Integer hoursDuration){
        this.originalId =originalId;
        this.start=start;
        this.hoursDuration=hoursDuration;
        this.minutesDuration = hoursDuration;
        this.end = start+hoursDuration;
    }

    public String printInfo() {
        return "日期:" + runTime + " 班次:" + schedule + " 工序组:" + stepId + " 生产总数:" + amount + " speed:" + speed;
    }

}
