package demo.domain;

import lombok.Data;

@Data
public class SubPhaseOneTask extends PhaseOneAssignedTask{
    private Integer subIndex;
    private Integer subStart;
    private Integer subEnd;
}
