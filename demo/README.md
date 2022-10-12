# Guião de Demonstração

## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados 
para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Lançar o *ZooKeeper* - na diretoria 'zookeeper/bin', correr o comando './zkServer.sh start' (Linux/Mac) 
ou 'zkServer.cmd' (Windows).

Lançar consola de interação com o *ZooKeeper* - na diretoria 'zookeeper/bin', correr o comando 
'./zkCli.sh' (Linux/Mac) ou 'zkCli.cmd' (Windows).

Terminar o *Zookeeper* - na diretoria 'zookeeper/bin', correr o comando './zkServer.sh stop' (Linux/Mac).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar o *rec*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java -Dexec.args="localhost 2181 localhost 8091 1"
```
ou só:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *rec* no endereço *localhost*, na porta *8091* e com id 1.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *rec* no endereço *localhost* e na porta *8091*.

De seguida, é preciso lançar o servidor *hub* .
Para isso basta ir à pasta *hub* e executar:

```sh
$ mvn compile exec:java
```

Este comando vai colocar o *hub* no endereço *localhost* e na porta *8081*, e registar os users e stations de acordo com os ficheiros users.csv e stations.csv.

Para confirmar o funcionamento do servidores *hub* e *rec* com um *ping* e *sys_status* , fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.

### 1.5. *App*

Iniciar a aplicação com a utilizadora alice:

```sh
$ cd app
$ mvn compile exec:java
```

ou

```sh
$ app localhost 8081 alice +35191102030 38.7380 -9.3000
```

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.

### 2.1. *balance*

Consultar o saldo de *bicloins*:

    > balance
    alice 0 BIC


### 2.2 *top-up*

Carrgar um valor válido em EUR:

    > top-up 15
    alice 150 BIC

Carregar um valor inválido em EUR:

    > top-up 25
    ERROR Invalid amount, must be between 1 and 20, inclusive!

### 2.3 *bike-up*

Levantar bicicleta numa estação a mais de 200 metros:

    > bike-up ocea
    ERROR This station is unreachable (more than 200 meters)!

Levantar bicicleta numa estação que não existe:

	> bike-up xpto
    ERROR Unknown Station Id!

Levantar bicicleta corretamente:

    > tag 38.7376 -9.3031 loc1
    OK
    > move loc1
    alice em https://www.google.com/maps/place/38.7376,-9.3031
    > bike-up istt
    OK
    
    
### 2.4 *bike-down*

Devolver bicicleta numa estação a mais de 200 metros:

    > bike-down ocea
    ERROR This station is unreachable (more than 200 meters)!

Devolver bicicleta corretamente:

    > bike-down istt
    OK

Devolver bicicleta sem o utilizador ter uma bicicleta:

    > bike-down istt
    ERROR User doesn't have a bike to bike down!

Devolver uma bicicleta numa estação que não existe:

    > bike-down xpto
    ERROR Unknown Station Id!

### 2.5 *scan*

Listar as 3 estações mais próximas:

    > scan 3
    istt, lat 38.7372, long -9.3023, 20 docas, 4 BIC prémio, 12 bicicletas, a 82 metros
    stao, lat 38.6867, long -9.3124, 30 docas, 3 BIC prémio, 20 bicicletas, a 5717 metros
    jero, lat 38.6972, long -9.2064, 30 docas, 3 BIC prémio, 20 bicicletas, a 9516 metros

Listar um número inválido estações mais próximas:

    > scan 0
    ERROR Invalid number of near stations!

### 2.6 *info*

Pedir informação sobre a estação ocea:

    > info ocea
    Oceanário, lat 38.7633, long -9.095, 20 docas, 2 BIC prémio, 15 bicicletas, 0 levantamentos, 0 devoluções, https://www.google.com/maps/place/38.7633,-9.095

Pedir informação sobre uma estação que não existe:

    > info xpto
    ERROR Unknown Station Id!

### 2.7 *ping*

Fazer um ping:

    > ping
    UP

Fazer um ping com o server em baixo:

    > ping
    ERROR Failed communication with server!

### 2.8 *sys_status*

Fazer sys_status:

    > sys_status
    path:/grpc/bicloin/rec/1, up:true
    path:/grpc/bicloin/hub/1, up:true

Fazer um ping com o server em baixo:

    > sys_status
    ERROR Failed communication with server!

## 3. Replicação e tolerância a faltas

Irão ser agora apresentados vários casos de situações de funcionamento normal do programa com 
vários Records e situações de tolerância a faltas.
Para testar todas operações de forma a apresentar o output demonstrado, os seguintes casos devem 
ser executados pela ordem correta.

### Setup

1 - Lançar o *registry* -> Secção 1.1;
2 - Compilar o projeto -> Secção 1.2;
3 - Lançar a primeira réplica Record -> Na diretoria /A66-Bicloin/rec, executar o comando "mvn clean compile exec:java -Dexec.args="localhost 2181 localhost 8091 1""
4 - Lançar a segunda réplica Record -> Na diretoria /A66-Bicloin/rec, executar o comando "mvn clean compile exec:java -Dexec.args="localhost 2181 localhost 8092 2""
5 - Lançar a terceira réplica Record -> Na diretoria /A66-Bicloin/rec, executar o comando "mvn clean compile exec:java -Dexec.args="localhost 2181 localhost 8093 3""
6 - Lançar a quarta réplica Record -> Na diretoria /A66-Bicloin/rec, executar o comando "mvn clean compile exec:java -Dexec.args="localhost 2181 localhost 8094 4""
7 - Lançar a quinta réplica Record -> Na diretoria /A66-Bicloin/rec, executar o comando "mvn clean compile exec:java -Dexec.args="localhost 2181 localhost 8095 5""
8 - Lançar o Hub -> Na diretoria /A66-Bicloin/hub, executar o comando "mvn compile exec:java"

###### Caso 1

Caso normal - executar a App com cinco Records em execução:

1 - Na diretoria /A66-Bicloin/app, executar o comando "mvn clean compile exec:java"
2 - Realizar o comando "top-up 10" no terminal de execução da App, de modo ao programa realizar uma escrita

    > top-up 10
    alice 100 BIC

3 - Realizar o comando "balance" no terminal de execução da App, de modo ao programa realizar uma leitura

    > balance
    alice 100 BIC

4 - Verificar os resultados no Hub e Records

###### Caso 2

Durante a execução - colocar um processo Record em pausa

1 - No terminal de um dos recs, pressionar as teclas CTRL + Z (SIGTSTP)
2 - Realizar o comando "sys_status" no terminal de execução da App, de modo ao programa realizar um ping a todos os Records e verificar se o Record escolhido está down.

    > sys_status
    path:/grpc/bicloin/rec/1, up:false
    path:/grpc/bicloin/rec/2, up:true
    path:/grpc/bicloin/rec/3, up:true
    path:/grpc/bicloin/rec/4, up:true
    path:/grpc/bicloin/rec/5, up:true
    path:/grpc/bicloin/hub/1, up:true

3 - Executar os pontos 2, 3 e 4 do caso 1 outra vez, sendo que o resultado dos comandos neste caso irá ser `alice 200 BIC` em ambos

###### Caso 3

Durante a execução - colocar o processo Record que estava em pausa a executar

1 - Escrever `fg` no terminal, resumindo o processo do rec que estava em pausa
2 - Executar os pontos 2, 3 e 4 do caso 1 outra vez, sendo que o resultado dos comandos neste caso irá ser `alice 300 BIC` em ambos

###### Caso 4

Durante a execução - terminar um processo Record

1 - No terminal de um dos recs, pressionar as teclas CTRL + C (SIGINT), interrompendo o processo
2 - Realizar o comando "sys_status" no terminal de execução da App, de modo ao programa realizar um ping a todos os Records e verificar se o Record escolhido está down.

    > sys_status
    path:/grpc/bicloin/rec/1, up:false
    path:/grpc/bicloin/rec/2, up:true
    path:/grpc/bicloin/rec/3, up:true
    path:/grpc/bicloin/rec/4, up:true
    path:/grpc/bicloin/rec/5, up:true
    path:/grpc/bicloin/hub/1, up:true

3 - Executar os pontos 2, 3 e 4 do caso 1 outra vez, sendo que o resultado dos comandos neste caso irá ser `alice 400 BIC` em ambos

###### Caso 5

Durante a execução - lançar um novo processo Record no lugar do que foi terminado:

1 - Lançar uma outra réplica Record -> Na diretoria /A66-Bicloin/rec, executar o comando "mvn clean compile exec:java"
2 - Realizar o comando "move 38.7061 -9.1440" no terminal de execução da App

    > move 38.7061 -9.1440
    alice em https://www.google.com/maps/place/38.7061,-9.1440

3 - Realizar o comando "bike-up cais" no terminal de execução da App

    > bike-up cais
    OK

4 - Verificar os resultados no Hub e Records

## 4. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.
