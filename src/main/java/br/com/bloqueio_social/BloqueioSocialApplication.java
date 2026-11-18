
package br.com.bloqueio_social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BloqueioSocialApplication {

    public static void main(String[] args) {
		
	System.setProperty("java.awt.headless", "false");
	System.setProperty("spring.devtools.restart.enabled", "false");

	if (!ControleInstancia.adquirir()) {
		return;
	}

        SpringApplication.run(BloqueioSocialApplication.class, args);
    }

}
