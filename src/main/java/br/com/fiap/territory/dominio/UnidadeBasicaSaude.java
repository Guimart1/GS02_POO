package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;

public class UnidadeBasicaSaude extends ComponenteTerritorial {

    private int capacidadeAtendimentoHora;
    private int equipesAtivas;

    public UnidadeBasicaSaude(String id, String nome, String regiao, String status,
                              int capacidadeAtendimentoHora, int equipesAtivas)
            throws DadosInvalidosException {
        super(id, nome, regiao, status);
        setCapacidadeAtendimentoHora(capacidadeAtendimentoHora);
        setEquipesAtivas(equipesAtivas);
    }

    @Override

    public String verificarStatus() {
        return String.format(
                "[UBS %s] %s (%s) | Status: %s | Capacidade: %d atend./h | Equipes ativas: %d",
                getId(), getNome(), getRegiao(), getStatus(),
                capacidadeAtendimentoHora, equipesAtivas
        );
    }

    public int getCapacidadeAtendimentoHora() {
        return capacidadeAtendimentoHora;
    }

    public void setCapacidadeAtendimentoHora(int capacidadeAtendimentoHora) throws DadosInvalidosException {
        if (capacidadeAtendimentoHora <= 0) {
            throw new DadosInvalidosException("Capacidade de atendimento por hora da UBS deve ser maior que zero.");
        }
        this.capacidadeAtendimentoHora = capacidadeAtendimentoHora;
    }


    public int getEquipesAtivas() {
        return equipesAtivas;
    }

    public void setEquipesAtivas(int equipesAtivas) throws DadosInvalidosException {
        if (equipesAtivas <= 0) {
            throw new DadosInvalidosException("Quantidade de equipes ativas na UBS deve ser positiva.");
        }
        this.equipesAtivas = equipesAtivas;
    }
}
