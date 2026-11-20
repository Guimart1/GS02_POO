package br.com.fiap.territory.dominio;

import br.com.fiap.territory.excecao.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAlerta nivel;

    @Column(nullable = false)
    private String sensorTipo;

    @Column(nullable = false)
    private double valorPrevisto;

    @Column(nullable = false)
    private double limiarExcedido;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Alerta() {
    }


    public Alerta(NivelAlerta nivel, String sensorTipo, double valorPrevisto,
                  double limiarExcedido, String mensagem, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (nivel == null) {
            throw new DadosInvalidosException("Nível do alerta é obrigatório.");
        }
        if (sensorTipo == null || sensorTipo.isBlank()) {
            throw new DadosInvalidosException("Tipo do sensor do alerta é obrigatório.");
        }
        if (valorPrevisto < 0 || limiarExcedido <= 0) {
            throw new DadosInvalidosException("Valores de previsão e limiar do alerta devem ser válidos.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new DadosInvalidosException("Mensagem explicativa do alerta é obrigatória.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Data e hora do alerta são obrigatórias.");
        }
        this.nivel = nivel;
        this.sensorTipo = sensorTipo.trim();
        this.valorPrevisto = valorPrevisto;
        this.limiarExcedido = limiarExcedido;
        this.mensagem = mensagem.trim();
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public String getSensorTipo() {
        return sensorTipo;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiarExcedido() {
        return limiarExcedido;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
