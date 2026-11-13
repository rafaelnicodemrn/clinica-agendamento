package com.clinica.agendamento.config;

import com.clinica.agendamento.model.Profissional;
import com.clinica.agendamento.repository.ProfissionalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Quando o sistema sobe pela primeira vez (banco vazio) já cadastra uns profissionais,
// pra não precisar ficar cadastrando tudo na mão toda vez que for testar/apresentar
@Configuration
public class DadosIniciais {

    @Bean
    CommandLineRunner carregarProfissionais(ProfissionalRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return; // já tem dados, não faz nada
            }
            repository.save(novo("Dra. Ana Souza", "Clínica Geral", "CRM-SP 100001"));
            repository.save(novo("Dr. Carlos Lima", "Cardiologia", "CRM-SP 100002"));
            repository.save(novo("Dra. Marina Alves", "Dermatologia", "CRM-SP 100003"));
            repository.save(novo("Dr. Paulo Mendes", "Ortopedia", "CRM-SP 100004"));
            repository.save(novo("Dra. Julia Rocha", "Pediatria", "CRM-SP 100005"));
        };
    }

    private Profissional novo(String nome, String especialidade, String registro) {
        Profissional p = new Profissional();
        p.setNome(nome);
        p.setEspecialidade(especialidade);
        p.setRegistro(registro);
        // gera um email a partir do nome, ex: "Dr. Carlos Lima" -> dr.carlos.lima@clinica.com
        p.setEmail(nome.toLowerCase().replaceAll("[^a-z]", ".").replaceAll("\\.+", ".") + "@clinica.com");
        p.setAtivo(true);
        return p;
    }
}
