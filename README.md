# Pesquisa de Limiar Auditivo - Aplicativo Android

Aplicativo Android para pesquisa de limiar auditivo baseado no método Hughson–Westlake modificado, com ISI aleatório (1000-3000ms) e janela de resposta ajustada para evitar confusão.

## Características

- **Método**: Hughson–Westlake modificado
- **ISI**: Aleatório entre 1000-3000ms a cada apresentação
- **Janela de resposta**: Ajustada automaticamente para terminar antes do próximo tom
- **Critério de parada**: 2 respostas corretas em 3 apresentações no mesmo nível (upTrack)
- **Regra de intensidade**: Descida de 10dB quando ouvido, subida de 5dB quando não ouvido

## Parâmetros Configuráveis

- Frequência (Hz): 500, 1000, 2000, 4000, 8000
- Intensidade inicial (dB HL): -10 a 90
- Janela máxima de resposta (ms)
- Margem de segurança (ms)
- Duração do tom (ms)
- Passos de descida e subida (dB)
- Volume geral (0-100)

## Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/audiometry/threshold/
│   │   ├── MainActivity.kt          # Activity principal
│   │   ├── ThresholdExam.kt         # Lógica do exame
│   │   ├── AudioToneGenerator.kt    # Geração de tons
│   │   ├── ChartView.kt             # Visualização gráfica
│   │   ├── LogAdapter.kt            # Adapter para log
│   │   └── DataModels.kt            # Modelos de dados
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml    # Layout principal
│   │   │   └── item_log.xml         # Item do log
│   │   ├── values/
│   │   │   ├── colors.xml
│   │   │   ├── strings.xml
│   │   │   └── styles.xml
│   │   └── drawable/                # Backgrounds e shapes
│   └── AndroidManifest.xml
└── build.gradle

## Requisitos

- Android SDK 24+ (Android 7.0 Nougat ou superior)
- Kotlin 1.9.20
- Gradle 8.2

## Como Compilar

1. Abra o projeto no Android Studio
2. Aguarde a sincronização do Gradle
3. Conecte um dispositivo Android ou inicie um emulador
4. Clique em "Run" ou pressione Shift+F10

## Como Usar

1. Configure os parâmetros do exame (frequência, intensidade inicial, etc.)
2. Pressione "Iniciar" para começar o exame
3. Durante a apresentação do tom, pressione "OUVI" se você ouvir o som
4. A janela de resposta fecha automaticamente após o tempo configurado
5. O exame termina automaticamente quando o critério de 2/3 é atingido
6. Visualize o gráfico e o log completo do exame

## Observações

- Este aplicativo é didático e não substitui equipamentos calibrados para audiometria clínica
- A conversão dB HL → amplitude é simplificada e não calibrada
- Para uso clínico, é necessária calibração profissional e certificação do equipamento

## Licença

Este projeto é fornecido como exemplo educacional.
```
