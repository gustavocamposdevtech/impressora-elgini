
import com.sun.jna.Library;
import com.sun.jna.Native;
import java.util.Scanner;
import java.nio.file.*;
import java.io.IOException;

public class Main {

    // Interface original do professor
    public interface ImpressoraDLL extends Library {
        ImpressoraDLL INSTANCE = (ImpressoraDLL) Native.load(
                Paths.get("E1_Impressora01.dll").toAbsolutePath().toString(),
                ImpressoraDLL.class);

        int AbreConexaoImpressora(int tipo, String modelo, String conexao, int param);
        int FechaConexaoImpressora();
        int ImpressaoTexto(String dados, int posicao, int estilo, int tamanho);
        int Corte(int avanco);
        int ImpressaoQRCode(String dados, int tamanho, int nivelCorrecao);
        int ImpressaoCodigoBarras(int tipo, String dados, int altura, int largura, int HRI);
        int AvancaPapel(int linhas);
        int StatusImpressora(int param);
        int AbreGavetaElgin(int pino, int ti, int tf);
        int AbreGaveta(int pino, int ti, int tf);
        int SinalSonoro(int qtd, int tempoInicio, int tempoFim);
        int ModoPagina();
        int LimpaBufferModoPagina();
        int ImprimeModoPagina();
        int ModoPadrao();
        int PosicaoImpressaoHorizontal(int posicao);
        int PosicaoImpressaoVertical(int posicao);
        int ImprimeXMLSAT(String dados, int param);
        int ImprimeXMLCancelamentoSAT(String dados, String assQRCode, int param);
    }

    // Variáveis do programa
    private static boolean conexaoAberta = false;
    private static boolean configuracaoFeita = false;

    private static int tipo;
    private static String modelo;
    private static String conexao;
    private static int parametro;

    private static final Scanner scanner = new Scanner(System.in);

    // Captura textos digitados pelo usuário
    private static String capturarEntrada(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine();
    }

    // Lê números inteiros e não aceita letras
    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine();

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite apenas números inteiros.");
            }
        }
    }

    // Lê o conteúdo de um arquivo
    private static String lerArquivo() {
        String caminho = capturarEntrada("Digite o caminho do arquivo XML: ");

        try {
            return Files.readString(Paths.get(caminho));
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            return null;
        }
    }

    // Realiza o login do usuário
    public static boolean realizarLogin() {
        int tentativas = 3;

        while (tentativas > 0) {
            String usuario = capturarEntrada("Usuário: ");
            String senha = capturarEntrada("Senha: ");

            if (usuario.equals("admin") && senha.equals("1234")) {
                System.out.println("Login realizado com sucesso!");
                return true;
            } else {
                tentativas--;
                System.out.println("Usuário ou senha incorretos.");

                if (tentativas > 0) {
                    System.out.println("Tentativas restantes: " + tentativas);
                }
            }
        }

        System.out.println("Número máximo de tentativas atingido.");
        return false;
    }

    // Configura os dados da conexão
    public static void configurarConexao() {
        if (conexaoAberta) {
            System.out.println("Feche a conexão antes de reconfigurar.");
            return;
        }

        System.out.println("\n--- CONFIGURAR CONEXÃO ---");

        tipo = lerInteiro("Tipo de conexão (1 para USB): ");
        modelo = capturarEntrada("Modelo da impressora (i8 ou i9): ");
        conexao = capturarEntrada("Conexão (USB, por exemplo): ");
        parametro = lerInteiro("Parâmetro da conexão: ");

        configuracaoFeita = true;

        System.out.println("Configuração realizada com sucesso!");
    }

    // Abre a conexão com a impressora
    public static void abrirConexao() {
        if (!configuracaoFeita) {
            System.out.println("Configure a conexão primeiro.");
            return;
        }

        if (!conexaoAberta) {
            try {
                int retorno = ImpressoraDLL.INSTANCE.AbreConexaoImpressora(
                        tipo, modelo, conexao, parametro);

                if (retorno == 0) {
                    conexaoAberta = true;
                    System.out.println("Conexão aberta com sucesso.");
                } else {
                    System.out.println("Erro ao abrir conexão. Código: " + retorno);
                }
            } catch (UnsatisfiedLinkError e) {
                System.out.println("Não foi possível carregar a DLL da impressora.");
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("A conexão já está aberta.");
        }
    }

    // Fecha a conexão com a impressora
    public static void fecharConexao() {
        if (conexaoAberta) {
            int retorno = ImpressoraDLL.INSTANCE.FechaConexaoImpressora();

            if (retorno == 0) {
                conexaoAberta = false;
                System.out.println("Conexão fechada com sucesso.");
            } else {
                System.out.println("Erro ao fechar conexão. Código: " + retorno);
            }
        } else {
            System.out.println("A conexão já está fechada.");
        }
    }

    // Imprime um texto
    public static void imprimirTexto() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        String dados = capturarEntrada("Digite o texto para imprimir: ");
        int posicao = lerInteiro("Posição: ");
        int estilo = lerInteiro("Estilo: ");
        int tamanho = lerInteiro("Tamanho: ");

        int retorno = ImpressoraDLL.INSTANCE.ImpressaoTexto(
                dados, posicao, estilo, tamanho);

        if (retorno >= 0) {
            System.out.println("Texto impresso com sucesso!");
        } else {
            System.out.println("Erro ao imprimir texto. Código: " + retorno);
        }
    }

    // Imprime um QR Code
    public static void imprimirQRCode() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        String dados = capturarEntrada("Digite os dados do QR Code: ");

        int retorno = ImpressoraDLL.INSTANCE.ImpressaoQRCode(dados, 6, 4);

        if (retorno == 0) {
            System.out.println("QR Code impresso com sucesso!");

            int corte = ImpressoraDLL.INSTANCE.Corte(5);

            if (corte == 0) {
                System.out.println("Papel cortado com sucesso!");
            } else {
                System.out.println("Erro ao cortar papel. Código: " + corte);
            }
        } else {
            System.out.println("Erro ao imprimir QR Code. Código: " + retorno);
        }
    }

    // Imprime código de barras
    public static void imprimirCodigoBarras() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        int retorno = ImpressoraDLL.INSTANCE.ImpressaoCodigoBarras(
                8, "{A012345678912", 100, 2, 3);

        if (retorno == 0) {
            System.out.println("Código de barras impresso com sucesso!");
        } else {
            System.out.println("Erro ao imprimir código de barras. Código: " + retorno);
        }
    }

    // Imprime XML SAT
    public static void imprimirXMLSAT() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        String xml = lerArquivo();

        if (xml == null) {
            return;
        }

        int retorno = ImpressoraDLL.INSTANCE.ImprimeXMLSAT(xml, 0);

        if (retorno == 0) {
            System.out.println("XML SAT impresso com sucesso!");
        } else {
            System.out.println("Erro ao imprimir XML SAT. Código: " + retorno);
        }
    }

    // Imprime XML de cancelamento SAT
    public static void imprimirXMLCancelamentoSAT() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        String xml = lerArquivo();

        if (xml == null) {
            return;
        }

        String assQRCode = capturarEntrada("Digite a assinatura do QR Code: ");

        int retorno = ImpressoraDLL.INSTANCE.ImprimeXMLCancelamentoSAT(
                xml, assQRCode, 0);

        if (retorno == 0) {
            System.out.println("XML de cancelamento impresso com sucesso!");
        } else {
            System.out.println("Erro ao imprimir cancelamento. Código: " + retorno);
        }
    }

    // Abre a gaveta
    public static void abrirGaveta() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        int retorno = ImpressoraDLL.INSTANCE.AbreGaveta(1, 5, 10);

        if (retorno == 0) {
            System.out.println("Gaveta aberta com sucesso!");
        } else {
            System.out.println("Erro ao abrir gaveta. Código: " + retorno);
        }
    }

    // Emite sinal sonoro
    public static void sinalSonoro() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        int qtd = lerInteiro("Quantidade de sinais sonoros: ");

        int retorno = ImpressoraDLL.INSTANCE.SinalSonoro(qtd, 50, 5);

        if (retorno == 0) {
            System.out.println("Sinal sonoro executado com sucesso!");
        } else {
            System.out.println("Erro no sinal sonoro. Código: " + retorno);
        }
    }

    // Avança o papel
    public static void avancarPapel() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        int linhas = lerInteiro("Quantidade de linhas para avançar: ");

        int retorno = ImpressoraDLL.INSTANCE.AvancaPapel(linhas);

        if (retorno == 0) {
            System.out.println("Papel avançado com sucesso!");
        } else {
            System.out.println("Erro ao avançar papel. Código: " + retorno);
        }
    }

    // Consulta o status da impressora
    public static void consultarStatus() {
        if (!conexaoAberta) {
            System.out.println("Abra a conexão com a impressora primeiro.");
            return;
        }

        int retorno = ImpressoraDLL.INSTANCE.StatusImpressora(0);

        System.out.println("Código retornado pelo status: " + retorno);
    }

    // Exibe as opções do programa
    public static void exibirMenu() {
        System.out.println("\n========================================");
        System.out.println("       MENU DA IMPRESSORA ELGIN");
        System.out.println("========================================");
        System.out.println("1  - Configurar conexão");
        System.out.println("2  - Abrir conexão");
        System.out.println("3  - Fechar conexão");
        System.out.println("4  - Imprimir texto");
        System.out.println("5  - Imprimir QR Code");
        System.out.println("6  - Imprimir código de barras");
        System.out.println("7  - Imprimir XML SAT");
        System.out.println("8  - Imprimir XML de cancelamento SAT");
        System.out.println("9  - Abrir gaveta");
        System.out.println("10 - Sinal sonoro");
        System.out.println("11 - Avançar papel");
        System.out.println("12 - Consultar status");
        System.out.println("0  - Sair");
        System.out.println("========================================");
    }

    // Método principal
    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("   SISTEMA DE IMPRESSORA ELGIN");
        System.out.println("================================");

        if (!realizarLogin()) {
            scanner.close();
            return;
        }

        String escolha;

        do {
            exibirMenu();
            escolha = capturarEntrada("Digite a opção desejada: ");

            if (escolha.equals("1")) {
                configurarConexao();

            } else if (escolha.equals("2")) {
                abrirConexao();

            } else if (escolha.equals("3")) {
                fecharConexao();

            } else if (escolha.equals("4")) {
                imprimirTexto();

            } else if (escolha.equals("5")) {
                imprimirQRCode();

            } else if (escolha.equals("6")) {
                imprimirCodigoBarras();

            } else if (escolha.equals("7")) {
                imprimirXMLSAT();

            } else if (escolha.equals("8")) {
                imprimirXMLCancelamentoSAT();

            } else if (escolha.equals("9")) {
                abrirGaveta();

            } else if (escolha.equals("10")) {
                sinalSonoro();

            } else if (escolha.equals("11")) {
                avancarPapel();

            } else if (escolha.equals("12")) {
                consultarStatus();

            } else if (escolha.equals("0")) {
                fecharConexao();
                System.out.println("Programa encerrado!");

            } else {
                System.out.println("Opção inválida. Tente novamente.");
            }

        } while (!escolha.equals("0"));

        scanner.close();
    }
}
Autor: Gustavo Silva Campos.
