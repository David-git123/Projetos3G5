-- Dump convertido do H2 para MySQL 8+
-- Rode após criar o banco (por exemplo, `care_db`) e apontar o datasource para ele.
-- Comando sugerido: mysql -u <user> -p<senha> -D <database> < scripts/mysql-import.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

INSERT INTO pessoa (id, email, nome, senha, tipo_acesso) VALUES
  (1, 'admin@care.com', 'Super Admin', '$2a$10$zRkm02fLTwCDSFbKGFqYc.4xWxPmHwp.TvbYhQ.d6A2JEIuWGqdKq', 'ADMINISTRADOR'),
  (33, 'joao@gmail.com', 'joao', '$2a$10$VmJkt/0tm8UxYAVEgZHulOGVPF.u//bzXfQMnkz8RZe7vDeqPc466', 'ADMINISTRADOR'),
  (34, 'guilherme@gmail.com', 'Guilherme', '$2a$10$aqtrqaw0uaFwThT206dN9e12kA9Y23AnQpkQIvBXxhoIZIB9t6Hue', 'CLIENTE')
ON DUPLICATE KEY UPDATE
  email = VALUES(email),
  nome = VALUES(nome),
  senha = VALUES(senha),
  tipo_acesso = VALUES(tipo_acesso);

INSERT INTO formulario (id, data_atualizacao, data_criacao, descricao, status, titulo, criador_id) VALUES
  (353, '2025-12-03 12:00:45.666559', '2025-12-03 12:00:45.666559', 'qwe', 'RASCUNHO', 'qwe', 33)
ON DUPLICATE KEY UPDATE
  data_atualizacao = VALUES(data_atualizacao),
  data_criacao = VALUES(data_criacao),
  descricao = VALUES(descricao),
  status = VALUES(status),
  titulo = VALUES(titulo),
  criador_id = VALUES(criador_id);

ALTER TABLE pessoa AUTO_INCREMENT = 66;
ALTER TABLE formulario AUTO_INCREMENT = 386;

SET FOREIGN_KEY_CHECKS=1;
