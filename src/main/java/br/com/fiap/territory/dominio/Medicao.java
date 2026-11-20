package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "medicoes")
public class Medicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sensorTipo;

    @Column(nullable = false)
    private String componenteId;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private String unidade;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Medicao() {
    }


    public Medicao(String sensorTipo, String componenteId, double valor, String unidade, LocalDateTime dataHora)
            throws DadosInvalidosException {
        setSensorTipo(sensorTipo);
        setComponenteId(componenteId);
        setValor(valor);
        setUnidade(unidade);
        setDataHora(dataHora);
    }


    public Long getId() {
        return id;
    }

    public String getSensorTipo() {
        return sensorTipo;
    }

    public void setSensorTipo(String sensorTipo) throws DadosInvalidosException {
        if (sensorTipo == null || sensorTipo.isBlank()) {
            throw new DadosInvalidosException("Tipo do sensor não pode ser vazio.");
        }
        this.sensorTipo = sensorTipo.trim();
    }


    public String getComponenteId() {
        return componenteId;
    }

    public void setComponenteId(String componenteId) throws DadosInvalidosException {
        if (componenteId == null || componenteId.isBlank()) {
            throw new DadosInvalidosException("ID do componente territorial não pode ser vazio.");
        }
        this.componenteId = componenteId.trim();
    }


    public double getValor() {
        return valor;
    }

    public void setValor(double valor) throws DadosInvalidosException {
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0) {
            throw new DadosInvalidosException("Valor da medição não pode ser negativo ou inválido: " + valor);
        }
        this.valor = valor;
    }


    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) throws DadosInvalidosException {
        if (unidade == null || unidade.isBlank()) {
            throw new DadosInvalidosException("Unidade de medida não pode ser vazia.");
        }
        this.unidade = unidade.trim();
    }


    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) throws DadosInvalidosException {
        if (dataHora == null) {
            throw new DadosInvalidosException("Data e hora da medição são obrigatórias.");
        }
        this.dataHora = dataHora;
    }
}
