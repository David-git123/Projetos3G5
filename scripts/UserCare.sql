-- ============================================
-- CONFIGURAR USUÁRIO MYSQL PARA O PROJETO
-- ============================================
-- Execute este script como root no MySQL

-- Criar usuário (ajuste o nome e senha conforme necessário)
CREATE USER IF NOT EXISTS 'care_user'@'localhost' IDENTIFIED BY 'care_password';

-- Dar permissões no banco
GRANT ALL PRIVILEGES ON care_db.* TO 'care_user'@'localhost';

-- Aplicar mudanças
FLUSH PRIVILEGES;

-- Verificar
SELECT 'Usuário criado com sucesso!' AS status;
SHOW GRANTS FOR 'care_user'@'localhost';


