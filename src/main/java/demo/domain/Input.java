package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HashMap<LocalDate, Map<String,Long>> dateToResourceIdToHoursPerDay;


}
