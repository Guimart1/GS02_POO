package br.com.fiap.territory.servico;

import br.com.fiap.territory.dominio.Alerta;
import br.com.fiap.territory.dominio.Medicao;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class ExportadorCsv {


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ExportadorCsv() {
    }


    public static Path exportarMedicoes(String sensorTipo, List<Medicao> medicoes) throws IOException {
        Path pasta = Paths.get("exports");
        Files.createDirectories(pasta);
        String nomeArquivo = String.format("medicoes_%s_%s.csv",
                sensorTipo.toLowerCase(Locale.ROOT),
                LocalDateTime.now().format(FILE_FORMATTER));
        Path destino = pasta.resolve(nomeArquivo);

        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            writer.write("id;sensor_tipo;componente_id;valor;unidade;data_hora");
            writer.newLine();
            for (Medicao m : medicoes) {
                writer.write(String.format(Locale.US, "%s;%s;%s;%.2f;%s;%s",
                        m.getId() != null ? m.getId() : "",
                        m.getSensorTipo(),
                        m.getComponenteId(),
                        m.getValor(),
                        m.getUnidade(),
                        m.getDataHora().format(FORMATTER)));
                writer.newLine();
            }
        }
        return destino;
    }


    public static Path exportarAlertas(List<Alerta> alertas) throws IOException {
        Path pasta = Paths.get("exports");
        Files.createDirectories(pasta);
        String nomeArquivo = String.format("alertas_%s.csv",
                LocalDateTime.now().format(FILE_FORMATTER));
        Path destino = pasta.resolve(nomeArquivo);

        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            writer.write("id;nivel;sensor_tipo;valor_previsto;limiar_excedido;mensagem;data_hora");
            writer.newLine();
            for (Alerta a : alertas) {
                String msgSanitizada = a.getMensagem().replace(";", ",");
                writer.write(String.format(Locale.US, "%s;%s;%s;%.2f;%.2f;%s;%s",
                        a.getId() != null ? a.getId() : "",
                        a.getNivel(),
                        a.getSensorTipo(),
                        a.getValorPrevisto(),
                        a.getLimiarExcedido(),
                        msgSanitizada,
                        a.getDataHora().format(FORMATTER)));
                writer.newLine();
            }
        }
        return destino;
    }
}
