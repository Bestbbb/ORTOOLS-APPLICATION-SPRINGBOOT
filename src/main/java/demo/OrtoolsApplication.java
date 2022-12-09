package demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
@SpringBootApplication
@MapperScan({"demo.mapper"})
public class OrtoolsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrtoolsApplication.class, args);
    }

}
