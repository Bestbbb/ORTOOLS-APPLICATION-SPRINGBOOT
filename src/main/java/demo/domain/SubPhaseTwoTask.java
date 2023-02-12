package demo.domain;

import lombok.Data;

@Data
public class SubPhaseTwoTask extends PhaseTwoAssignedTask{
    private Integer subStart;
    private Integer subEnd;
//    private Integer subQuantity;
}
