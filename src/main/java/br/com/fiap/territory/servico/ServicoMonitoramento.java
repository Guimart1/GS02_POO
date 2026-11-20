package br.com.fiap.territory.servico;

import br.com.fiap.territory.dominio.Alerta;
import br.com.fiap.territory.dominio.CentroAssistenciaSocial;
import br.com.fiap.territory.dominio.ComponenteTerritorial;
import br.com.fiap.territory.dominio.DadosTerritorio;
import br.com.fiap.territory.dominio.EstrategiaPrevisao;
import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.NivelAlerta;
import br.com.fiap.territory.dominio.Sensor;
import br.com.fiap.territory.dominio.SensorFluxoPessoas;
import br.com.fiap.territory.dominio.SensorOcupacaoAtendimento;
import br.com.fiap.territory.dominio.SensorTempoEspera;
import br.com.fiap.territory.dominio.UnidadeBasicaSaude;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;
import br.com.fiap.territory.persistencia.AlertaRepository;
import br.com.fiap.territory.persistencia.MedicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ServicoMonitoramento {

    private final MedicaoRepository medicaoRepository;
    private final AlertaRepository alertaRepository;
    private final List<ComponenteTerritorial> componentes;
    private final List<Sensor> sensores;
    private final DadosTerritorio dadosTerritorio;

    @Autowired

    public ServicoMonitoramento(MedicaoRepository medicaoRepository, AlertaRepository alertaRepository)
            throws DadosInvalidosException {
        this(
                medicaoRepository,
                alertaRepository,
                criarComponentesPadrao(),
                criarSensoresPadrao(),
                new DadosTerritorio("Distrito Leste - Vila Esperança", 48500, "GS2026")
        );
    }

    public ServicoMonitoramento(MedicaoRepository medicaoRepository,
                                AlertaRepository alertaRepository,
                                List<ComponenteTerritorial> componentes,
                                List<Sensor> sensores,
                                DadosTerritorio dadosTerritorio) {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
        this.componentes = new ArrayList<>(componentes);
        this.sensores = new ArrayList<>(sensores);
        this.dadosTerritorio = dadosTerritorio;
    }

    private static List<ComponenteTerritorial> criarComponentesPadrao() throws DadosInvalidosException {
        List<ComponenteTerritorial> lista = new ArrayList<>();
        lista.add(new UnidadeBasicaSaude(
                "UBS-01", "UBS Jardim Aurora", "Distrito Leste", "OPERACIONAL", 65, 4));
        lista.add(new CentroAssistenciaSocial(
                "CRAS-01", "CRAS Vila Esperança", "Distrito Leste", "OPERACIONAL", 8, 180));
        return lista;
    }


    private static List<Sensor> criarSensoresPadrao() {
        return List.of(
                new SensorFluxoPessoas(),
                new SensorTempoEspera(),
                new SensorOcupacaoAtendimento()
        );
    }

    public List<String> coletarESalvarLeituras() {
        List<String> resultados = new ArrayList<>();
        String componentePrincipalId = componentes.isEmpty() ? "TERRITORIO-01" : componentes.get(0).getId();

        for (Sensor sensor : sensores) {
            try {
                double valor = sensor.lerValor();
                Medicao medicao = new Medicao(
                        sensor.retornarTipo(),
                        componentePrincipalId,
                        valor,
                        sensor.getUnidadeMedida(),
                        LocalDateTime.now()
                );
                medicaoRepository.save(medicao);
                resultados.add(String.format("[OK] %s: %.2f %s (gravada no banco)",
                        sensor.retornarTipo(), valor, sensor.getUnidadeMedida()));
            } catch (FonteIndisponivelException | DadosInvalidosException e) {
                resultados.add(String.format("[ERRO] %s: %s", sensor.retornarTipo(), e.getMessage()));
            }
        }
        return resultados;
    }

    public ResultadoPrevisao avaliarPrevisaoEAlertas(String sensorTipo) throws DadosInvalidosException {
        List<Medicao> historico = medicaoRepository.findBySensorTipoOrderByDataHoraDesc(sensorTipo);
        EstrategiaPrevisao estrategia = FabricaPrevisao.criarEstrategia();
        double valorPrevisto = estrategia.prever(historico);
        String explicacao = estrategia.explicarResultado(valorPrevisto, historico);

        int minimo = ConfiguracaoSistema.getInstancia().getMinimoLeiturasConfiaveis();
        boolean baixaConfiabilidade = historico.size() < minimo;

        Optional<Alerta> alertaEmitido = verificarLimiarEGerarAlerta(sensorTipo, valorPrevisto);
        if (alertaEmitido.isPresent()) {
            alertaRepository.save(alertaEmitido.get());
        }

        return new ResultadoPrevisao(
                sensorTipo,
                estrategia.getNomeEstrategia(),
                valorPrevisto,
                explicacao,
                historico.size(),
                baixaConfiabilidade,
                alertaEmitido.orElse(null)
        );
    }


    public List<String> executarPrevisoesParaTodosSensores() {
        List<String> saidas = new ArrayList<>();
        for (Sensor sensor : sensores) {
            try {
                ResultadoPrevisao res = avaliarPrevisaoEAlertas(sensor.retornarTipo());
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("[%s] Estratégia: %s | Previsto: %.2f %s | %s",
                        res.sensorTipo(), res.estrategia(), res.valorPrevisto(),
                        sensor.getUnidadeMedida(), res.explicacao()));
                if (res.baixaConfiabilidade()) {
                    sb.append(String.format(
                            "%n   -> AVISO: Apenas %d leitura(s) no histórico (recomendado >= %d para maior confiabilidade).",
                            res.totalLeituras(), ConfiguracaoSistema.getInstancia().getMinimoLeiturasConfiaveis()));
                }
                if (res.alerta() != null) {
                    sb.append(String.format("%n   -> %s", res.alerta().getMensagem()));
                } else {
                    sb.append("\n   -> Situação dentro dos limites normais (sem alerta).");
                }
                saidas.add(sb.toString());
            } catch (DadosInvalidosException e) {
                saidas.add(String.format("[%s] Previsão não executada: %s", sensor.retornarTipo(), e.getMessage()));
            }
        }
        return saidas;
    }


    private Optional<Alerta> verificarLimiarEGerarAlerta(String sensorTipo, double valorPrevisto)
            throws DadosInvalidosException {
        ConfiguracaoSistema cfg = ConfiguracaoSistema.getInstancia();
        NivelAlerta nivel = null;
        double limiarEstourado = 0.0;

        if (valorPrevisto >= cfg.getLimiarCritico()) {
            nivel = NivelAlerta.CRITICO;
            limiarEstourado = cfg.getLimiarCritico();
        } else if (valorPrevisto >= cfg.getLimiarAlerta()) {
            nivel = NivelAlerta.ALERTA;
            limiarEstourado = cfg.getLimiarAlerta();
        } else if (valorPrevisto >= cfg.getLimiarAtencao()) {
            nivel = NivelAlerta.ATENCAO;
            limiarEstourado = cfg.getLimiarAtencao();
        }

        if (nivel == null) {
            return Optional.empty();
        }

        String mensagem = String.format(
                "ALERTA [%s] no sensor %s: valor previsto de %.2f ultrapassou o limiar de %.2f.",
                nivel, sensorTipo, valorPrevisto, limiarEstourado);

        return Optional.of(new Alerta(
                nivel,
                sensorTipo,
                valorPrevisto,
                limiarEstourado,
                mensagem,
                LocalDateTime.now()
        ));
    }


    public ResultadoPrevisao catalisarSituacaoDeRisco(Sensor sensor, double valorSimulado, int quantidadeLeituras)
            throws DadosInvalidosException {
        if (sensor == null) {
            throw new DadosInvalidosException("Sensor para simulação de risco não pode ser nulo.");
        }
        if (quantidadeLeituras <= 0) {
            throw new DadosInvalidosException("Quantidade de leituras simuladas deve ser maior que zero.");
        }

        String componentePrincipalId = componentes.isEmpty() ? "TERRITORIO-01" : componentes.get(0).getId();
        LocalDateTime agora = LocalDateTime.now();

        for (int i = 0; i < quantidadeLeituras; i++) {
            Medicao medicao = new Medicao(
                    sensor.retornarTipo(),
                    componentePrincipalId,
                    valorSimulado,
                    sensor.getUnidadeMedida(),
                    agora.minusMinutes((long) (quantidadeLeituras - 1 - i) * 10L)
            );
            medicaoRepository.save(medicao);
        }

        return avaliarPrevisaoEAlertas(sensor.retornarTipo());
    }

    public List<Medicao> buscarHistoricoPorSensor(String sensorTipo) {
        return medicaoRepository.findBySensorTipoOrderByDataHoraDesc(sensorTipo);
    }

    public List<Alerta> buscarAlertasEmitidos() {
        return alertaRepository.findAllByOrderByDataHoraDesc();
    }

    public List<ComponenteTerritorial> getComponentes() {
        return Collections.unmodifiableList(componentes);
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    public DadosTerritorio getDadosTerritorio() {
        return dadosTerritorio;
    }

    public record ResultadoPrevisao(
            String sensorTipo,
            String estrategia,
            double valorPrevisto,
            String explicacao,
            int totalLeituras,
            boolean baixaConfiabilidade,
            Alerta alerta
    ) {
    }
}
