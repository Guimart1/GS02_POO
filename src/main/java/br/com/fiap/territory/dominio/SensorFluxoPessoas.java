package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;

import java.util.Random;

public class SensorFluxoPessoas implements Sensor {

    private static final String TIPO = "FLUXO_PESSOAS";
    private final Random random;
    private boolean ativo;

    public SensorFluxoPessoas() {
        this(new Random(), true);
    }

    public SensorFluxoPessoas(Random random, boolean ativo) {
        this.random = random;
        this.ativo = ativo;
    }

    @Override

    public double lerValor() throws FonteIndisponivelException, DadosInvalidosException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + TIPO + " está indisponível no momento.");
        }
        double leitura = 20.0 + (random.nextDouble() * 95.0);
        double arredondado = Math.round(leitura * 100.0) / 100.0;
        if (arredondado < 0 || arredondado > 500) {
            throw new DadosInvalidosException("Leitura de fluxo de pessoas fora da faixa válida: " + arredondado);
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
        return "pessoas/h";
    }
}
