package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorOcupacaoAtendimento implements Sensor {

    private static final String TIPO = "OCUPACAO_ATENDIMENTO";
    private final Random random;
    private boolean ativo;

    public SensorOcupacaoAtendimento() {
        this(new Random(), true);
    }

    public SensorOcupacaoAtendimento(Random random, boolean ativo) {
        this.random = random;
        this.ativo = ativo;
    }

    @Override

    public double lerValor() throws FonteIndisponivelException, DadosInvalidosException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + TIPO + " sem comunicação com o painel.");
        }
        double leitura = 25.0 + (random.nextDouble() * 75.0);
        double arredondado = Math.round(leitura * 100.0) / 100.0;
        if (arredondado < 0 || arredondado > 100) {
            throw new DadosInvalidosException("Percentual de ocupação fora da faixa [0, 100]: " + arredondado);
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
        return "%";
    }
}
