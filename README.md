````markdown
# Como rodar o projeto na Máquina Virtual Ubuntu (C3 – MySQL + Migração para MongoDB)

Este guia descreve **todos os comandos necessários** para executar o projeto na máquina virtual Ubuntu, incluindo:

- atualização da máquina  
- instalação de Git, Java, MongoDB e MySQL  
- criação do banco da C2 no MySQL  
- migração dos dados da C2 → MongoDB  
- execução do sistema C3 utilizando MongoDB  

Repositório do projeto C3:  
`https://github.com/Kathelyn-Farias/GerenciamentoConcessionaria_C3_A2.git`

````
## 1. Atualizar a máquina virtual

```bash
sudo apt update
sudo apt upgrade -y
````

---

## 2. Instalar dependências

### 2.1. Git (para clonar o repositório)

```bash
sudo apt install -y git
```

### 2.2. Java (JDK 17 ou superior)

```bash
sudo apt install -y openjdk-17-jdk
```

Conferir:

```bash
java -version
javac -version
```

### 2.3. MongoDB

```bash
sudo apt install -y mongodb
```

> Em algumas distros o serviço se chama `mongod`, em outras `mongodb`.
> Nos comandos abaixo, usamos `||` para cobrir ambos os casos.

### 2.4. MySQL Server

```bash
sudo apt install -y mysql-server
```

---

## 3. Iniciar e habilitar os serviços (MongoDB e MySQL)

### 3.1. MongoDB

```bash
# iniciar o serviço
sudo systemctl start mongod || sudo systemctl start mongodb

# habilitar para iniciar automaticamente
sudo systemctl enable mongod || sudo systemctl enable mongodb

# (opcional) ver status
sudo systemctl status mongod || sudo systemctl status mongodb
```

### 3.2. MySQL

```bash
sudo systemctl start mysql
sudo systemctl enable mysql
sudo systemctl status mysql
```

---

## 4. Clonar o repositório do projeto C3

Escolha uma pasta (por exemplo `~/projetos`) e clone o repositório:

```bash
mkdir -p ~/projetos
cd ~/projetos

git clone https://github.com/Kathelyn-Farias/GerenciamentoConcessionaria_C3_A2.git

cd GerenciamentoConcessionaria_C3_A2
```

A partir de agora, todos os comandos são executados **dentro dessa pasta**.

---

## 5. Preparar o banco relacional (C2) no MySQL

> Esta etapa cria o banco da C2 no MySQL, que será utilizado como **fonte de dados** para migração para o MongoDB.

### 5.1. Entrar no MySQL

```bash
sudo mysql
```

No prompt do MySQL:

```sql
-- criar o banco de dados da C2 (nome "teste" neste exemplo)
CREATE DATABASE teste;
USE teste;

-- (opcional) criar usuário específico para a aplicação
CREATE USER 'app'@'localhost' IDENTIFIED BY 'app123';
GRANT ALL PRIVILEGES ON teste.* TO 'app'@'localhost';
FLUSH PRIVILEGES;

EXIT;
```

### 5.2. Criar tabelas e inserir dados da C2

De volta ao terminal normal, ainda na pasta do projeto:

```bash
cd ~/projetos/GerenciamentoConcessionaria_C3_A2
```

Entre de novo no MySQL, apontando para o banco `teste`:

```bash
sudo mysql teste
```

Dentro do MySQL, execute os scripts SQL da pasta `sql/`:

```sql
SOURCE sql/create_tables.sql;
SOURCE sql/insert_sample_records.sql;

EXIT;
```

> ⚠️ Se os nomes dos arquivos SQL forem diferentes no seu projeto, ajuste os comandos `SOURCE` para o nome correto.

Isso criará as tabelas `cliente`, `veiculo` e `venda` com registros de exemplo, igual à C2.

---

## 6. Configurar o acesso MySQL no projeto (db.properties)

O projeto utiliza um arquivo de propriedades para conectar ao MySQL:

Arquivo: `src/conexion/db.properties`

Editar:

```bash
nano src/conexion/db.properties
```

Conteúdo sugerido:

```properties
db.url=jdbc:mysql://localhost:3306/teste?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=app
db.password=app123
```

Salve e saia (`Ctrl+O`, `Enter`, `Ctrl+X` no nano).

---

## 7. Migrar dados da C2 (MySQL) para o MongoDB

> Agora usaremos o script `MigrationMySQLToMongo.java` para ler as tabelas da C2 (`cliente`, `veiculo`, `venda`) e criar/popular as coleções `cliente`, `veiculo`, `venda` no MongoDB (`concessionaria`).

### 7.1. Compilar o script de migração

Na raiz do projeto (onde está o arquivo `MigrationMySQLToMongo.java`):

```bash
cd ~/projetos/GerenciamentoConcessionaria_C3_A2

javac -cp ".:lib/*:src" MigrationMySQLToMongo.java
```

### 7.2. Executar o script de migração

```bash
java -cp ".:lib/*:src" MigrationMySQLToMongo
```

Saída esperada (exemplo):

```text
[1/3] Migrando tabela CLIENTE...
CLIENTE -> documentos inseridos: X
[2/3] Migrando tabela VEICULO...
VEICULO -> documentos inseridos: Y
[3/3] Migrando tabela VENDA...
VENDA -> documentos inseridos: Z
=== Migração concluída com sucesso! ===
```

---

## 8. Conferir dados no MongoDB

Opcionalmente, você pode verificar as coleções diretamente no Mongo:

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

Se aparecerem documentos nessas consultas, a migração foi feita corretamente.

---

## 9. Compilar o sistema principal (Main)

Agora vamos compilar a aplicação C3, que acessa diretamente o MongoDB.

Na raiz do projeto:

```bash
cd ~/projetos/GerenciamentoConcessionaria_C3_A2

javac -cp ".:lib/*:src" src/Main.java
```

> Esse comando compila o `Main.java` e, por consequência, todas as classes usadas (controllers Mongo, utils, relatórios, etc.).

---

## 10. Executar o sistema

Ainda na raiz do projeto:

```bash
java -cp ".:lib/*:src" Main
```

Funcionamento esperado:

* O sistema exibirá uma **SplashScreen** com:

  * nome do sistema,
  * disciplina, professor e grupo,
  * quantidade de documentos nas coleções `cliente`, `veiculo`, `venda`,
  * total de documentos no banco `concessionaria`.
* Em seguida, aparecerá o **menu principal** com as opções:

  * Relatórios
  * Inserir documentos
  * Remover documentos
  * Atualizar documentos
  * Listar documentos
  * Sair

Todo o CRUD e os relatórios passam a ser executados **diretamente no MongoDB**.

---

## 11. Resumo rápido dos principais comandos

```bash
# Atualizar sistema
sudo apt update
sudo apt upgrade -y

# Instalar dependências
sudo apt install -y git openjdk-17-jdk mongodb mysql-server

# Iniciar serviços
sudo systemctl start mongod || sudo systemctl start mongodb
sudo systemctl enable mongod || sudo systemctl enable mongodb
sudo systemctl start mysql
sudo systemctl enable mysql

# Clonar repositório
mkdir -p ~/projetos
cd ~/projetos
git clone https://github.com/Kathelyn-Farias/GerenciamentoConcessionaria_C3_A2.git
cd GerenciamentoConcessionaria_C3_A2

# (Dentro do MySQL) criar banco teste e carregar scripts da C2
# (ver seção 5 deste arquivo)

# Migrar MySQL → MongoDB
javac -cp ".:lib/*:src" MigrationMySQLToMongo.java
java  -cp ".:lib/*:src" MigrationMySQLToMongo

# Compilar e rodar o sistema C3
javac -cp ".:lib/*:src" src/Main.java
java  -cp ".:lib/*:src" Main
```
