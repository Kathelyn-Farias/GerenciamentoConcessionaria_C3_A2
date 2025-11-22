package model;

import java.util.Locale;

public class Veiculo {
    private int id_veiculo;
    private String marca;
    private String modelo;
    private String cor;
    private int ano;
    private float preco;
    private boolean disponivel;

    // CONSTRUTOR
    public Veiculo(int id_veiculo, String marca, String modelo, String cor, int ano, float preco, boolean disponivel) {
        this.id_veiculo = id_veiculo;
        this.marca = marca;
        this.modelo = modelo;
        this.cor = cor;
        this.ano = ano;
        this.preco = preco;
        this.disponivel = disponivel;
    }

    // GETTERS
    public int getId_veiculo() {
        return id_veiculo;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getCor() {
        return cor;
    }

    public int getAno() {
        return ano;
    }

    public float getPreco() {
        return preco;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    // SETTERS
    public void setId_veiculo(int id_veiculo) {
        this.id_veiculo = id_veiculo;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "ID Veículo: %d | Modelo: %s %s | Cor: %s | Ano: %d | Preço: R$ %.2f | Disponível: %s",
                id_veiculo, marca, modelo, cor, ano, preco, disponivel);
    }
}