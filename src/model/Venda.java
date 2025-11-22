package model;

import java.util.Date;

public class Venda {
    private int id_venda;
    private Date data_venda;
    private float valor_final;
    private int id_cliente;
    private int id_veiculo;

    // CONSTRUTOR
    public Venda(int id_venda, Date data_venda, float valor_final, int id_cliente, int id_veiculo) {
        this.setId_venda(id_venda);
        this.setData_venda(data_venda);
        this.setValor_final(valor_final);
        this.setId_cliente(id_cliente);
        this.setId_veiculo(id_veiculo);
    }

    // GETTERS
    public int getId_venda() {
        return id_venda;
    }

    public Date getData_venda() {
        return data_venda;
    }

    public float getValor_final() {
        return valor_final;
    }

    public int getId_cliente() {
        return id_cliente;
    }

    public int getId_veiculo() {
        return id_veiculo;
    }

    // SETTERS
    public void setId_venda(int id_venda) {
        this.id_venda = id_venda;
    }

    public void setData_venda(Date data_venda) {
        this.data_venda = data_venda;
    }

    public void setValor_final(float valor_final) {
        this.valor_final = valor_final;
    }

    public void setId_cliente(int id_cliente) {
        this.id_cliente = id_cliente;
    }

    public void setId_veiculo(int id_veiculo) {
        this.id_veiculo = id_veiculo;
    }

    // Metodo toString
    @Override
    public String toString() {
        return "ID Venda: " + getId_venda() + " | Cliente: " + getId_cliente() + " | Veículo: " + getId_veiculo()
                + " | Valor: R$" + getValor_final() + " | Data: " + getData_venda();
    }
}