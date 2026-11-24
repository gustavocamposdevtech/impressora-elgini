import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    // A interface abaixo faz a ponte entre Java e a DLL da impressora. Não altere.
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

private static final String USUARIO = "admin";
    private static final String SENHA = "1234";
    // Chave de teste fornecida junto com os arquivos da atividade.
    private static final String ASS_QRCODE = "Q5DLkpdRijIRGY6YSSNsTWK1TztHL1vD0V1Jc4spo/CEUqICEb9SFy82ym8EhBRZjbh3btsZhF+sjHqEMR159i4agru9x6KsepK/q0E2e5xlU5cv3m1woYfgHyOkWDNcSdMsS6bBh2Bpq6s89yJ9Q6qh/J8YHi306ce9Tqb/drKvN2XdE5noRSS32TAWuaQEVd7u+TrvXlOQsE3fHR1D5f1saUwQLPSdIv01NF6Ny7jZwjCwv1uNDgGZONJdlTJ6p0ccqnZvuE70aHOI09elpjEO6Cd+orI7XHHrFCwhFhAcbalc+ZfO5b/+vkyAHS6CYVFCDtYR9Hi5qgdk31v23w==";

    private static boolean conexaoAberta = false;
    private static int tipo = 1;
    private static String modelo = "i9";
    private static String conexao = "USB";
    private static int parametro = 0;
    private static final Scanner scanner = new Scanner(System.in);

    private static String capturarEntrada(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    // Repete a pergunta até receber um número inteiro.
    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static boolean exigirConexao() {
        if (!conexaoAberta) {
            System.out.println("Conexão fechada. Abra a conexão antes (opção 2).");
            return false;
        }
        return true;
    }

    private static void mostrarResultado(String acao, int retorno) {
        if (retorno == 0) {
            System.out.println(acao + " concluído com sucesso.");
        } else {
            System.out.println("Erro ao " + acao.toLowerCase() + ". Código: " + retorno);
        }
    }

    private static void cortarPapel() {
        int retorno = ImpressoraDLL.INSTANCE.Corte(5);
        mostrarResultado("Corte do papel", retorno);
    }

    private static String lerArquivo(String nome) {
        try {
            Path caminho = Paths.get(nome);
            if (!Files.isRegularFile(caminho)) {
                System.out.println("Arquivo não encontrado: " + nome);
                return null;
            }
            return Files.readString(caminho);
        } catch (IOException e) {
            System.out.println("Não foi possível ler o arquivo: " + e.getMessage());
            return null;
        }
    }

 public static boolean fazerLogin() {
        for (int tentativa = 1; tentativa <= 3; tentativa++) {
            String usuario = capturarEntrada("Usuário: ");
            String senha = capturarEntrada("Senha: ");
            if (usuario.equals(USUARIO) && senha.equals(SENHA)) {
                System.out.println("Login realizado com sucesso.");
                return true;
            }
            int restantes = 3 - tentativa;
            if (restantes > 0) {
                System.out.println("Usuário ou senha incorretos. Tentativas restantes: " + restantes);
            }
        }
        System.out.println("Número máximo de tentativas atingido. Programa encerrado.");
        return false;
    }

    public static void configurarConexao() {
        if (conexaoAberta) {
            System.out.println("Feche a conexão antes de alterar as configurações.");
            return;
        }

        System.out.println("Tipo de conexão: 1-USB, 2-Serial, 3-Rede (IP), 4-Bluetooth");
        int novoTipo;
        do {
            novoTipo = lerInteiro("Tipo: ");
            if (novoTipo < 1 || novoTipo > 4) {
                System.out.println("Escolha um tipo entre 1 e 4.");
            }
        } while (novoTipo < 1 || novoTipo > 4);

        String novoModelo;
        do {
            novoModelo = capturarEntrada("Modelo da impressora (i8 ou i9): ").toLowerCase();
            if (!novoModelo.equals("i8") && !novoModelo.equals("i9")) {
                System.out.println("Digite i8 ou i9.");
            }
        } while (!novoModelo.equals("i8") && !novoModelo.equals("i9"));

        String novaConexao;
        int novoParametro;
        if (novoTipo == 1) {
            novaConexao = "USB";
            novoParametro = 0;
            System.out.println("USB selecionado; conexão USB e parâmetro 0 serão usados.");
        } else {
            novaConexao = capturarEntrada("Porta, IP ou identificador da conexão: ");
            novoParametro = lerInteiro("Parâmetro numérico da conexão: ");
        }

        tipo = novoTipo;
        modelo = novoModelo;
        conexao = novaConexao;
        parametro = novoParametro;
        System.out.println("Configuração salva: tipo=" + tipo + ", modelo=" + modelo
                + ", conexão=" + conexao + ", parâmetro=" + parametro);
    }

    public static void abrirConexao() {
        if (conexaoAberta) {
            System.out.println("A conexão já está aberta.");
            return;
        }
        int retorno = ImpressoraDLL.INSTANCE.AbreConexaoImpressora(tipo, modelo, conexao, parametro);
        if (retorno == 0) {
            conexaoAberta = true;
            System.out.println("Conexão aberta com sucesso.");
        } else {
            System.out.println("Erro ao abrir a conexão. Código: " + retorno);
        }
    }

    public static void fecharConexao() {
        if (!conexaoAberta) {
            System.out.println("A conexão já está fechada.");
            return;
        }
        int retorno = ImpressoraDLL.INSTANCE.FechaConexaoImpressora();
        mostrarResultado("Fechamento da conexão", retorno);
        if (retorno == 0) {
            conexaoAberta = false;
        }
    }

public static void imprimirTexto() {
        if (!exigirConexao()) return;
        String texto = capturarEntrada("Texto a imprimir: ");
        if (texto.isEmpty()) {
            System.out.println("Digite algum texto antes de imprimir.");
            return;
        }

        int copias = lerInteiro("Quantas cópias? ");
        if (copias < 1) {
            System.out.println("Informe pelo menos uma cópia.");
            return;
        }

        int copiasEnviadas = 0;
        for (int i = 1; i <= copias; i++) {
            int retorno = ImpressoraDLL.INSTANCE.ImpressaoTexto(texto, 1, 4, 0);
            if (retorno >= 0) {
                copiasEnviadas++;
                System.out.println("Cópia " + i + " enviada à impressora.");
            } else {
                System.out.println("Erro ao imprimir a cópia " + i + ". Código: " + retorno);
            }
        }

        System.out.println("Total de cópias enviadas: " + copiasEnviadas + " de " + copias + ".");
        if (copiasEnviadas > 0) {
            cortarPapel();
        }
    }

    public static void imprimirQRCode() {
        if (!exigirConexao()) return;
        String dados = capturarEntrada("Conteúdo do QR Code: ");
        int retorno = ImpressoraDLL.INSTANCE.ImpressaoQRCode(dados, 6, 4);
        mostrarResultado("Impressão do QR Code", retorno);
        if (retorno == 0) cortarPapel();
    }

    public static void imprimirCodigoBarras() {
        if (!exigirConexao()) return;
        String dados = capturarEntrada("Conteúdo do código de barras: ");
        if (!dados.startsWith("{A")) dados = "{A" + dados;
        int retorno = ImpressoraDLL.INSTANCE.ImpressaoCodigoBarras(8, dados, 100, 2, 3);
        mostrarResultado("Impressão do código de barras", retorno);
        if (retorno == 0) cortarPapel();
    }

    public static void imprimirXMLSAT() {
        if (!exigirConexao()) return;
        String nome = capturarEntrada("Arquivo XML SAT (ex.: XMLSAT.xml): ");
        String xml = lerArquivo(nome);
        if (xml == null) return;
        int retorno = ImpressoraDLL.INSTANCE.ImprimeXMLSAT(xml, 0);
        mostrarResultado("Impressão do XML SAT", retorno);
    }

    public static void imprimirXMLCancelamentoSAT() {
        if (!exigirConexao()) return;
        String nome = capturarEntrada("Arquivo XML de cancelamento (ex.: CANC_SAT.xml): ");
        String xml = lerArquivo(nome);
        if (xml == null) return;
        int retorno = ImpressoraDLL.INSTANCE.ImprimeXMLCancelamentoSAT(xml, ASS_QRCODE, 0);
        mostrarResultado("Impressão do XML de cancelamento", retorno);
    }

    public static void abrirGaveta() {
        if (!exigirConexao()) return;
        int retorno = ImpressoraDLL.INSTANCE.AbreGaveta(1, 5, 10);
        mostrarResultado("Abertura da gaveta", retorno);
    }

public static void sinalSonoro() {
        if (!exigirConexao()) return;
        int quantidade;
        do {
            quantidade = lerInteiro("Quantos bips (1 a 63)? ");
            if (quantidade < 1 || quantidade > 63) System.out.println("Escolha um valor de 1 a 63.");
        } while (quantidade < 1 || quantidade > 63);
        int retorno = ImpressoraDLL.INSTANCE.SinalSonoro(quantidade, 50, 5);
        mostrarResultado("Sinal sonoro", retorno);
    }

    public static void avancarPapel() {
        if (!exigirConexao()) return;
        int linhas = lerInteiro("Quantas linhas deseja avançar? ");
        if (linhas < 1) {
            System.out.println("Informe pelo menos uma linha.");
            return;
        }
        int retorno = ImpressoraDLL.INSTANCE.AvancaPapel(linhas);
        mostrarResultado("Avanço do papel", retorno);
    }

    // Consulta o status atual da impressora.
    public static void consultarStatus() {
        if (!exigirConexao()) return;
        int retorno = ImpressoraDLL.INSTANCE.StatusImpressora(0);
        System.out.println("Código retornado pelo status: " + retorno);
    }

    public static void exibirMenu() {
        System.out.println("\n===== MENU DA IMPRESSORA ELGIN =====");
        System.out.println("1  - Configurar conexão");
        System.out.println("2  - Abrir conexão");
        System.out.println("3  - Fechar conexão");
        System.out.println("4  - Imprimir texto");
        System.out.println("5  - Imprimir QR Code");
        System.out.println("6  - Imprimir código de barras");
        System.out.println("7  - Imprimir XML SAT");
        System.out.println("8  - Imprimir XML de cancelamento SAT");
        System.out.println("9  - Abrir gaveta");
        System.out.println("10 - Emitir sinal sonoro");
        System.out.println("11 - Avançar papel");
        System.out.println("12 - Consultar status");
        System.out.println("0  - Sair");
    }

    public static void main(String[] args) {
        try {
            if (!fazerLogin()) return;

            int opcao;
            do {
                exibirMenu();
                opcao = lerInteiro("Digite a opção desejada: ");
                if (opcao == 1) {
                    configurarConexao();
                } else if (opcao == 2) {
                    abrirConexao();
                } else if (opcao == 3) {
                    fecharConexao();
                } else if (opcao == 4) {
                    imprimirTexto();
                } else if (opcao == 5) {
                    imprimirQRCode();
                } else if (opcao == 6) {
                    imprimirCodigoBarras();
                } else if (opcao == 7) {
                    imprimirXMLSAT();
                } else if (opcao == 8) {
                    imprimirXMLCancelamentoSAT();
                } else if (opcao == 9) {
                    abrirGaveta();
                } else if (opcao == 10) {
                    sinalSonoro();
                } else if (opcao == 11) {
                    avancarPapel();
                } else if (opcao == 12) {
                    consultarStatus();
                } else if (opcao == 0) {
                    fecharConexao();
                } else {
                    System.out.println("Opção inválida.");
                }
            } while (opcao != 0);

            System.out.println("Programa encerrado.");
        } finally {
            scanner.close();
        }
    }
}
