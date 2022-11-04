package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Input {

    private Integer id;
    private String relationID;
    private Period planningPeriod;
    private String inputSetting;
    private String outputSetting;
    private String dateTimeFormat;
    private String planningTimeUnit;
    private List<ResourcePool> resourcePool;
    private List<ManufacturerOrder> manufacturerOrderList;

}
