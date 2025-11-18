-- Criar banco de dados
CREATE DATABASE IF NOT EXISTS care_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar o banco
USE care_db;

-- Tabela de Pessoas (Usuários)
CREATE TABLE IF NOT EXISTS pessoa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    tipo_acesso VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Formulários (Pesquisas)
CREATE TABLE IF NOT EXISTS formulario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    status VARCHAR(50) DEFAULT 'RASCUNHO',
    criador_id BIGINT NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (criador_id) REFERENCES pessoa(id) ON DELETE CASCADE,
    INDEX idx_criador (criador_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela de Perguntas
CREATE TABLE IF NOT EXISTS pergunta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    formulario_id BIGINT NOT NULL,
    texto VARCHAR(500) NOT NULL,
    tipo_pergunta VARCHAR(50) NOT NULL,
    obrigatoria BOOLEAN DEFAULT FALSE,
    ordem INT DEFAULT 1,
    opcoes VARCHAR(1000),
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (formulario_id) REFERENCES formulario(id) ON DELETE CASCADE,
    INDEX idx_formulario (formulario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO pessoa (nome, email, senha, tipo_acesso) 
VALUES ('Administrador', 'admin@care.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMINISTRADOR')
ON DUPLICATE KEY UPDATE nome=nome;

INSERT INTO pessoa (nome, email, senha, tipo_acesso) 
VALUES ('Cliente Teste', 'cliente@care.com', '$2a$10$8K1p/a0dL3YqZ1Z2Z3Z4ZeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENTE')
ON DUPLICATE KEY UPDATE nome=nome;

SELECT 'Banco de dados criado com sucesso!' AS status;


