package com.custmanage.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.custmanage.server.mapper")
public class CustManageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustManageServerApplication.class, args);
    }
}
