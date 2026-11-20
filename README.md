# UBStatus — Monitoramento Territorial de Demanda por Serviços

**Global Solution 2º/2026 — Programação Orientada a Objetos (FIAP)**  
**Tema:** CitizenTech | **Recorte:** Demanda por serviços públicos (UBS e CRAS)

## Integrantes e Divisão de Tarefas

- **Guilherme Martins dos Santos (RM566570):** Domínio orientado a objetos (`ComponenteTerritorial`, `Sensor`, `DadosTerritorio`, `EstrategiaPrevisao`) e exceções customizadas.
- **Gabriel Rodrigues Barbosa (RM566475):** Persistência com Spring Data JPA + H2 (`Medicao`, `Alerta`, repositórios) e exportação CSV.
- **Matheus Cordeiro Alves (RM563107):** Padrões criacionais (`FabricaPrevisao`, `ConfiguracaoSistema`), `ServicoMonitoramento`, menu de console, catalisador de alertas e testes unitários.

## Público Beneficiado

- **Quem sofre o problema:** População atendida em Unidades Básicas de Saúde (UBS) e Centros de Referência de Assistência Social (CRAS), além das equipes locais expostas a picos de procura e filas longas.
- **Qual decisão o sistema apoia:** Ajuda gestores e coordenadores regionais a identificar tendências de sobrecarga nos próximos períodos para decidir sobre reforço de equipes, abertura de guichês extras ou redirecionamento preventivo de atendimentos.

## Como Rodar e Persistência

- **Pré-requisito:** Java 17+ (o projeto inclui Maven Wrapper `mvnw` / `mvnw.cmd`).
- **Banco de dados:** H2 em arquivo local (`jdbc:h2:file:./data/territory_db`), configurado em `src/main/resources/application.properties`. Os dados sobrevivem ao reiniciar o programa.
- **Executar testes unitários:**
  ```bash
  .\mvnw.cmd test
  ```
- **Executar a aplicação (menu de console):**
  ```bash
  .\mvnw.cmd spring-boot:run
  ```
- **Recursos extras:** Código de acesso padrão para alterar limiares: `GS2026`. O menu inclui a opção **6 (Catalisador de alertas)** para simular situações de risco e **exportação do histórico em CSV** (bônus).

## Decisões da Equipe e Padrões Criacionais

- **Persistência (Spring Boot + JPA + H2):** Escolhida para gerenciar conexões automaticamente, evitar SQL concatenado e manter histórico persistido entre execuções.
- **Factory (`FabricaPrevisao`):** Centraliza a criação da estratégia de previsão (`PrevisaoMediaMovel` ou `PrevisaoComparacao`) via `EnumMap`, evitando `if tipo ==` ou `instanceof` espalhados no serviço e no menu.
- **Singleton (`ConfiguracaoSistema`):** Garante instância única para limiares (`ATENCAO`, `ALERTA`, `CRITICO`) e parâmetros globais compartilhados entre território, fábrica e serviço.

## Reflexão Crítica

- **Quando o sistema deve se calar?** Quando não há histórico suficiente ou a coleta falha. Sem medições no banco, o sistema lança `DadosInvalidosException`; se um sensor estiver offline, lança `FonteIndisponivelException`; e com menos de 5 leituras no histórico, exibe aviso de baixa confiabilidade em vez de assumir certeza.
- **O que os dados não representam?** Os sensores medem apenas fluxo interno, tempo de espera e ocupação de guichês. Não captam demanda reprimida (quem desistiu da fila), sazonalidade de campanhas de vacinação, problemas de transporte no bairro ou a complexidade clínica/social de cada atendimento.
- **Por que o alerta apoia a decisão da pessoa, em vez de decidir por ela?** O alerta indica apenas que um limiar numérico foi ultrapassado. Cabe ao gestor humano avaliar o contexto real da unidade (equipes disponíveis, estoque, prioridades locais) e assumir a decisão operacional.

## Uso de Inteligência Artificial

- **Onde foi usada:** Apoio na formatação de texto e na estruturação do fluxo de como o projeto deveria ser organizado.
- **Como foi revisada:** Todo o código, os testes e o texto do README foram revisados, executados localmente (`.\mvnw.cmd test` e `.\mvnw.cmd spring-boot:run`) e validados pela equipe quanto às regras do domínio e tratamento de exceções.
