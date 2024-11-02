package jpabook.jangsu_jpashop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JangsuJpashopApplication {

    public static void main(String[] args) {
        SpringApplication.run(JangsuJpashopApplication.class, args);
    }

    // 초기화 된 프록시 객체만 노출하며, 그렇지 않은 것들은 안한다.
    @Bean
    Hibernate5JakartaModule hibernate5Module() {
        return new Hibernate5JakartaModule();
    }
}
