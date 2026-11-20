package br.com.fiap.territory;

import br.com.fiap.territory.dominio.DadosTerritorio;
import br.com.fiap.territory.dominio.Medicao;
import br.com.fiap.territory.dominio.SensorFluxoPessoas;
import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicaoTest {

    @Test
    void deveCriarMedicaoValidaComSucesso() throws DadosInvalidosException {
        // Arrange
        String sensorTipo = "FLUXO_PESSOAS";
        String componenteId = "UBS-01";
        double valor = 72.5;
        String unidade = "pessoas/h";
        LocalDateTime agora = LocalDateTime.now();

        // Act
        Medicao medicao = new Medicao(sensorTipo, componenteId, valor, unidade, agora);

        // Assert
        assertEquals(sensorTipo, medicao.getSensorTipo());
        assertEquals(componenteId, medicao.getComponenteId());
        assertEquals(72.5, medicao.getValor());
        assertEquals(unidade, medicao.getUnidade());
    }

    @Test
    void deveLancarDadosInvalidosExceptionQuandoValorMedicaoForNegativo() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act & Assert
        assertThrows(DadosInvalidosException.class, () ->
                new Medicao("FLUXO_PESSOAS", "UBS-01", -5.0, "pessoas/h", agora));
    }

    @Test
    void deveLancarDadosInvalidosExceptionParaCodigoDeAcessoInvalidoOuPopulacaoNegativa()
            throws DadosInvalidosException {
        // Arrange
        DadosTerritorio territorio = new DadosTerritorio("Distrito Leste", 30000, "GS2026");

        // Act & Assert
        assertThrows(DadosInvalidosException.class, () ->
                territorio.alterarLimiares("CODIGO_ERRADO", 50.0, 70.0, 90.0));
        assertThrows(DadosInvalidosException.class, () ->
                territorio.setPopulacaoAtendida(-100));
    }

    @Test
    void deveLancarFonteIndisponivelExceptionQuandoSensorEstiverDesligado() {
        // Arrange
        SensorFluxoPessoas sensorOffline = new SensorFluxoPessoas(new Random(42), false);

        // Act & Assert
        assertThrows(FonteIndisponivelException.class, sensorOffline::lerValor);
    }
}
