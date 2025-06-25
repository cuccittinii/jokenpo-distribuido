import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class JogadorDoJogo {
  private String enderecoServidor;
  private int portaServidor;

  // Construtor para inicializar o endereço e porta do servidor
  public JogadorDoJogo(String endereco, int porta) {
    this.enderecoServidor = endereco;
    this.portaServidor = porta;
  }

  // Método para iniciar o cliente
  public void iniciar() {
    try (Socket socket = new Socket(enderecoServidor, portaServidor);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Scanner scanner = new Scanner(System.in)) {

      InetAddress ip = InetAddress.getLocalHost();
      System.out.println("Cliente conectado ao servidor: " + ip.getHostAddress() + ":" + portaServidor);

      String resposta;
      while ((resposta = in.readLine()) != null) {
        System.out.println(resposta); // Imprimir as mensagens recebidas do servidor

        // Verificar se o servidor está solicitando uma jogada do cliente
        if (resposta.equals("Digite sua escolha (pedra, papel ou tesoura):") ||
            resposta.startsWith("Outro jogador se conectou. Digite sua escolha")) {
          String jogada = scanner.nextLine(); // Ler a jogada do cliente
          out.println(jogada); // Enviar a jogada para o servidor
        } else if (resposta.equals("Digite seu nome:") ||
            resposta.equals(
                "Deseja jogar contra a máquina ou outro jogador? (Digite '1' para máquina ou '2' para jogador):")) {
          String entrada = scanner.nextLine(); // Ler a entrada do cliente
          out.println(entrada); // Enviar a entrada para o servidor
        }
      }
    } catch (IOException e) {
      System.err.println("Erro na comunicação com o servidor: " + e.getMessage());
    }
  }

  // Método principal para executar o cliente
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Uso: java JogadorDoJogo <endereço do servidor> <porta>");
      System.exit(1);
    }

    String enderecoServidor = args[0]; // Obter o endereço do servidor a partir dos argumentos de linha de comando
    int portaServidor = Integer.parseInt(args[1]); // Obter a porta do servidor a partir dos argumentos de linha de
                                                   // comando

    JogadorDoJogo cliente = new JogadorDoJogo(enderecoServidor, portaServidor); // Criar uma instância do cliente com o
                                                                                // endereço e porta do servidor
    cliente.iniciar(); // Iniciar o cliente
  }
}
