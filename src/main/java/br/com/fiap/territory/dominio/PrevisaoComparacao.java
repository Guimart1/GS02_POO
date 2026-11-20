package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.List;

public class PrevisaoComparacao extends EstrategiaPrevisao {


    public PrevisaoComparacao() {
        super("Comparação com Período Anterior");
    }

    @Override

    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        super.prever(historico);

        if (historico.size() == 1) {
            return Math.round(historico.get(0).getValor() * 100.0) / 100.0;
        }

        int metade = historico.size() / 2;
        double somaRecente = 0.0;
        for (int i = 0; i < metade; i++) {
            somaRecente += historico.get(i).getValor();
        }
        double mediaRecente = somaRecente / metade;

        double somaAnterior = 0.0;
        int qtdAnterior = historico.size() - metade;
        for (int i = metade; i < historico.size(); i++) {
            somaAnterior += historico.get(i).getValor();
        }
        double mediaAnterior = somaAnterior / qtdAnterior;

        double variacao = mediaRecente - mediaAnterior;
        double projecao = Math.max(0.0, mediaRecente + (variacao * 0.5));
        return Math.round(projecao * 100.0) / 100.0;
    }

    @Override

    public String explicarResultado(double valorPrevisto, List<Medicao> historico) {
        String unidade = historico.get(0).getUnidade();
        if (historico.size() < 2) {
            return String.format(
                    "Apenas 1 leitura disponível; projeção mantida em %.2f %s.",
                    valorPrevisto, unidade);
        }
        int metade = historico.size() / 2;
        double mediaRecente = historico.subList(0, metade).stream()
                .mapToDouble(Medicao::getValor).average().orElse(0.0);
        double mediaAnterior = historico.subList(metade, historico.size()).stream()
                .mapToDouble(Medicao::getValor).average().orElse(0.0);
        String tendencia = mediaRecente >= mediaAnterior ? "alta" : "queda";

        return String.format(
                "Tendência de %s (recente %.2f vs. anterior %.2f %s): projeção de %.2f %s.",
                tendencia, mediaRecente, mediaAnterior, unidade, valorPrevisto, unidade);
    }
}
