package br.com.fiap.territory;

import br.com.fiap.territory.dominio.EstrategiaPrevisao;
import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.PrevisaoComparacao;
import br.com.fiap.territory.dominio.PrevisaoMediaMovel;
import br.com.fiap.territory.dominio.TipoPrevisao;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.servico.ConfiguracaoSistema;
import br.com.fiap.territory.servico.FabricaPrevisao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrevisaoMediaMovelTest {

    @AfterEach
    void limparConfiguracao() {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
    }

    @Test
    void deveCalcularMediaMovelDasUltimasNLeituras() throws DadosInvalidosException {
        // Arrange
        LocalDateTime base = LocalDateTime.now();
        List<Medicao> historico = List.of(
                new Medicao("FLUXO_PESSOAS", "UBS-01", 90.0, "pessoas/h", base),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 80.0, "pessoas/h", base.minusHours(1)),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 70.0, "pessoas/h", base.minusHours(2)),
                new Medicao("FLUXO_PESSOAS", "UBS-01", 10.0, "pessoas/h", base.minusHours(3))
        );
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);

        // Act
        double previsto = estrategia.prever(historico);

        assertEquals(80.0, previsto);
    }

    @Test
    void deveCalcularProjecaoDeTendenciaNaPrevisaoComparacao() throws DadosInvalidosException {
        // Arrange
        LocalDateTime base = LocalDateTime.now();
        List<Medicao> historico = List.of(
                new Medicao("TEMPO_ESPERA", "UBS-01", 90.0, "min", base),
                new Medicao("TEMPO_ESPERA", "UBS-01", 80.0, "min", base.minusHours(1)),
                new Medicao("TEMPO_ESPERA", "UBS-01", 60.0, "min", base.minusHours(2)),
                new Medicao("TEMPO_ESPERA", "UBS-01", 50.0, "min", base.minusHours(3))
        );
        PrevisaoComparacao estrategia = new PrevisaoComparacao();

        // Act
        double previsto = estrategia.prever(historico);

        assertEquals(100.0, previsto);
    }

    @Test
    void deveLancarDadosInvalidosExceptionQuandoHistoricoEstiverVazio() {
        // Arrange
        EstrategiaPrevisao estrategia = new PrevisaoMediaMovel();

        // Act & Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(List.of()));
    }

    @Test
    void deveCriarEstrategiaCorretaViaFabricaPrevisao() throws DadosInvalidosException {
        // Arrange
        ConfiguracaoSistema.getInstancia().setTipoPrevisaoAtiva(TipoPrevisao.COMPARACAO);

        // Act
        EstrategiaPrevisao estrategia = FabricaPrevisao.criarEstrategia();

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }
}
