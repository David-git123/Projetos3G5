#############################################
# Estágio 1 — Build do Backend (Spring Boot)
#############################################
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia apenas arquivos de configuração para cache das dependências
COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .

# Baixa dependências antes do código (melhor cache)
RUN ./mvnw dependency:go-offline

# Agora copia o código-fonte inteiro
COPY src ./src

# Compila o jar (inclui o frontend em /static automaticamente)
RUN ./mvnw clean package -DskipTests


#############################################
# Estágio 2 — Imagem final de produção
#############################################
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# expõe a porta da aplicação spring
EXPOSE 8080

# Copia o jar do estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# comando de início da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]