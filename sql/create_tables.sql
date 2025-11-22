DROP VIEW IF EXISTS vw_vendas_detalhadas;
DROP VIEW IF EXISTS vw_vendas_por_marca_mes;
DROP TABLE IF EXISTS venda;
DROP TABLE IF EXISTS veiculo;
DROP TABLE IF EXISTS cliente;

-- ===== TABELAS =====
CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    cpf        CHAR(11)     NOT NULL UNIQUE,
    telefone   VARCHAR(20)  NOT NULL,
    email      VARCHAR(120) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE veiculo (
    id_veiculo INT AUTO_INCREMENT PRIMARY KEY,
    marca      VARCHAR(60)  NOT NULL,
    modelo     VARCHAR(80)  NOT NULL,
    cor        VARCHAR(30)  NOT NULL,
    ano        INT          NOT NULL,
    preco      FLOAT        NOT NULL,
    disponivel BOOLEAN      NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE venda (
    id_venda    INT AUTO_INCREMENT PRIMARY KEY,
    data_venda  DATE   NOT NULL,
    valor_final FLOAT  NOT NULL,
    id_cliente  INT    NOT NULL,
    id_veiculo  INT    NOT NULL,
    CONSTRAINT fk_venda_cliente
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_venda_veiculo
    FOREIGN KEY (id_veiculo) REFERENCES veiculo(id_veiculo)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_venda_veiculo UNIQUE (id_veiculo) -- 1 venda por veículo
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== TRIGGERS =====
DROP TRIGGER IF EXISTS trg_venda_after_insert;
DELIMITER //
CREATE TRIGGER trg_venda_after_insert
AFTER INSERT ON venda
FOR EACH ROW
BEGIN
    UPDATE veiculo SET disponivel = FALSE WHERE id_veiculo = NEW.id_veiculo;
END//
DELIMITER ;

DROP TRIGGER IF EXISTS trg_venda_after_delete;
DELIMITER //
CREATE TRIGGER trg_venda_after_delete
AFTER DELETE ON venda
FOR EACH ROW
BEGIN
    UPDATE veiculo SET disponivel = TRUE WHERE id_veiculo = OLD.id_veiculo;
END//
DELIMITER ;

-- ===== VIEWS (relatórios) =====
CREATE OR REPLACE VIEW vw_vendas_por_marca_mes AS
SELECT v.marca,
        DATE_FORMAT(ve.data_venda, '%Y-%m') AS ano_mes,
        COUNT(*)  AS qtd_vendas,
        SUM(ve.valor_final) AS total_vendido
FROM venda ve
JOIN veiculo v ON v.id_veiculo = ve.id_veiculo
GROUP BY v.marca, DATE_FORMAT(ve.data_venda, '%Y-%m');

CREATE OR REPLACE VIEW vw_vendas_detalhadas AS
SELECT ve.id_venda, ve.data_venda,
        c.nome AS cliente, v.marca, v.modelo, v.cor,
        ve.valor_final
FROM venda ve
JOIN cliente c ON c.id_cliente = ve.id_cliente
JOIN veiculo v ON v.id_veiculo = ve.id_veiculo
ORDER BY ve.data_venda DESC;
