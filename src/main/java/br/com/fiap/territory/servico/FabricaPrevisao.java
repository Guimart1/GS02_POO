package br.com.fiap.territory.servico;

import br.com.fiap.territory.dominio.EstrategiaPrevisao;
import br.com.fiap.territory.dominio.PrevisaoComparacao;
import br.com.fiap.territory.dominio.PrevisaoMediaMovel;
import br.com.fiap.territory.dominio.TipoPrevisao;
import br.com.fiap.territory.excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class FabricaPrevisao {

    private static final Map<TipoPrevisao, Supplier<EstrategiaPrevisao>> ESTRATEGIAS =
            new EnumMap<>(TipoPrevisao.class);

    static {
        ESTRATEGIAS.put(TipoPrevisao.MEDIA_MOVEL,
                () -> new PrevisaoMediaMovel(ConfiguracaoSistema.getInstancia().getJanelaMediaMovel()));
        ESTRATEGIAS.put(TipoPrevisao.COMPARACAO, PrevisaoComparacao::new);
    }

    private FabricaPrevisao() {
    }


    public static EstrategiaPrevisao criarEstrategia() throws DadosInvalidosException {
        return criarEstrategia(ConfiguracaoSistema.getInstancia().getTipoPrevisaoAtiva());
    }

    public static EstrategiaPrevisao criarEstrategia(TipoPrevisao tipo) throws DadosInvalidosException {
        if (tipo == null || !ESTRATEGIAS.containsKey(tipo)) {
            throw new DadosInvalidosException("Estratégia de previsão não suportada: " + tipo);
        }
        return ESTRATEGIAS.get(tipo).get();
    }
}
