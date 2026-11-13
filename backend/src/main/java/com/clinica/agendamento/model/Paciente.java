package com.clinica.agendamento.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

// Entidade Paciente (etapa 1 do fluxo)
// O @Entity faz o Hibernate criar a tabela "paciente" sozinho no Postgres.
// As validações (@NotBlank, @Pattern...) rodam quando o controller usa @Valid,
// se algo estiver errado a API responde 400 com a mensagem de cada campo
@Entity
@Table(name = "paciente")
public class Paciente {

    // id gerado pelo banco (auto incremento)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
    @Column(nullable = false, length = 120)
    private String nome;

    // CPF só com numeros. O unique no banco é uma segurança a mais, o service ja confere antes
    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos numéricos")
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve estar no passado")
    @Column(nullable = false)
    private LocalDate dataNascimento;

    // DDD + numero: 10 digitos se for fixo, 11 se for celular
    @NotBlank(message = "O telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter 10 ou 11 dígitos numéricos")
    @Column(nullable = false, length = 11)
    private String telefone;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Column(nullable = false, length = 120)
    private String email;

    // getters e setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
