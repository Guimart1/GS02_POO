package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.List;

public abstract class EstrategiaPrevisao {

    private final String nomeEstrategia;


    protected EstrategiaPrevisao(String nomeEstrategia) {
        this.nomeEstrategia = nomeEstrategia;
    }

    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        if (historico == null || historico.isEmpty()) {
            throw new DadosInvalidosException(
                    "Não há medições no histórico para executar a estratégia: " + nomeEstrategia);
        }
        return 0.0;
    }

    public abstract String explicarResultado(double valorPrevisto, List<Medicao> historico);

    public String getNomeEstrategia() {
        return nomeEstrategia;
    }
}
