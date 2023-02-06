package demo.domain;

import lombok.Data;

@Data
public class SubPhaseTwoTask extends PhaseTwoAssignedTask{
    private Integer subIndex;
    private Integer subStart;
    private Integer subEnd;
}
