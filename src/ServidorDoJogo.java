import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServidorDoJogo {
  private List<GerenciadorDeClientes> clientes; // Lista que armazena todos os manipuladores de clientes conectados
  private GerenciadorDeClientes jogadorAguardando; // Jogador que esta esperando por um oponente
  private int porta; // Numero da porta do servidor
  private List<String> historicoDeJogadores; // Lista que mantem o historico de jogadores conectados

  // Construtor para inicializar o servidor
  public ServidorDoJogo(int porta) {
    this.porta = porta;
    clientes = new ArrayList<>();
    jogadorAguardando = null;
    historicoDeJogadores = new ArrayList<>();
  }

  // Método para iniciar o servidor
  public void iniciarServidor() {
    try (ServerSocket serverSocket = new ServerSocket(porta)) {
      InetAddress ip = InetAddress.getLocalHost();
      System.out.println("Servidor iniciado em: " + ip.getHostAddress());

      // Loop principal para aceitar conexões de clientes
      while (true) {
        Socket socketCliente = serverSocket.accept(); // Aceitar a conexão do cliente
        GerenciadorDeClientes gerenciador = new GerenciadorDeClientes(socketCliente, this); // Cria um manipulador de
                                                                                            // cliente para lidar com o
                                                                                            // cliente
        clientes.add(gerenciador);// Adicionar o manipulador do cliente à lista de clientes
        new Thread(gerenciador).start();// Iniciar uma nova thread para lidar com o cliente
      }
    } catch (IOException e) {
      System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
    }
  }

  // Método para remover um cliente desconectado
  public synchronized void removerCliente(GerenciadorDeClientes gerenciador) {
    clientes.remove(gerenciador); // Remover o manipulador de cliente da lista de clientes
    if (jogadorAguardando == gerenciador) { // Se o cliente desconectado era o jogador que estava esperando
      jogadorAguardando = null; // Limpar o jogador que estava esperando
    }
  }

  // Método para adicionar um jogador que está esperando por um oponente
  public synchronized void adicionarJogadorAguardando(GerenciadorDeClientes gerenciador) {
    jogadorAguardando = gerenciador;
  }

  // Método para obter o jogador que está esperando por um oponente
  public synchronized GerenciadorDeClientes obterJogadorAguardando() {
    return jogadorAguardando;
  }

  // Método para adicionar um jogador ao histórico de jogadores conectados
  public synchronized void adicionarAoHistoricoDeJogadores(String nomeJogador) {
    historicoDeJogadores.add(nomeJogador); // Adicionar o nome do jogador ao histórico
    System.out.println("Novo jogador conectado: " + nomeJogador); // Imprimir o nome do jogador conectado
  }

  // Método para obter o histórico de jogadores conectados
  public synchronized List<String> obterHistoricoDeJogadores() {
    return historicoDeJogadores;
  }

  // Método para processar a jogada do cliente
  public synchronized void processarJogada(String jogada, GerenciadorDeClientes gerenciador, boolean contraMaquina) {
    // Verificar se o cliente escolheu sair
    if (jogada.equalsIgnoreCase("sair")) {
      // Enviar mensagem de desconexão para o cliente
      gerenciador.enviarMensagem("Você escolheu sair. Desconectando...");
      try {
        // Fechar os recursos do cliente
        gerenciador.in.close();
        gerenciador.out.close();
        gerenciador.socketCliente.close();
      } catch (IOException e) {
        System.err.println("Erro ao fechar recursos do cliente: " + e.getMessage());
      }
      removerCliente(gerenciador);// Remover o cliente desconectado
      return;
    }
    // Verificar se o cliente está jogando contra a máquina
    if (contraMaquina) {
      String jogadaMaquina = gerarJogadaMaquina(); // Gerar a jogada da máquina
      String resultado = determinarVencedor(jogada, jogadaMaquina); // Determinar o resultado do jogo

      // Enviar mensagens para o cliente sobre a jogada
      gerenciador.enviarMensagem("Você escolheu: " + jogada);
      gerenciador.enviarMensagem("A máquina escolheu: " + jogadaMaquina);

      // Atualizar as estatísticas do cliente com base no resultado
      if (resultado.equals("Você venceu!")) {
        gerenciador.incrementarVitorias();
      } else if (resultado.equals("A máquina venceu!")) {
        gerenciador.incrementarDerrotas();
      } else {
        gerenciador.incrementarEmpates();
      }
      // Enviar mensagem com o resultado para o cliente
      gerenciador.enviarMensagem(resultado);
      gerenciador.enviarMensagem(gerenciador.obterPlacar()); // Enviar as estatísticas do cliente
      gerenciador.enviarMensagem("Digite sua escolha (pedra, papel ou tesoura):"); // Solicitar uma nova jogada
    } else { // Se o cliente estiver jogando contra outro jogador
      gerenciador.setJogada(jogada); // Definir a jogada do cliente
      GerenciadorDeClientes oponente = gerenciador.obterOponente(); // Obter o oponente do cliente
      if (oponente != null && oponente.obterJogada() != null) { // Se o oponente já tiver feito sua jogada
        String resultado = determinarVencedor(gerenciador.obterJogada(), oponente.obterJogada()); // Determinar o
                                                                                                  // resultado do jogo
                                                                                                  // entre os dois
                                                                                                  // clientes
        // Atualizar as estatísticas de ambos os jogadores com base no resultado
        if (resultado.equals("Você venceu!")) {
          gerenciador.incrementarVitorias();
          oponente.incrementarDerrotas();
          resultado = gerenciador.obterNomeJogador() + " venceu!";
        } else if (resultado.equals("A máquina venceu!")) {
          gerenciador.incrementarDerrotas();
          oponente.incrementarVitorias();
          resultado = oponente.obterNomeJogador() + " venceu!";
        } else {
          gerenciador.incrementarEmpates();
          oponente.incrementarEmpates();
        }
        // Enviar mensagem com o resultado para ambos os clientes
        gerenciador.enviarMensagem(resultado);
        oponente.enviarMensagem(resultado);

        // Enviar as estatísticas atualizadas para ambos os clientes
        gerenciador.enviarMensagem(gerenciador.obterPlacar());
        oponente.enviarMensagem(oponente.obterPlacar());

        // Solicitar uma nova jogada para ambos os clientes
        gerenciador.enviarMensagem("Digite sua escolha (pedra, papel ou tesoura):");
        oponente.enviarMensagem("Digite sua escolha (pedra, papel ou tesoura):");

        // Redefinir as jogadas dos clientes para a próxima rodada
        gerenciador.resetarJogada();
        oponente.resetarJogada();
      }
    }
  }

  // Método para gerar a jogada da máquina
  private String gerarJogadaMaquina() {
    String[] jogadas = { "pedra", "papel", "tesoura" };
    return jogadas[(int) (Math.random() * jogadas.length)];
  }

  // Método para determinar o vencedor entre duas jogadas
  private String determinarVencedor(String jogada1, String jogada2) {
    if (jogada1.equals(jogada2)) {
      return "Empate! Ambos escolheram " + jogada1;
    } else if ((jogada1.equals("pedra") && jogada2.equals("tesoura")) ||
        (jogada1.equals("papel") && jogada2.equals("pedra")) ||
        (jogada1.equals("tesoura") && jogada2.equals("papel"))) {
      return "Você venceu!";
    } else {
      return "A máquina venceu!";
    }
  }

  // Método principal para executar o servidor
  public static void main(String[] args) {
    if (args.length != 0) {
      System.out.println("Este programa não aceita argumentos de linha de comando. Use-o sem argumentos.");
      System.exit(1);
    }

    int portaMinima = 1024; // Portas abaixo de 1024 são geralmente reservadas
    int portaMaxima = 65535; // O número máximo de porta permitido

    int portaAleatoria = gerarPortaAleatoria(portaMinima, portaMaxima); // Gerar uma porta aleatória
    System.out.println("Porta aleatória gerada: " + portaAleatoria);

    ServidorDoJogo servidor = new ServidorDoJogo(portaAleatoria); // Criar uma instância do servidor com a porta gerada
    servidor.iniciarServidor(); // Iniciar o servidor
  }

  // Método para gerar uma porta aleatória dentro do intervalo especificado
  private static int gerarPortaAleatoria(int portaMinima, int portaMaxima) {
    Random random = new Random();
    return random.nextInt(portaMaxima - portaMinima + 1) + portaMinima;
  }

  // Método para lidar com a desconexão de um cliente
  public synchronized void lidarComDesconexaoDeCliente(GerenciadorDeClientes gerenciador) {
    String nomeJogador = gerenciador.obterNomeJogador(); // Obter o nome do jogador desconectado
    System.out.println("Jogador desconectado: " + nomeJogador); // Imprimir mensagem de desconexão

    // Remover o cliente desconectado da lista de clientes
    removerCliente(gerenciador);

    // Verifique se o jogador desconectado estava esperando por um oponente
    if (gerenciador == jogadorAguardando) {
      jogadorAguardando = null; // Limpar o jogador que estava esperando
    } else if (gerenciador.obterOponente() != null) { // Se o jogador desconectado tinha um oponente
      GerenciadorDeClientes oponente = gerenciador.obterOponente(); // Obter o oponente do jogador desconectado
      oponente.enviarMensagem("Seu oponente se desconectou. Você vence automaticamente!"); // Informar ao oponente que
                                                                                           // ele venceu
      removerCliente(oponente); // Remover o oponente desconectado da lista de clientes
    }
  }
}

// Classe para lidar com a comunicação com um cliente
class GerenciadorDeClientes implements Runnable {
  Socket socketCliente; // Socket do cliente
  private ServidorDoJogo servidor; // Referencia ao servidor
  PrintWriter out; // Saida para o cliente
  BufferedReader in;// Entrada do cliente
  private String jogada;// Jogada do cliente
  private boolean contraMaquina; // Flag para indicar se o cliente está jogando contra a máquina
  private GerenciadorDeClientes oponente; // Referência ao oponente do cliente

  private int vitorias; // Número de vitórias do cliente
  private int derrotas; // Número de derrotas do cliente
  private int empates; // Número de empates do cliente
  private String nomeJogador; // Nome do jogador

  // Construtor para inicializar o manipulador do cliente
  public GerenciadorDeClientes(Socket socket, ServidorDoJogo servidor) {
    this.socketCliente = socket;
    this.servidor = servidor;
    this.jogada = null;
    this.oponente = null;
    this.vitorias = 0;
    this.derrotas = 0;
    this.empates = 0;
  }

  // Método para executar a thread do manipulador do cliente
  @Override
  public void run() {
    try {
      out = new PrintWriter(socketCliente.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

      enviarMensagem("Digite seu nome:");
      nomeJogador = in.readLine();
      enviarMensagem("Bem-vindo, " + nomeJogador + "!");

      servidor.adicionarAoHistoricoDeJogadores(nomeJogador); // Adicionar o jogador ao histórico de jogadores conectados

      enviarMensagem("Deseja jogar contra a máquina ou outro jogador? (Digite '1' para máquina ou '2' para jogador):");
      String escolha = in.readLine();
      contraMaquina = escolha.equals("1"); // Verificar se o cliente quer jogar contra a máquina

      // Iniciar o jogo com base na escolha do cliente
      if (contraMaquina) {
        enviarMensagem("Você escolheu jogar contra a máquina.");
        enviarMensagem("Digite sua escolha (pedra, papel ou tesoura):");
      } else {
        enviarMensagem("Você escolheu jogar contra outro jogador.");
        synchronized (servidor) {
          oponente = servidor.obterJogadorAguardando();
          if (oponente == null) {
            servidor.adicionarJogadorAguardando(this);
            enviarMensagem("Esperando outro jogador se conectar...");
          } else {
            oponente.setOponente(this);
            servidor.adicionarJogadorAguardando(null); // Limpar o jogador que estava esperando
            oponente.enviarMensagem("Outro jogador se conectou. Digite sua escolha (pedra, papel ou tesoura):");
            enviarMensagem("Outro jogador se conectou. Digite sua escolha (pedra, papel ou tesoura):");
          }
        }
      }

      String linhaEntrada;
      while ((linhaEntrada = in.readLine()) != null) {
        servidor.processarJogada(linhaEntrada, this, contraMaquina); // Processar a jogada do cliente
      }
    } catch (IOException e) {
      System.err.println("Erro na comunicação com o cliente: " + e.getMessage());
    } finally {
      try {
        in.close();
        out.close();
        socketCliente.close();
      } catch (IOException e) {
        System.err.println("Erro ao fechar recursos do cliente: " + e.getMessage());
      }
      servidor.removerCliente(this); // Remover o cliente desconectado do servidor
      notificarServidorSobreDesconexao(); // Notificar o servidor quando o cliente se desconectar
    }
  }

  // Método para enviar uma mensagem para o cliente
  public void enviarMensagem(String mensagem) {
    out.println(mensagem);
  }

  // Métodos para definir, obter e redefinir a jogada do cliente
  public void setJogada(String jogada) {
    this.jogada = jogada;
  }

  public String obterJogada() {
    return jogada;
  }

  public void resetarJogada() {
    this.jogada = null;
  }

  // Métodos para definir e obter o oponente do cliente
  public void setOponente(GerenciadorDeClientes oponente) {
    this.oponente = oponente;
  }

  public GerenciadorDeClientes obterOponente() {
    return oponente;
  }

  // Métodos para incrementar as estatísticas do cliente
  public void incrementarVitorias() {
    this.vitorias++;
  }

  public void incrementarDerrotas() {
    this.derrotas++;
  }

  public void incrementarEmpates() {
    this.empates++;
  }

  // Método para obter as estatísticas do cliente
  public String obterPlacar() {
    return "Jogador: " + nomeJogador + " | Vitórias: " + vitorias + ", Derrotas: " + derrotas + ", Empates: " + empates;
  }

  // Método para obter o nome do jogador
  public String obterNomeJogador() {
    return nomeJogador;
  }

  // Método para notificar o servidor quando o cliente se desconecta
  public void notificarServidorSobreDesconexao() {
    servidor.lidarComDesconexaoDeCliente(this);
  }
}
