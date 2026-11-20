package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.servico.ConfiguracaoSistema;

public class DadosTerritorio {

    private String regiao;
    private int populacaoAtendida;
    private final String codigoAcesso;

    public DadosTerritorio(String regiao, int populacaoAtendida, String codigoAcesso)
            throws DadosInvalidosException {
        setRegiao(regiao);
        setPopulacaoAtendida(populacaoAtendida);
        if (codigoAcesso == null || codigoAcesso.isBlank()) {
            throw new DadosInvalidosException("Código de acesso do território não pode ser vazio.");
        }
        this.codigoAcesso = codigoAcesso;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("Região do território não pode ser vazia.");
        }
        this.regiao = regiao.trim();
    }


    public int getPopulacaoAtendida() {
        return populacaoAtendida;
    }

    public void setPopulacaoAtendida(int populacaoAtendida) throws DadosInvalidosException {
        if (populacaoAtendida < 0) {
            throw new DadosInvalidosException("População atendida não pode ser negativa: " + populacaoAtendida);
        }
        this.populacaoAtendida = populacaoAtendida;
    }


    public void alterarLimiares(String codigoInformado, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        if (codigoInformado == null || !codigoAcesso.equals(codigoInformado)) {
            throw new DadosInvalidosException("Código de acesso inválido para alterar limiares do território.");
        }
        ConfiguracaoSistema.getInstancia().atualizarLimiares(atencao, alerta, critico);
    }
}
