package com.clinica.agendamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Classe principal, o main sobe o Spring e o Tomcat embutido na porta 8080.
// Ela fica no pacote de cima (com.clinica.agendamento) pra o @SpringBootApplication
// achar os controllers, services e repositories que estao nos subpacotes
@SpringBootApplication
public class AgendamentoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendamentoApplication.class, args);
    }
}
