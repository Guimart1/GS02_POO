package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.List;

public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private final int janela;


    public PrevisaoMediaMovel() {
        this(5);
    }

    public PrevisaoMediaMovel(int janela) {
        super("Média Móvel");
        this.janela = Math.max(1, janela);
    }

    @Override

    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        super.prever(historico);

        int quantidade = Math.min(janela, historico.size());
        double soma = 0.0;
        for (int i = 0; i < quantidade; i++) {
            soma += historico.get(i).getValor();
        }
        double media = soma / quantidade;
        return Math.round(media * 100.0) / 100.0;
    }

    @Override

    public String explicarResultado(double valorPrevisto, List<Medicao> historico) {
        int usadas = Math.min(janela, historico.size());
        String unidade = historico.get(0).getUnidade();
        return String.format(
                "Média móvel das últimas %d leitura(s) indica demanda esperada de %.2f %s.",
                usadas, valorPrevisto, unidade);
    }


    public int getJanela() {
        return janela;
    }
}
