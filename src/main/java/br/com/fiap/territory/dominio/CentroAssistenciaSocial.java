package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

public class CentroAssistenciaSocial extends ComponenteTerritorial {

    private int guichesDisponiveis;
    private int capacidadeDiaria;

    public CentroAssistenciaSocial(String id, String nome, String regiao, String status,
                                   int guichesDisponiveis, int capacidadeDiaria)
            throws DadosInvalidosException {
        super(id, nome, regiao, status);
        setGuichesDisponiveis(guichesDisponiveis);
        setCapacidadeDiaria(capacidadeDiaria);
    }

    @Override

    public String verificarStatus() {
        return String.format(
                "[CRAS %s] %s (%s) | Status: %s | Guichês: %d | Capacidade diária: %d atendimentos",
                getId(), getNome(), getRegiao(), getStatus(),
                guichesDisponiveis, capacidadeDiaria
        );
    }

    public int getGuichesDisponiveis() {
        return guichesDisponiveis;
    }

    public void setGuichesDisponiveis(int guichesDisponiveis) throws DadosInvalidosException {
        if (guichesDisponiveis <= 0) {
            throw new DadosInvalidosException("Quantidade de guichês disponíveis no CRAS deve ser maior que zero.");
        }
        this.guichesDisponiveis = guichesDisponiveis;
    }


    public int getCapacidadeDiaria() {
        return capacidadeDiaria;
    }

    public void setCapacidadeDiaria(int capacidadeDiaria) throws DadosInvalidosException {
        if (capacidadeDiaria <= 0) {
            throw new DadosInvalidosException("Capacidade diária do CRAS deve ser maior que zero.");
        }
        this.capacidadeDiaria = capacidadeDiaria;
    }
}
