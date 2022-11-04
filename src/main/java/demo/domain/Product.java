package demo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    // 产品唯一ID
    private String id;
    // 产品编号
    private String code;
    // 产品名称
    private String name;
    // 当前产品的生产步骤列表
    private List<Step> stepList;

}
