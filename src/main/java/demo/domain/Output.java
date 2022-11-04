package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Output implements Serializable {

    private Integer code;
    private String message;
    private int status;
    private String requestId;
    private List<ManufacturerOrder> manufacturerOrderList;

}
