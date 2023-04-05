package demo.domain;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerOrder {

    // 生产计划ID
    private String id;
    // 生产计划编号
    private String code;
    // 生产计划名称
    private String name;
    // 生产数量
    private Integer quantity;
    // 优先级
    private Integer priority;
    // 类型，生产计划类型：0代表正常生产计划，1代表样品生产计划
    private Integer type;
    // type为1时传入，是为关联的正常生产计划ID
    private String relatedManufactureOrderId;
    // type为0时传入，是为因打样导致的延滞天数
    private Integer delayDays;
    // 生产计划的就绪时间与要求完成日期
    @JSONField(serialzeFeatures = SerializerFeature.WriteMapNullValue)
    private Period period;
    // 当前生产计划对应的产品，目前的结构设计中，一个生产计划只对应一个产品。未来的版本将会视需求情况，可支持多个产品。
    // 因为产品与工艺信息相关，因此，即命名一个生产计划可对应多个产品，这些产品的工艺路线与资源需求，
    // 也需要一定的限制（工艺路线需要相同才有可能放在一个生产计划）。
    private Product product;
    //最晚结束时间
    private LocalDateTime endDate;
    //排程天数
    private Integer duration;

    private Integer releaseDate = 0;
    //合并小样单和正常后的数量
    @JSONField(serialize = false)
    private Integer joinQuantity;

    private Integer index;

    private Integer isComplete;



}
