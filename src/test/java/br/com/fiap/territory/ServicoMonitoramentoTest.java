package br.com.fiap.territory;

import br.com.fiap.territory.dominio.Alerta;
import br.com.fiap.territory.dominio.ComponenteTerritorial;
import br.com.fiap.territory.dominio.DadosTerritorio;
import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.NivelAlerta;
import br.com.fiap.territory.dominio.Sensor;
import br.com.fiap.territory.dominio.SensorFluxoPessoas;
import br.com.fiap.territory.dominio.UnidadeBasicaSaude;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.persistencia.AlertaRepository;
import br.com.fiap.territory.persistencia.MedicaoRepository;
import br.com.fiap.territory.servico.ConfiguracaoSistema;
import br.com.fiap.territory.servico.ServicoMonitoramento;
import br.com.fiap.territory.servico.ServicoMonitoramento.ResultadoPrevisao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoRepository medicaoRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @AfterEach
    void limparConfiguracao() {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
    }

    @Test
    void deveEmitirEPersistirAlertaCriticoQuandoPrevisaoUltrapassarLimiarCritico()
            throws DadosInvalidosException {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();
        List<Medicao> historicoCritico = List.of(
                new Medicao("FLUXO_PESSOAS", "UBS-01", 102.0, "pessoas/h", agora),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 98.0, "pessoas/h", agora.minusHours(1)),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 100.0, "pessoas/h", agora.minusHours(2))
        );
        when(medicaoRepository.findBySensorTipoOrderByDataHoraDesc("FLUXO_PESSOAS"))
                .thenReturn(historicoCritico);
        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoRepository, alertaRepository);

        // Act
        ResultadoPrevisao resultado = servico.avaliarPrevisaoEAlertas("FLUXO_PESSOAS");

        // Assert
        assertEquals(100.0, resultado.valorPrevisto());
        assertNotNull(resultado.alerta());
        assertEquals(NivelAlerta.CRITICO, resultado.alerta().getNivel());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    @Test
    void naoDeveEmitirAlertaQuandoPrevisaoEstiverAbaixoDoLimiarDeAtencao()
            throws DadosInvalidosException {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();
        List<Medicao> historicoNormal = List.of(
                new Medicao("TEMPO_ESPERA", "UBS-01", 30.0, "min", agora),
                new Medicao("TEMPO_ESPERA", "UBS-01", 40.0, "min", agora.minusHours(1))
        );
        when(medicaoRepository.findBySensorTipoOrderByDataHoraDesc("TEMPO_ESPERA"))
                .thenReturn(historicoNormal);

        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoRepository, alertaRepository);

        // Act
        ResultadoPrevisao resultado = servico.avaliarPrevisaoEAlertas("TEMPO_ESPERA");

        // Assert
        assertEquals(35.0, resultado.valorPrevisto());
        assertNull(resultado.alerta());
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void deveTratarSensorIndisponivelSemInterromperDemaisSensores() throws DadosInvalidosException {
        // Arrange
        ComponenteTerritorial ubs = new UnidadeBasicaSaude(
                "UBS-01", "UBS Teste", "Distrito Leste", "OPERACIONAL", 50, 3);
        Sensor sensorOnline = new SensorFluxoPessoas(new Random(10), true);
        Sensor sensorOffline = new SensorFluxoPessoas(new Random(10), false);
        DadosTerritorio territorio = new DadosTerritorio("Distrito Leste", 20000, "GS2026");

        ServicoMonitoramento servico = new ServicoMonitoramento(
                medicaoRepository,
                alertaRepository,
                List.of(ubs),
                List.of(sensorOnline, sensorOffline),
                territorio
        );

        // Act
        List<String> resultados = servico.coletarESalvarLeituras();

        // Assert
        assertEquals(2, resultados.size());
        assertTrue(resultados.get(0).startsWith("[OK]"));
        assertTrue(resultados.get(1).startsWith("[ERRO]"));
        verify(medicaoRepository, times(1)).save(any(Medicao.class));
    }

    @Test
    void deveCatalisarSituacaoDeRiscoEGerarAlertaCritico() throws DadosInvalidosException {
        // Arrange
        Sensor sensor = new SensorFluxoPessoas();
        LocalDateTime agora = LocalDateTime.now();
        List<Medicao> historicoSimulado = List.of(
                new Medicao("FLUXO_PESSOAS", "UBS-01", 105.0, "pessoas/h", agora),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 105.0, "pessoas/h", agora.minusMinutes(10)),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 105.0, "pessoas/h", agora.minusMinutes(20))
        );
        when(medicaoRepository.findBySensorTipoOrderByDataHoraDesc("FLUXO_PESSOAS"))
                .thenReturn(historicoSimulado);
        when(alertaRepository.save(any(Alerta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoRepository, alertaRepository);

        // Act
        ResultadoPrevisao resultado = servico.catalisarSituacaoDeRisco(sensor, 105.0, 3);

        // Assert
        verify(medicaoRepository, times(3)).save(any(Medicao.class));
        assertEquals(105.0, resultado.valorPrevisto());
        assertNotNull(resultado.alerta());
        assertEquals(NivelAlerta.CRITICO, resultado.alerta().getNivel());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }
}
