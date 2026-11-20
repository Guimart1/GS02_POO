package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import br.com.fiap.territory.excecao.FonteIndisponivelException;

public interface Sensor {

    double lerValor() throws FonteIndisponivelException, DadosInvalidosException;

    boolean verificarFuncionamento();

    String retornarTipo();

    String getUnidadeMedida();
}
