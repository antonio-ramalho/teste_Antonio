# Query da criação das tabelas

CREATE TABLE operadoras (
    registro_ans VARCHAR(20) PRIMARY KEY,
    cnpj VARCHAR(20),
    razao_social VARCHAR(255),
    nome_fantasia VARCHAR(255),
    modalidade VARCHAR(100),
    uf CHAR(2),
    cidade VARCHAR(100),
    data_registro DATE,
    INDEX idx_uf (uf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE despesas_consolidadas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cnpj VARCHAR(20),
    trimestre VARCHAR(50),
    ano INT,
    valor_despesa DECIMAL(18,2),
    -- Relacionamento lógico com a operadora
    INDEX idx_cnpj (cnpj)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE despesas_agregadas (
    registro_ans VARCHAR(20) PRIMARY KEY,
    cnpj VARCHAR(20),
    razao_social VARCHAR(255),
    modalidade VARCHAR(100),
    uf CHAR(2),
    total_despesas DECIMAL(18,2),
    media_despesas DECIMAL(18,2),
    desvio_padrao DOUBLE,
    FOREIGN KEY (registro_ans) REFERENCES operadoras(registro_ans)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# Query da importação dos arquivos

LOAD DATA LOCAL INFILE 'Relatorio_cadop.csv' 
INTO TABLE operadoras 
CHARACTER SET utf8mb4 
FIELDS TERMINATED BY ';' 
IGNORE 1 ROWS 
(registro_ans, cnpj, razao_social, nome_fantasia, modalidade, @dummy, @dummy, @dummy, @dummy, cidade, uf, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, @dummy, data_registro);

LOAD DATA LOCAL INFILE 'consolidado_despesas.csv' 
INTO TABLE despesas_consolidadas 
FIELDS TERMINATED BY ',' 
IGNORE 1 ROWS 
(cnpj, @dummy, trimestre, ano, valor_despesa);

LOAD DATA LOCAL INFILE 'despesas_agregadas.csv' 
INTO TABLE despesas_agregadas 
FIELDS TERMINATED BY ',' 
IGNORE 1 ROWS 
(cnpj, registro_ans, uf, razao_social, modalidade, total_despesas, media_despesas, desvio_padrao);

# Query análiticas

SELECT 
    o.razao_social,
    ((v3.valor - v1.valor) / v1.valor) * 100 AS crescimento_percentual
FROM (SELECT cnpj, valor_despesa AS valor FROM despesas_consolidadas WHERE trimestre LIKE '1%') v1
JOIN (SELECT cnpj, valor_despesa AS valor FROM despesas_consolidadas WHERE trimestre LIKE '3%') v3 ON v1.cnpj = v3.cnpj
JOIN operadoras o ON o.cnpj = v1.cnpj
WHERE v1.valor > 0
ORDER BY crescimento_percentual DESC
LIMIT 5;

SELECT 
    o.uf, 
    SUM(d.valor_despesa) AS total_uf,
    AVG(d.valor_despesa) AS media_por_operadora
FROM despesas_consolidadas d
JOIN operadoras o ON d.cnpj = o.cnpj
GROUP BY o.uf
ORDER BY total_uf DESC
LIMIT 5;

SELECT COUNT(*) AS total_operadoras_acima_media
FROM (
    SELECT cnpj
    FROM despesas_consolidadas
    WHERE valor_despesa > (SELECT AVG(valor_despesa) FROM despesas_consolidadas)
    GROUP BY cnpj
    HAVING COUNT(DISTINCT trimestre) >= 2
) AS subquery;
