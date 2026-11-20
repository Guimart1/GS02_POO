package br.com.fiap.territory;

import br.com.fiap.territory.dominio.Alerta;
import br.com.fiap.territory.dominio.ComponenteTerritorial;
import br.com.fiap.territory.dominio.DadosTerritorio;
import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.Sensor;
import br.com.fiap.territory.dominio.TipoPrevisao;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.servico.ConfiguracaoSistema;
import br.com.fiap.territory.servico.ExportadorCsv;
import br.com.fiap.territory.servico.ServicoMonitoramento;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class SistemaMonitoramento {

    private static final DateTimeFormatter DATA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        SpringApplication.run(SistemaMonitoramento.class, args);
    }

    @Bean
    @Profile("!test")

    public CommandLineRunner iniciarMenu(ServicoMonitoramento servico) {
        return args -> executarLoopMenu(servico);
    }


    private void executarLoopMenu(ServicoMonitoramento servico) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean executando = true;
            while (executando) {
                exibirMenuPrincipal();
                String opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1" -> tratarVerificarSensores(servico);
                    case "2" -> tratarRodarPrevisoes(servico);
                    case "3" -> tratarVerHistorico(scanner, servico);
                    case "4" -> tratarGerenciarTerritorio(scanner, servico);
                    case "5" -> tratarStatusCompleto(servico);
                    case "6" -> tratarCatalisadorAlertas(scanner, servico);
                    case "0" -> {
                        System.out.println("\nEncerrando o sistema. Os dados permanecem salvos no banco H2.");
                        executando = false;
                    }
                    default -> System.out.println("\n[AVISO] Opção inválida. Escolha um número de 0 a 6.");
                }
            }
        }
    }

    private void exibirMenuPrincipal() {
        System.out.println("\n========================================================");
        System.out.println("       UBStatus - MONITORAMENTO DE DEMANDA POR SERVIÇOS");
        System.out.println("========================================================");
        System.out.println("1. Verificar sensores (coletar e gravar medições)");
        System.out.println("2. Rodar previsões e verificar alertas");
        System.out.println("3. Ver histórico (medições, alertas e exportar CSV)");
        System.out.println("4. Gerenciar dados do território e estratégia");
        System.out.println("5. Status completo do território");
        System.out.println("6. Catalisador de alertas (simular situação de risco)");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void tratarVerificarSensores(ServicoMonitoramento servico) {
        System.out.println("\n--- Leitura dos Sensores ---");
        List<String> resultados = servico.coletarESalvarLeituras();
        resultados.forEach(System.out::println);
    }

    private void tratarRodarPrevisoes(ServicoMonitoramento servico) {
        System.out.println("\n--- Execução de Previsões ---");
        System.out.println("Estratégia ativa: " + ConfiguracaoSistema.getInstancia().getTipoPrevisaoAtiva().getDescricao());
        List<String> resultados = servico.executarPrevisoesParaTodosSensores();
        resultados.forEach(System.out::println);
    }


    private void tratarVerHistorico(Scanner scanner, ServicoMonitoramento servico) {
        System.out.println("\n--- Histórico Persistido ---");
        System.out.println("1. Listar medições por sensor");
        System.out.println("2. Listar alertas emitidos");
        System.out.print("Escolha: ");
        String escolha = scanner.nextLine().trim();

        if ("1".equals(escolha)) {
            Sensor sensor = selecionarSensor(scanner, servico.getSensores());
            if (sensor == null) {
                return;
            }
            List<Medicao> medicoes = servico.buscarHistoricoPorSensor(sensor.retornarTipo());
            if (medicoes.isEmpty()) {
                System.out.println("Nenhuma medição registrada para " + sensor.retornarTipo() + ".");
                return;
            }
            System.out.println("\nMedições de " + sensor.retornarTipo() + " (" + medicoes.size() + " registro(s)):");
            for (Medicao m : medicoes) {
                System.out.printf("  #%d | %.2f %s | %s | %s%n",
                        m.getId(), m.getValor(), m.getUnidade(), m.getComponenteId(),
                        m.getDataHora().format(DATA_FMT));
            }
            perguntarExportacaoMedicoesCsv(scanner, sensor.retornarTipo(), medicoes);
        } else if ("2".equals(escolha)) {
            List<Alerta> alertas = servico.buscarAlertasEmitidos();
            if (alertas.isEmpty()) {
                System.out.println("Nenhum alerta emitido até o momento.");
                return;
            }
            System.out.println("\nAlertas emitidos (" + alertas.size() + " registro(s)):");
            for (Alerta a : alertas) {
                System.out.printf("  #%d | [%s] %s | %s%n",
                        a.getId(), a.getNivel(), a.getDataHora().format(DATA_FMT), a.getMensagem());
            }
            perguntarExportacaoAlertasCsv(scanner, alertas);
        } else {
            System.out.println("[AVISO] Subopção inválida.");
        }
    }


    private Sensor selecionarSensor(Scanner scanner, List<Sensor> sensores) {
        System.out.println("Sensores disponíveis:");
        for (int i = 0; i < sensores.size(); i++) {
            System.out.printf("%d. %s (%s)%n",
                    i + 1, sensores.get(i).retornarTipo(), sensores.get(i).getUnidadeMedida());
        }
        System.out.print("Número do sensor: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= sensores.size()) {
                System.out.println("[AVISO] Índice de sensor fora do intervalo.");
                return null;
            }
            return sensores.get(idx);
        } catch (NumberFormatException e) {
            System.out.println("[ERRO] Entrada inválida: informe um número inteiro para o sensor.");
            return null;
        }
    }

    private void perguntarExportacaoMedicoesCsv(Scanner scanner, String sensorTipo, List<Medicao> medicoes) {
        System.out.print("Deseja exportar este histórico para CSV? (S/N): ");
        if ("S".equalsIgnoreCase(scanner.nextLine().trim())) {
            try {
                Path arquivo = ExportadorCsv.exportarMedicoes(sensorTipo, medicoes);
                System.out.println("[OK] Arquivo CSV gerado em: " + arquivo.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("[ERRO] Falha ao exportar CSV de medições: " + e.getMessage());
            }
        }
    }

    private void perguntarExportacaoAlertasCsv(Scanner scanner, List<Alerta> alertas) {
        System.out.print("Deseja exportar os alertas para CSV? (S/N): ");
        if ("S".equalsIgnoreCase(scanner.nextLine().trim())) {
            try {
                Path arquivo = ExportadorCsv.exportarAlertas(alertas);
                System.out.println("[OK] Arquivo CSV gerado em: " + arquivo.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("[ERRO] Falha ao exportar CSV de alertas: " + e.getMessage());
            }
        }
    }

    private void tratarGerenciarTerritorio(Scanner scanner, ServicoMonitoramento servico) {
        DadosTerritorio territorio = servico.getDadosTerritorio();
        ConfiguracaoSistema cfg = ConfiguracaoSistema.getInstancia();

        System.out.println("\n--- Gestão do Território e Estratégia ---");
        System.out.printf("Região: %s | População atendida: %d habitantes%n",
                territorio.getRegiao(), territorio.getPopulacaoAtendida());
        System.out.printf("Limiares atuais -> Atenção: %.1f | Alerta: %.1f | Crítico: %.1f%n",
                cfg.getLimiarAtencao(), cfg.getLimiarAlerta(), cfg.getLimiarCritico());
        System.out.println("Estratégia atual: " + cfg.getTipoPrevisaoAtiva().getDescricao());
        System.out.println("\n1. Alterar limiares (requer código de acesso - padrão: GS2026)");
        System.out.println("2. Alternar estratégia de previsão");
        System.out.println("3. Atualizar população atendida");
        System.out.print("Escolha (ou Enter para voltar): ");
        String op = scanner.nextLine().trim();

        try {
            switch (op) {
                case "1" -> {
                    System.out.print("Código de acesso: ");
                    String codigo = scanner.nextLine().trim();
                    System.out.print("Novo limiar ATENÇÃO: ");
                    double atencao = Double.parseDouble(scanner.nextLine().trim());
                    System.out.print("Novo limiar ALERTA: ");
                    double alerta = Double.parseDouble(scanner.nextLine().trim());
                    System.out.print("Novo limiar CRÍTICO: ");
                    double critico = Double.parseDouble(scanner.nextLine().trim());
                    territorio.alterarLimiares(codigo, atencao, alerta, critico);
                    System.out.println("[OK] Limiares atualizados com sucesso.");
                }
                case "2" -> {
                    System.out.println("1. Média Móvel");
                    System.out.println("2. Comparação com Período Anterior");
                    System.out.print("Escolha a estratégia: ");
                    String est = scanner.nextLine().trim();
                    if ("1".equals(est)) {
                        cfg.setTipoPrevisaoAtiva(TipoPrevisao.MEDIA_MOVEL);
                    } else if ("2".equals(est)) {
                        cfg.setTipoPrevisaoAtiva(TipoPrevisao.COMPARACAO);
                    } else {
                        System.out.println("[AVISO] Estratégia inválida.");
                        return;
                    }
                    System.out.println("[OK] Estratégia alterada para: " + cfg.getTipoPrevisaoAtiva().getDescricao());
                }
                case "3" -> {
                    System.out.print("Nova população atendida: ");
                    int pop = Integer.parseInt(scanner.nextLine().trim());
                    territorio.setPopulacaoAtendida(pop);
                    System.out.println("[OK] População atualizada para " + pop + " habitantes.");
                }
                default -> {
                    if (!op.isEmpty()) {
                        System.out.println("[AVISO] Subopção inválida.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERRO] Valor numérico inválido informado: " + e.getMessage());
        } catch (DadosInvalidosException e) {
            System.out.println("[ERRO DE VALIDAÇÃO] " + e.getMessage());
        }
    }

    private void tratarStatusCompleto(ServicoMonitoramento servico) {
        ConfiguracaoSistema cfg = ConfiguracaoSistema.getInstancia();
        DadosTerritorio territorio = servico.getDadosTerritorio();

        System.out.println("\n--- Status Completo do Território ---");
        System.out.printf("Território: %s | População atendida: %d hab.%n",
                territorio.getRegiao(), territorio.getPopulacaoAtendida());
        System.out.printf("Limiares: Atenção=%.1f | Alerta=%.1f | Crítico=%.1f | Estratégia=%s%n",
                cfg.getLimiarAtencao(), cfg.getLimiarAlerta(), cfg.getLimiarCritico(),
                cfg.getTipoPrevisaoAtiva().getDescricao());

        System.out.println("\nEquipamentos Públicos Monitorados:");
        for (ComponenteTerritorial c : servico.getComponentes()) {
            System.out.println("  - " + c.verificarStatus());
        }

        System.out.println("\nSensores Conectados:");
        for (Sensor s : servico.getSensores()) {
            int qtd = servico.buscarHistoricoPorSensor(s.retornarTipo()).size();
            System.out.printf("  - %s (%s) | Operacional: %s | Medições no banco: %d%n",
                    s.retornarTipo(), s.getUnidadeMedida(),
                    s.verificarFuncionamento() ? "SIM" : "NÃO", qtd);
        }

        System.out.println("\nTotal de alertas registrados no banco: " + servico.buscarAlertasEmitidos().size());
    }


    private void tratarCatalisadorAlertas(Scanner scanner, ServicoMonitoramento servico) {
        System.out.println("\n--- Catalisador de Alertas (Simular Situação de Risco) ---");
        Sensor sensor = selecionarSensor(scanner, servico.getSensores());
        if (sensor == null) {
            return;
        }

        ConfiguracaoSistema cfg = ConfiguracaoSistema.getInstancia();
        System.out.println("\nEscolha a intensidade da situação de risco:");
        System.out.printf("1. Risco ATENÇÃO (simula %.1f %s)%n", cfg.getLimiarAtencao() + 5.0, sensor.getUnidadeMedida());
        System.out.printf("2. Risco ALERTA (simula %.1f %s)%n", cfg.getLimiarAlerta() + 5.0, sensor.getUnidadeMedida());
        System.out.printf("3. Risco CRÍTICO (simula %.1f %s)%n", cfg.getLimiarCritico() + 10.0, sensor.getUnidadeMedida());
        System.out.println("4. Informar valor manualmente");
        System.out.print("Escolha: ");
        String nivelEscolha = scanner.nextLine().trim();

        try {
            double valorSimulado;
            switch (nivelEscolha) {
                case "1" -> valorSimulado = cfg.getLimiarAtencao() + 5.0;
                case "2" -> valorSimulado = cfg.getLimiarAlerta() + 5.0;
                case "3" -> valorSimulado = cfg.getLimiarCritico() + 10.0;
                case "4" -> {
                    System.out.print("Informe o valor da medição simulada (" + sensor.getUnidadeMedida() + "): ");
                    valorSimulado = Double.parseDouble(scanner.nextLine().trim());
                }
                default -> {
                    System.out.println("[AVISO] Opção de risco inválida.");
                    return;
                }
            }

            ServicoMonitoramento.ResultadoPrevisao resultado =
                    servico.catalisarSituacaoDeRisco(sensor, valorSimulado, 5);

            System.out.printf("[OK] 5 medições de %.2f %s gravadas no banco para %s.%n",
                    valorSimulado, sensor.getUnidadeMedida(), sensor.retornarTipo());
            System.out.printf("Previsão (%s): %.2f %s | %s%n",
                    resultado.estrategia(), resultado.valorPrevisto(),
                    sensor.getUnidadeMedida(), resultado.explicacao());
            if (resultado.alerta() != null) {
                System.out.println("-> " + resultado.alerta().getMensagem());
            } else {
                System.out.println("-> Valor simulado não ultrapassou os limiares configurados (sem alerta).");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERRO] Valor numérico inválido: " + e.getMessage());
        } catch (DadosInvalidosException e) {
            System.out.println("[ERRO DE VALIDAÇÃO] " + e.getMessage());
        }
    }
}
