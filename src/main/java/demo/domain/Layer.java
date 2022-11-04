package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Layer {
    private Integer layerId;

    private Integer layerNumber;

    private Product product;
}
