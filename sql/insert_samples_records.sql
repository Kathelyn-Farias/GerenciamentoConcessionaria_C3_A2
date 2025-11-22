-- CLIENTES
INSERT INTO cliente (nome, cpf, telefone, email) VALUES
('Ana Souza','12345678901','11999990000','ana@ex.com'),
('Bruno Lima','23456789012','11988887777','bruno@ex.com');

-- VEÍCULOS
INSERT INTO veiculo (marca, modelo, cor, ano, preco, disponivel) VALUES
('Fiat','Argo','Prata',2022,65000,TRUE),
('VW','Gol','Branco',2019,42000,TRUE);

-- VENDA (disponível -> FALSE via trigger)
INSERT INTO venda (data_venda, valor_final, id_cliente, id_veiculo)
VALUES ('2025-10-10', 64000, 1, 1);
