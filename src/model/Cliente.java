package model;

public class Cliente {
    private int id_cliente;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;

    // CONSTRUTOR
    public Cliente(int id_cliente, String nome, String cpf, String telefone, String email) {
        this.id_cliente = id_cliente;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    // SETTERS
    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Metodo toString
    @Override
    public String toString() {
        return "ID: " + id_cliente + " | Nome: " + nome + " | CPF: " + cpf;
    }
}