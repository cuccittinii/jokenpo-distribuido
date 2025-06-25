// Source code is decompiled from a .class file using FernFlower decompiler.
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class GerenciadorDeClientes implements Runnable {
   Socket socketCliente;
   private ServidorDoJogo servidor;
   PrintWriter out;
   BufferedReader in;
   private String jogada;
   private boolean contraMaquina;
   private GerenciadorDeClientes oponente;
   private int vitorias;
   private int derrotas;
   private int empates;
   private String nomeJogador;

   public GerenciadorDeClientes(Socket var1, ServidorDoJogo var2) {
      this.socketCliente = var1;
      this.servidor = var2;
      this.jogada = null;
      this.oponente = null;
      this.vitorias = 0;
      this.derrotas = 0;
      this.empates = 0;
   }

   public void run() {
      try {
         this.out = new PrintWriter(this.socketCliente.getOutputStream(), true);
         this.in = new BufferedReader(new InputStreamReader(this.socketCliente.getInputStream()));
         this.enviarMensagem("Digite seu nome:");
         this.nomeJogador = this.in.readLine();
         this.enviarMensagem("Bem-vindo, " + this.nomeJogador + "!");
         this.servidor.adicionarAoHistoricoDeJogadores(this.nomeJogador);
         this.enviarMensagem("Deseja jogar contra a máquina ou outro jogador? (Digite '1' para máquina ou '2' para jogador):");
         String var1 = this.in.readLine();
         this.contraMaquina = var1.equals("1");
         if (this.contraMaquina) {
            this.enviarMensagem("Você escolheu jogar contra a máquina.");
            this.enviarMensagem("Digite sua escolha (pedra, papel ou tesoura):");
         } else {
            this.enviarMensagem("Você escolheu jogar contra outro jogador.");
            synchronized(this.servidor) {
               this.oponente = this.servidor.obterJogadorAguardando();
               if (this.oponente == null) {
                  this.servidor.adicionarJogadorAguardando(this);
                  this.enviarMensagem("Esperando outro jogador se conectar...");
               } else {
                  this.oponente.setOponente(this);
                  this.servidor.adicionarJogadorAguardando((GerenciadorDeClientes)null);
                  this.oponente.enviarMensagem("Outro jogador se conectou. Digite sua escolha (pedra, papel ou tesoura):");
                  this.enviarMensagem("Outro jogador se conectou. Digite sua escolha (pedra, papel ou tesoura):");
               }
            }
         }

         String var2;
         while((var2 = this.in.readLine()) != null) {
            this.servidor.processarJogada(var2, this, this.contraMaquina);
         }
      } catch (IOException var14) {
         System.err.println("Erro na comunicação com o cliente: " + var14.getMessage());
      } finally {
         try {
            this.in.close();
            this.out.close();
            this.socketCliente.close();
         } catch (IOException var12) {
            System.err.println("Erro ao fechar recursos do cliente: " + var12.getMessage());
         }

         this.servidor.removerCliente(this);
         this.notificarServidorSobreDesconexao();
      }

   }

   public void enviarMensagem(String var1) {
      this.out.println(var1);
   }

   public void setJogada(String var1) {
      this.jogada = var1;
   }

   public String obterJogada() {
      return this.jogada;
   }

   public void resetarJogada() {
      this.jogada = null;
   }

   public void setOponente(GerenciadorDeClientes var1) {
      this.oponente = var1;
   }

   public GerenciadorDeClientes obterOponente() {
      return this.oponente;
   }

   public void incrementarVitorias() {
      ++this.vitorias;
   }

   public void incrementarDerrotas() {
      ++this.derrotas;
   }

   public void incrementarEmpates() {
      ++this.empates;
   }

   public String obterPlacar() {
      return "Jogador: " + this.nomeJogador + " | Vitórias: " + this.vitorias + ", Derrotas: " + this.derrotas + ", Empates: " + this.empates;
   }

   public String obterNomeJogador() {
      return this.nomeJogador;
   }

   public void notificarServidorSobreDesconexao() {
      this.servidor.lidarComDesconexaoDeCliente(this);
   }
}
