package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorTempoEspera implements Sensor {

    private static final String TIPO = "TEMPO_ESPERA";
    private final Random random;
    private boolean ativo;

    public SensorTempoEspera() {
        this(new Random(), true);
    }

    public SensorTempoEspera(Random random, boolean ativo) {
        this.random = random;
        this.ativo = ativo;
    }

    @Override

    public double lerValor() throws FonteIndisponivelException, DadosInvalidosException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + TIPO + " falhou ao responder.");
        }
        double leitura = 10.0 + (random.nextDouble() * 115.0);
        double arredondado = Math.round(leitura * 100.0) / 100.0;
        if (arredondado < 0 || arredondado > 600) {
            throw new DadosInvalidosException("Tempo de espera inválido: " + arredondado);
        }
        return arredondado;
    }

    @Override

    public boolean verificarFuncionamento() {
        return ativo;
    }


    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override

    public String retornarTipo() {
        return TIPO;
    }

    @Override

    public String getUnidadeMedida() {
        return "min";
    }
}
