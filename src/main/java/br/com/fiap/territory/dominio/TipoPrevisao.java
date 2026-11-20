package br.com.fiap.territory.dominio;

public enum TipoPrevisao {
    MEDIA_MOVEL("Média Móvel"),
    COMPARACAO("Comparação com Período Anterior");

    private final String descricao;

    TipoPrevisao(String descricao) {
        this.descricao = descricao;
    }


    public String getDescricao() {
        return descricao;
    }
}
