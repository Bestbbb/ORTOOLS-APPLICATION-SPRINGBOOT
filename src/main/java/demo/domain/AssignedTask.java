package demo.domain;

//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AssignedTask implements Serializable{

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

    private Integer stepIndex;

    private Integer orderIndex;

    private Integer quantity;

    private String requiredResourceId;

    private String relatedTaskId;

    private Integer priority;

    @TableField(exist = false)
    private Integer subIndex;

    @TableField(exist = false)
    private Integer subQuantity;



    public AssignedTask(String originalId,Integer start,Integer hoursDuration){
        this.originalId =originalId;
        this.start=start;
        this.hoursDuration=hoursDuration;
    }

    public String printInfo() {
        return "日期:" + runTime + " 班次:" + schedule + " 工序组:" + stepId + " 生产总数:" + amount + " speed:" + speed;
    }

}
