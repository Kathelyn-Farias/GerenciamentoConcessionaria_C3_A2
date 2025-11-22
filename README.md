````markdown
# GerenciamentoConcessionaria_C3_A2  
## Execução na Máquina Virtual Ubuntu (MySQL + Migração para MongoDB)

Este guia mostra **todos os comandos necessários** para rodar o projeto na máquina virtual Ubuntu da disciplina, incluindo:

- atualização do sistema  
- instalação de Git, Java, MySQL e MongoDB  
- criação do banco relacional da C2 (`teste`)  
- migração MySQL → MongoDB  
- execução do sistema C3 em MongoDB  

Repositório do projeto C3:  
`https://github.com/Kathelyn-Farias/GerenciamentoConcessionaria_C3_A2.git`

````
## 1. Atualizar a máquina

```bash
sudo apt update
sudo apt upgrade -y
````

---

## 2. Instalar dependências

### 2.1. Git

```bash
sudo apt install -y git
```

### 2.2. Java (JDK 17)

```bash
sudo apt install -y openjdk-17-jdk
java -version
javac -version
```

### 2.3. Adicionar repositório oficial do MongoDB 8.0 (Ubuntu 24.04 – noble)

```bash
sudo apt install -y curl gnupg

# chave GPG do MongoDB 8.0
curl -fsSL https://www.mongodb.org/static/pgp/server-8.0.asc \
  | sudo gpg -o /usr/share/keyrings/mongodb-server-8.0.gpg --dearmor

# repositório MongoDB 8.0 para Ubuntu noble
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu noble/mongodb-org/8.0 multiverse" \
  | sudo tee /etc/apt/sources.list.d/mongodb-org-8.0.list

sudo apt update
sudo apt install -y mongodb-org
```

### 2.4. MySQL Server

```bash
sudo apt install -y mysql-server
```

---

## 3. Iniciar e habilitar serviços

### 3.1. MongoDB

```bash
sudo systemctl start mongod
sudo systemctl enable mongod
sudo systemctl status mongod
```

O status deve mostrar `active (running)`.

### 3.2. MySQL

```bash
sudo systemctl start mysql
sudo systemctl enable mysql
sudo systemctl status mysql
```

---

## 4. Clonar o repositório do projeto

No diretório HOME do usuário:

```bash
cd ~
git clone https://github.com/Kathelyn-Farias/GerenciamentoConcessionaria_C3_A2.git
cd GerenciamentoConcessionaria_C3_A2
```

A estrutura deve conter, entre outras, as pastas/arquivos:

* `src/`
* `sql/`
* `lib/` (com os JARs do MongoDB e MySQL)
* `MigrationMySQLToMongo.java`
* `Main.java`
* `README.md`

---

## 5. Criar banco relacional da C2 no MySQL

### 5.1. Criar banco e usuário

```bash
sudo mysql
```

No prompt `mysql>`:

```sql
CREATE DATABASE teste;
USE teste;

CREATE USER 'app'@'localhost' IDENTIFIED BY 'app123';
GRANT ALL PRIVILEGES ON teste.* TO 'app'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

### 5.2. Executar scripts SQL da pasta `sql/`

Na pasta do projeto:

```bash
cd ~/GerenciamentoConcessionaria_C3_A2
sudo mysql teste
```

No prompt `mysql>`:

```sql
SOURCE sql/create_tables.sql;
SOURCE sql/insert_samples_records.sql;
EXIT;
```

> Atenção ao nome do arquivo: **`insert_samples_records.sql`** (com **`samples`**).

Opcional: conferir dados:

```bash
sudo mysql teste
```

```sql
SELECT * FROM cliente;
SELECT * FROM veiculo;
SELECT * FROM venda;
EXIT;
```

---

## 6. Configurar acesso ao MySQL no projeto

Arquivo: `src/conexion/db.properties`

```bash
cd ~/GerenciamentoConcessionaria_C3_A2
nano src/conexion/db.properties
```

Conteúdo:

```properties
db.url=jdbc:mysql://localhost:3306/teste?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=app
db.password=app123
```

Salvar (`Ctrl+O`, `Enter`) e sair (`Ctrl+X`).

---

## 7. Migrar dados da C2 (MySQL) para MongoDB

O script `MigrationMySQLToMongo.java` conecta no MySQL, lê as tabelas `cliente`, `veiculo`, `venda` e insere documentos nas coleções correspondentes do MongoDB (`concessionaria`).

Na raiz do projeto:

```bash
cd ~/GerenciamentoConcessionaria_C3_A2

# compilar
javac -cp "lib/*:src:." MigrationMySQLToMongo.java

# executar
java -cp "lib/*:src:." MigrationMySQLToMongo
```

Saída esperada (exemplo):

```text
[1/3] Migrando tabela CLIENTE...
CLIENTE -> documentos inseridos: 2
[2/3] Migrando tabela VEICULO...
VEICULO -> documentos inseridos: 3
[3/3] Migrando tabela VENDA...
VENDA -> documentos inseridos: 1
=== Migração concluída com sucesso! ===
```

---

## 8. Conferir dados no MongoDB (opcional)

```bash
mongosh
```

Dentro do `mongosh`:

```javascript
use concessionaria
show collections
db.cliente.find()
db.veiculo.find()
db.venda.find()
```

Se retornarem documentos, a migração funcionou.

---

## 9. Compilar e executar o sistema principal (MongoDB)

### 9.1. Compilar `Main.java`

```bash
cd ~/GerenciamentoConcessionaria_C3_A2
javac -cp "lib/*:src:." src/Main.java
```

### 9.2. Executar o sistema

```bash
java -cp "lib/*:src:." Main
```

Funcionamento esperado:

1. Exibe **SplashScreen** com informações do sistema e contagem de documentos nas coleções.
2. Em seguida, apresenta o **menu principal** com opções para:

   * Relatórios
   * Inserir documentos
   * Remover documentos
   * Atualizar documentos
   * Listar documentos
   * Sair

Todo o CRUD e relatórios são feitos diretamente no **MongoDB**.

---

## 10. Resumo rápido dos comandos principais

```bash
# atualizar sistema
sudo apt update
sudo apt upgrade -y

# instalar dependências
sudo apt install -y git openjdk-17-jdk curl gnupg mysql-server

# configurar repositório oficial do MongoDB 8.0 (Ubuntu noble)
curl -fsSL https://www.mongodb.org/static/pgp/server-8.0.asc \
  | sudo gpg -o /usr/share/keyrings/mongodb-server-8.0.gpg --dearmor
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu noble/mongodb-org/8.0 multiverse" \
  | sudo tee /etc/apt/sources.list.d/mongodb-org-8.0.list
sudo apt update
sudo apt install -y mongodb-org

# iniciar serviços
sudo systemctl start mongod
sudo systemctl enable mongod
sudo systemctl start mysql
sudo systemctl enable mysql

# clonar projeto
cd ~
git clone https://github.com/Kathelyn-Farias/GerenciamentoConcessionaria_C3_A2.git
cd GerenciamentoConcessionaria_C3_A2

# criar banco teste + usuário app dentro do MySQL
# (ver seção 5)

# rodar scripts SQL da pasta sql/ dentro do MySQL
# (create_tables.sql e insert_samples_records.sql)

# migração MySQL -> Mongo
javac -cp "lib/*:src:." MigrationMySQLToMongo.java
java  -cp "lib/*:src:." MigrationMySQLToMongo

# compilar e rodar sistema
javac -cp "lib/*:src:." src/Main.java
java  -cp "lib/*:src:." Main
```
