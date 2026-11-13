package com.clinica.agendamento.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// Entidade Profissional (etapa 2 do fluxo)
// A especialidade ficou como texto simples, a lista de especialidades da tela
// sai de um "select distinct" nessa coluna (ver ProfissionalRepository)
@Entity
@Table(name = "profissional")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "A especialidade é obrigatória")
    @Size(max = 80, message = "A especialidade deve ter no máximo 80 caracteres")
    @Column(nullable = false, length = 80)
    private String especialidade;

    // CRM, não pode repetir
    @NotBlank(message = "O registro profissional é obrigatório")
    @Size(max = 20, message = "O registro deve ter no máximo 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String registro;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(nullable = false, length = 120)
    private String email;

    // inativo = continua no historico mas não aparece pra marcar consulta.
    // é o que usamos no lugar de excluir quando ele já tem consultas
    @Column(nullable = false)
    private boolean ativo = true;

    // getters e setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public String getRegistro() { return registro; }
    public void setRegistro(String registro) { this.registro = registro; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
