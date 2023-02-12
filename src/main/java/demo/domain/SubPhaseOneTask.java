package demo.domain;

import lombok.Data;

@Data
public class SubPhaseOneTask extends PhaseOneAssignedTask{
    private Integer subStart;
    private Integer subEnd;
//    private Integer subQuantity;
}
