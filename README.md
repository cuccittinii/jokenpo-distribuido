# 🕹️ Jokenpô Distribuído em Java

Projeto desenvolvido na disciplina de **Sistemas Distribuídos** para implementar um jogo de **Pedra, Papel e Tesoura (Jokenpô)** no console, com suporte a:

- 👤 **Modo Local** (jogador vs. máquina)  
- 🧑‍🤝‍🧑 **Modo Multiplayer** (dois jogadores conectados via rede local)

---

## 🚀 Tecnologias e Conceitos

- **Java 17+**  
- **Sockets TCP/IP** (java.net.ServerSocket / java.net.Socket)  
- **Threads** (java.lang.Thread) para atendimento concorrente de clientes  
- **Execução em terminal/console**  

---

## 📁 Estrutura do Projeto
├── src/
│ ├── ServidorDoJogo.java
│ ├── GerenciadorDeClientes.java
│ └── JogadorDoJogo.java
├── .gitignore
└── README.md

- **ServidorDoJogo.java**  
  - Inicializa `ServerSocket` em porta padrão (e.g. 5000)  
  - Aguarda conexões, cria `GerenciadorDeClientes` para cada jogador  
  - Contém lógica de pontuação e de escolha automática da máquina  

- **GerenciadorDeClientes.java**  
  - Executado em **thread separada** para cada cliente  
  - Faz handshake (escolha de modo local vs. multiplayer)  
  - Encaminha jogadas entre jogadores ou contra a máquina  

- **JogadorDoJogo.java**  
  - Conecta ao servidor via `Socket`  
  - Lê a escolha do usuário no console (`Scanner`)  
  - Envia jogada e exibe resultado recebido  

---

---

## 💻 Como Executar

Para colocar o jogo em funcionamento, siga os passos abaixo:

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/cuccitinii/jokenpo-distribuido.git](https://github.com/cuccitinii/jokenpo-distribuido.git)
    cd jokenpo-distribuido/src
    ```

2.  **Compile o código:**
    ```bash
    javac *.java
    ```

3.  **Inicie o Servidor:**
    Abra um terminal e execute:
    ```bash
    java ServidorDoJogo
    ```
    O servidor exibirá mensagens informando que está aguardando conexões.

4.  **Inicie os Jogadores:**
    Em um ou mais terminais (pode ser na mesma máquina ou em máquinas distintas na mesma rede), execute:
    ```bash
    java JogadorDoJogo
    ```
    * **Modo Local:** Escolha `1` quando solicitado para jogar contra a máquina.
    * **Modo Multiplayer:** Escolha `2` para conectar dois jogadores e disputar.

---

## 🎮 Modos de Jogo

| Modo          | Escolha ao Iniciar | Comportamento                                     |
| :------------ | :----------------- | :------------------------------------------------ |
| **Local** | `1`                | Jogador vs. lógica de máquina no servidor         |
| **Multiplayer** | `2`                | Dois clientes trocam jogadas através do servidor |

---

## 🛠️ Personalização

* **Porta do servidor:** A porta do servidor é gerada automaticamente a cada execução (entre 1024 e 65535). O número da porta será exibido no console do servidor ao iniciar.
* **Timeout de resposta:** Ajuste as constantes de tempo de espera nas classes, se necessário, para configurar o tempo limite de resposta.

---

📝 Exemplo de Uso
Para ver o jogo em ação:

Abra três terminais.

No primeiro, execute o servidor (java ServidorDoJogo).

Nos dois terminais seguintes, execute java JogadorDoJogo e siga as instruções no console para disputar as rodadas.

Observe a pontuação e o vencedor de cada rodada sendo exibidos no console.

📌 Status
✅ Finalizado — Versão de console totalmente funcional.

✍️ Autor
Feito com ☕ e 💡 por Iago Pisa Bandeira

github.com/cuccittinii
