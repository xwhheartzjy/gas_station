package org.codec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author cross
 *
 */
@SpringBootApplication
@MapperScan("org.codec.mapper")
public class GasStationApplication {
    public static void main(String[] args) {
        SpringApplication.run(GasStationApplication.class, args);
    }
}
