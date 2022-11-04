package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ResourceItem extends TaskOrResource{

    private String id;
    private String code;
    private Period period;
    private Integer capacity;
    private String resourcePoolId;

    @Override
    public Integer getEndTime(int quantity) {
        return 0;
    }
}
