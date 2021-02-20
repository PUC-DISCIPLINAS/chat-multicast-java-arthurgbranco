# chat-multicast-java-arthurgbranco

Trabalho de Chat utilizando java e Multicast
Autor: Arthur Gramiscelli Branco

Como rodar:
1. Compile as classes
	* `javac ./src/app/Client.java`
	* `javac ./src/app/Server.java`
	* `javac ./src/object/User.java`
	* `javac ./src/object/Room.java`
	* `javac ./src/util/Converter.java`

2. Execute o servidor (tem de ser antes do cliente)
	* `cd ./src/`
	* `java app.Server`

3. Execute o cliente
	* `java app.Client`

### Comandos do cliente:

* `!join 1` Faz com que o usuario entre na sala 1 (Caso o usuario tente entrar em uma sala que nao existe, um excessao sera lancada)


* `!rooms` Lista as salas com usuarios conectados


* `!leave` Faz com que o usuario saia da sala atual e finalize o processo do cliente

### Comandos do servidor:

* `!create 1` Faz com que a sala 1 seja criada


* `!delete 1` Faz com que a sala 1 seja deletada


* `!exit` Finaliza o servidor

