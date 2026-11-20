package br.com.fiap.territory.servico;

import br.com.fiap.territory.dominio.TipoPrevisao;
import br.com.fiap.territory.excecao.DadosInvalidosException;

public final class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    private double limiarAtencao;
    private double limiarAlerta;
    private double limiarCritico;
    private TipoPrevisao tipoPrevisaoAtiva;
    private int janelaMediaMovel;
    private int minimoLeiturasConfiaveis;


    private ConfiguracaoSistema() {
        restaurarPadroes();
    }

    public static synchronized ConfiguracaoSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }


    public void restaurarPadroes() {
        this.limiarAtencao = 60.0;
        this.limiarAlerta = 80.0;
        this.limiarCritico = 95.0;
        this.tipoPrevisaoAtiva = TipoPrevisao.MEDIA_MOVEL;
        this.janelaMediaMovel = 5;
        this.minimoLeiturasConfiaveis = 5;
    }


    public void atualizarLimiares(double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (atencao <= 0 || alerta <= 0 || critico <= 0) {
            throw new DadosInvalidosException("Todos os limiares devem ser maiores que zero.");
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(
                    "Ordem de limiares inválida: deve obedecer atenção < alerta < crítico.");
        }
        this.limiarAtencao = atencao;
        this.limiarAlerta = alerta;
        this.limiarCritico = critico;
    }


    public double getLimiarAtencao() {
        return limiarAtencao;
    }

    public double getLimiarAlerta() {
        return limiarAlerta;
    }

    public double getLimiarCritico() {
        return limiarCritico;
    }

    public TipoPrevisao getTipoPrevisaoAtiva() {
        return tipoPrevisaoAtiva;
    }

    public void setTipoPrevisaoAtiva(TipoPrevisao tipoPrevisaoAtiva) throws DadosInvalidosException {
        if (tipoPrevisaoAtiva == null) {
            throw new DadosInvalidosException("Tipo de previsão não pode ser nulo.");
        }
        this.tipoPrevisaoAtiva = tipoPrevisaoAtiva;
    }


    public int getJanelaMediaMovel() {
        return janelaMediaMovel;
    }

    public void setJanelaMediaMovel(int janelaMediaMovel) throws DadosInvalidosException {
        if (janelaMediaMovel <= 0) {
            throw new DadosInvalidosException("Janela da média móvel deve ser maior que zero.");
        }
        this.janelaMediaMovel = janelaMediaMovel;
    }


    public int getMinimoLeiturasConfiaveis() {
        return minimoLeiturasConfiaveis;
    }
}
