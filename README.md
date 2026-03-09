# reab.LITA Tablet (aplicativo para Android)

Aplicativo Android para **triagem auditiva pediátrica** desenvolvido para a Rede reabLITA, voltado ao uso em tablets de 10 polegadas por facilitadores treinados. Integra os módulos **aud.IT** (triagem pediátrica) e **Ouvir Brasil** (triagem rápida +18).

---

## Índice

- [Visão Geral](#visão-geral)
- [Funcionalidades](#funcionalidades)
- [Fluxo de Telas](#fluxo-de-telas)
- [Arquitetura e Tecnologias](#arquitetura-e-tecnologias)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Banco de Dados](#banco-de-dados)
- [Internacionalização](#internacionalização)
- [Configuração do Ambiente](#configuração-do-ambiente)
- [Compilação e Instalação](#compilação-e-instalação)
- [Credenciais de Teste](#credenciais-de-teste)
- [Geração do APK Release](#geração-do-apk-release)
- [Requisitos do Dispositivo](#requisitos-do-dispositivo)
- [Integrações Externas](#integrações-externas)
- [Roadmap](#roadmap--em-desenvolvimento)
- [Licença](#licença)

---

## Visão Geral

O **reab.LITA Tablet** é uma plataforma mobile de acesso restrito a profissionais autorizados para aplicação de testes audiométricos em ambiente escolar e clínico. O aplicativo opera em conformidade com a **LGPD**, garantindo sigilo e segurança no tratamento dos dados dos participantes.

| Atributo | Valor |
|---|---|
| **Plataforma** | Android 7.0+ (API 24+) |
| **Orientação** | Portrait (retrato) exclusivo |
| **Dispositivo alvo** | Tablet 10 polegadas |
| **Versão** | 1.0 (versionCode 1) |
| **Package** | `com.audiometry.threshold` |
| **Idiomas** | Português, English, Español |

---

## Funcionalidades

### Autenticação e Acesso
- Login com e-mail e senha com validação de credenciais
- Seleção de idioma na tela de login (PT-BR / EN / ES)
- Solicitação de acesso para novos facilitadores
- Auto-login via `SharedPreferences` quando sessão já autenticada
- Logout com limpeza completa da sessão

### Triagem Auditiva Pediátrica — aud.IT
- Painel completo com lista de participantes
- Busca de participante em tempo real (debounce 300 ms)
- Filtros por: Status auditivo, Necessita reavaliação, Teste realizado
- Paginação de 10 itens por página
- Seleção múltipla de participantes para iniciar teste em lote
- Modal "Selecionar participantes" (92 × 90% da tela) com overlay

### Cadastro de Participante
- Formulário completo com dados de identificação, contatos, dados profissionais e endereço
- **Validação de CPF** via dígito verificador
- **Busca automática de CEP** pela API [ViaCEP](https://viacep.com.br/) com preenchimento automático de logradouro, bairro, cidade e estado
- Máscara de campos: CPF (000.000.000-00), CEP (00000-000), Telefone

### Verificação de Ruído Ambiente
- Medição em tempo real do nível de ruído (dB) via microfone
- Indicação visual por velocímetro animado (`SpeedometerView`)
- Aviso quando ruído ultrapassa 30 dB NA

### Instruções ao Facilitador
- Lista de verificação básica pré-teste
- Principais dúvidas e orientações
- Responsabilidades do facilitador — seções expansíveis (accordion)

### Barra Lateral de Opções
- Exibição do nome e perfil do usuário autenticado
- Navegação para: Instruções, Checar ruído, Gerenciar participantes
- Itens em desenvolvimento: Protocolo de teste, Calibração, Gerenciar facilitadores, Editar perfil
- Largura responsiva: 42% da largura da tela

### Diálogos e Modais

| Diálogo | Gatilho |
|---|---|
| Selecionar participantes | Botão "Iniciar teste" (aud.IT) |
| Confirmar participantes | Botão "Iniciar teste" no modal de seleção |
| Cancelar alterações | Botão "Cancelar teste" na confirmação |
| Cancelar cadastro | Botão "Cancelar" no cadastro de participante |

---

## Fluxo de Telas

```
LoginActivity
│
├── [Credenciais válidas] ──► HomeActivity
│                               │
│   ┌────────────────────────────┴──────────────────────────────┐
│   │                                                            │
│   ▼                                                            ▼
│ [Painel Ouvir Brasil]                               [Painel aud.IT]
│ (em desenvolvimento)                                PainelAuditActivity
│                                                          │
│                          ┌───────────────────────────────┼─────────────────────┐
│                          │                               │                     │
│                          ▼                               ▼                     ▼
│                 InstrucoesActivity           ChecarRuidoActivity   CadastroParticipanteActivity
│                                                                                │
│                                              ┌─────────────────────────────────┘
│                                              │
│                                    [Modal: Selecionar participantes]
│                                              │
│                                    [Modal: Confirmar participantes]
│                                              │
│                                    [Dialog: Cancelar alterações]
│
├── [Solicitar acesso] ──► SolicitarAcessoActivity
│
└── [☰ Barra Lateral] ──► SideMenuDialog
                            ├── InstrucoesActivity
                            ├── ChecarRuidoActivity
                            ├── PainelAuditActivity (Gerenciar participantes)
                            └── LoginActivity (Sair)
```

---

## Arquitetura e Tecnologias

### Padrão Arquitetural
- **MVVM simplificado** com `LifecycleScope` e `Coroutines` para operações assíncronas
- `BaseActivity` como superclasse com gerenciamento centralizado de idioma e barra lateral

### Stack Tecnológico

| Componente | Tecnologia | Versão |
|---|---|---|
| Linguagem | Kotlin | 1.9.x |
| UI | Android Views + XML Layouts | — |
| Material Design | Material Components | 1.11.0 |
| Banco de dados local | Room (SQLite) | 2.6.1 |
| Requisições HTTP | `HttpURLConnection` (nativo) | — |
| Gráficos | MPAndroidChart | 3.1.0 |
| Coroutines | Kotlinx Coroutines | 1.7.3 |
| Lifecycle | AndroidX Lifecycle | 2.7.0 |
| Build system | Gradle | 8.9 |
| Min SDK | Android 7.0 | API 24 |
| Target SDK | Android 14 | API 34 |
| Java | JVM Target | 17 |

### Dependências Principais

```gradle
// UI e Material
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.recyclerview:recyclerview:1.3.2'

// Room Database
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// Lifecycle + Coroutines
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

// Gráficos audiométricos
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```

---

## Estrutura do Projeto

```
app/
├── src/main/
│   ├── java/com/audiometry/threshold/
│   │   ├── BaseActivity.kt                   # Superclasse: idioma, barra lateral
│   │   ├── LoginActivity.kt                  # Autenticação e seleção de idioma
│   │   ├── HomeActivity.kt                   # Tela inicial com cards de módulos
│   │   ├── PainelAuditActivity.kt            # Painel aud.IT: lista e seleção
│   │   ├── CadastroParticipanteActivity.kt   # Formulário de cadastro
│   │   ├── InstrucoesActivity.kt             # Instruções ao facilitador
│   │   ├── ChecarRuidoActivity.kt            # Medição de ruído ambiente
│   │   ├── SolicitarAcessoActivity.kt        # Formulário de solicitação de acesso
│   │   ├── MainActivity.kt                   # Exame audiométrico (legado, paisagem)
│   │   ├── SideMenuDialog.kt                 # Diálogo da barra lateral
│   │   ├── SelectParticipantsDialog.kt       # Modal seleção de participantes
│   │   ├── CpfValidator.kt                   # Validação de CPF (dígito verificador)
│   │   ├── CpfMaskWatcher.kt                 # Máscara de campo CPF
│   │   ├── ViaCepService.kt                  # Integração API ViaCEP
│   │   ├── SpeedometerView.kt                # View customizada: velocímetro dB
│   │   ├── ChartView.kt                      # View customizada: gráfico audiométrico
│   │   ├── AudioToneGenerator.kt             # Gerador de tons audiométricos
│   │   ├── ThresholdExam.kt                  # Lógica Hughson-Westlake (exame)
│   │   ├── adapter/
│   │   │   ├── ParticipanteAdapter.kt        # Adapter RecyclerView: lista de participantes
│   │   │   └── ConfirmParticipanteAdapter.kt # Adapter: confirmação do teste
│   │   ├── database/
│   │   │   ├── AppDatabase.kt                # Singleton Room Database
│   │   │   ├── SampleDataHelper.kt           # Dados de exemplo (seed)
│   │   │   ├── entity/
│   │   │   │   ├── Participant.kt            # Entidade: participante
│   │   │   │   └── User.kt                   # Entidade: usuário/facilitador
│   │   │   └── dao/
│   │   │       ├── ParticipantDao.kt         # DAO: operações de participante
│   │   │       └── UserDao.kt                # DAO: operações de usuário
│   │   └── models/
│   │       └── ExamData.kt                   # Modelo de dados do exame
│   │
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_home.xml
│   │   │   ├── activity_painel_audit.xml
│   │   │   ├── activity_cadastro_participante.xml
│   │   │   ├── activity_instrucoes.xml
│   │   │   ├── activity_checar_ruido.xml
│   │   │   ├── activity_solicitar_acesso.xml
│   │   │   ├── activity_main.xml
│   │   │   ├── dialog_side_menu.xml
│   │   │   ├── dialog_select_participants.xml
│   │   │   ├── dialog_confirm_participants.xml
│   │   │   ├── dialog_warning.xml
│   │   │   ├── item_participante.xml
│   │   │   ├── item_participante_confirm.xml
│   │   │   └── item_log.xml
│   │   │
│   │   ├── values/              # Strings PT-BR (padrão)
│   │   ├── values-en/           # Strings EN
│   │   ├── values-es/           # Strings ES
│   │   ├── values-sw600dp/      # Dimensões para tablets >= 600dp
│   │   └── values-sw720dp/      # Dimensões para tablets >= 720dp
│   │
│   └── AndroidManifest.xml
│
├── build.gradle
└── reablita.jks                  # Keystore de assinatura (não versionado — ver .gitignore)
```

---

## Banco de Dados

O aplicativo utiliza **Room (SQLite)** com banco local no dispositivo. Os dados são populados automaticamente na primeira execução via `SampleDataHelper`.

### Entidades

#### `User` — Usuários / Facilitadores

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Int (PK, auto) | Identificador único |
| `email` | String | E-mail de acesso (único) |
| `password` | String | Senha |
| `name` | String | Nome completo |
| `role` | String | Perfil: Administrador(a), Facilitador(a) |

#### `Participant` — Participantes

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Int (PK, auto) | Identificador único |
| `cpf` | String | CPF (000.000.000-00) |
| `nomeCompleto` | String | Nome completo |
| `dataNascimento` | String | Data de nascimento (dd/MM/yyyy) |
| `nomeMae` | String | Nome da mãe |
| `nomePai` | String | Nome do pai (opcional) |
| `sexoBiologico` | String | Masculino / Feminino / Outro |
| `identidadeGenero` | String | Identidade de gênero (opcional) |
| `racaCor` | String | Raça/Cor autodeclarada |
| `nomeSocial` | String | Nome social (opcional) |
| `email` | String | E-mail pessoal (opcional) |
| `telefone` | String | Telefone com DDD |
| `cep` | String | CEP do endereço |
| `estado` | String | Estado (UF) |
| `cidade` | String | Município |
| `bairro` | String | Bairro |
| `logradouro` | String | Logradouro |
| `numero` | String | Número |
| `complemento` | String | Complemento (opcional) |
| `instituicao` | String | Escola / Instituição |
| `professor` | String | Professor(a) responsável |
| `queixaAuditiva` | String | Sim / Não |
| `detalhesQueixa` | String | Detalhes da queixa auditiva |
| `observacoes` | String | Outras observações |
| `statusAuditivo` | String | Normal / Alterado |
| `testeRealizado` | String | Aud.IT / Ouvir Brasil |
| `dataUltimoTeste` | String | Data do último exame |
| `necessitaReavaliacao` | String | Sim / Não |

### Dados de Exemplo (Seed)

O `SampleDataHelper` popula automaticamente na primeira execução:
- **1 usuário administrador** (credenciais de teste abaixo)
- **20 participantes** com dados fictícios variados (status normal/alterado, diferentes programas de teste)

---

## Internacionalização

O aplicativo suporta **3 idiomas** com troca dinâmica em tempo de execução, sem reinicialização do app:

| Código | Idioma | Arquivo |
|---|---|---|
| `pt` | Português (padrão) | `res/values/strings.xml` |
| `en` | English | `res/values-en/strings.xml` |
| `es` | Español | `res/values-es/strings.xml` |

### Como Funciona

1. O idioma selecionado é salvo em `SharedPreferences` (chave `"language"`)
2. `BaseActivity.attachBaseContext()` aplica o locale antes da inflação do layout
3. `BaseActivity.onResume()` detecta mudança de idioma na back stack e recria a Activity
4. A seleção persiste entre sessões

### Adicionar Novo Idioma

1. Criar pasta `res/values-XX/` (ex: `values-fr` para francês)
2. Copiar `res/values/strings.xml` para a nova pasta e traduzir todos os valores
3. Adicionar o botão de seleção no layout `activity_login.xml`
4. Tratar o novo código de idioma em `LoginActivity`

---

## Configuração do Ambiente

### Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) ou superior |
| JDK | 17 (incluído no Android Studio — JBR) |
| Android SDK | API 34 (Android 14) |
| Gradle | 8.9 (via wrapper) |

### 1. Clonar o Repositório

```bash
git clone https://github.com/garcezv/reabLITA-Tablet.git
cd reabLITA-Tablet
```

### 2. Abrir no Android Studio

```
File → Open → selecionar a pasta do projeto
```

Aguarde a sincronização do Gradle e o download automático das dependências.

### 3. Configurar o JDK (se necessário)

```
File → Project Structure → SDK Location → Gradle Settings
→ Gradle JDK: selecionar JDK 17 (Android Studio bundled JBR)
```

### 4. Criar Dispositivo Virtual (AVD)

Para tablets de 10 polegadas:
```
Tools → Device Manager → Create Device
→ Categoria: Tablet → Pixel Tablet (ou 10.1" WXGA)
→ System Image: API 34 (Google Play Store)
→ Orientation: Portrait
```

---

## Compilação e Instalação

### Via Android Studio

```
Build → Make Project        (compilar)
Run   → Run 'app'           (compilar e instalar no emulador/dispositivo)
```

### Via Linha de Comando (Windows PowerShell)

```powershell
# Configurar JDK do Android Studio
$env:JAVA_HOME = "C:\Android\Android Studio\jbr"

# Caminho do Gradle (após sincronização inicial)
$gradle = (Resolve-Path "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.9-bin\*\gradle-8.9\bin\gradle.bat")

# Entrar na pasta do projeto
cd "caminho\para\reabLITA-Tablet"

# Compilar APK debug (desenvolvimento)
& $gradle assembleDebug

# Compilar APK release (distribuição)
& $gradle clean assembleRelease
```

**Localização dos APKs gerados:**

```
app/build/outputs/apk/debug/app-debug.apk       ← instalação via adb
app/build/outputs/apk/release/app-release.apk   ← distribuição assinada
```

### Instalar via ADB

```powershell
# Verificar dispositivos conectados
adb devices

# Instalar APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Iniciar o aplicativo
adb shell am start -n "com.audiometry.threshold/.LoginActivity"
```

---

## Credenciais de Teste

Criadas automaticamente pelo `SampleDataHelper` na primeira execução:

| Campo | Valor |
|---|---|
| **E-mail** | `admin@reablita.org` |
| **Senha** | `Admin@123` |
| **Perfil** | Administrador(a) |

> **Atenção:** Estas credenciais são apenas para desenvolvimento e testes locais. Em produção, o gerenciamento de usuários deve ser feito via backend autenticado.

---

## Geração do APK Release

### 1. Criar o Keystore (primeira vez)

O arquivo `app/reablita.jks` **não é versionado** (`.gitignore`). Para recriar:

```powershell
& "C:\Android\Android Studio\jbr\bin\keytool.exe" `
    -genkeypair -v `
    -keystore "app\reablita.jks" `
    -alias reablita `
    -keyalg RSA -keysize 2048 -validity 10000 `
    -storepass "SuaSenhaForte" -keypass "SuaSenhaForte" `
    -dname "CN=reab.LITA, O=Rede reabLITA, C=BR"
```

### 2. Configuração de Assinatura em `app/build.gradle`

```gradle
signingConfigs {
    release {
        storeFile file('reablita.jks')
        storePassword 'SuaSenhaForte'
        keyAlias 'reablita'
        keyPassword 'SuaSenhaForte'
    }
}

buildTypes {
    release {
        minifyEnabled false
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}
```

### 3. Gerar e Verificar o APK

```powershell
& $gradle clean assembleRelease

# Verificar assinatura
& "C:\Android\Android Studio\jbr\bin\keytool.exe" -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

---

## Requisitos do Dispositivo

| Requisito | Especificação |
|---|---|
| **Android** | 7.0 (Nougat) ou superior — API 24+ |
| **Tela** | Tablet 10 polegadas, resolução mínima 1280 × 800 |
| **Orientação** | Retrato (Portrait) exclusivo |
| **RAM** | 2 GB mínimo recomendado |
| **Armazenamento** | ~15 MB (app) + dados do banco local |
| **Microfone** | Necessário para medição de ruído ambiente |
| **Internet** | Necessária para busca de CEP (ViaCEP) |
| **Permissões** | `MODIFY_AUDIO_SETTINGS`, `INTERNET` |

### Breakpoints de Layout (Smallest Width)

| Qualificador | Dispositivo alvo |
|---|---|
| `sw600dp` | Tablets a partir de 7 polegadas |
| `sw720dp` | Tablets a partir de 10 polegadas |

---

## Integrações Externas

### API ViaCEP

Busca automática de endereço pelo CEP no cadastro de participante.

- **Endpoint:** `https://viacep.com.br/ws/{CEP}/json/`
- **Método:** GET (sem autenticação)
- **Implementação:** `ViaCepService.kt` via `HttpURLConnection`
- **Permissão:** `android.permission.INTERNET`

**Exemplo de resposta:**
```json
{
  "cep": "01310-100",
  "logradouro": "Avenida Paulista",
  "bairro": "Bela Vista",
  "localidade": "São Paulo",
  "uf": "SP",
  "erro": false
}
```

---

## Validação de CPF

Implementada em `CpfValidator.kt` com verificação completa de **dígitos verificadores**:

1. Rejeita sequências repetidas (ex: `111.111.111-11`)
2. Calcula o 1º dígito verificador (módulo 11)
3. Calcula o 2º dígito verificador (módulo 11)
4. Exibe mensagem de erro inline no campo `TextInputLayout` se inválido

---

## Permissões Android

```xml
<!-- Necessária para geração de tons audiométricos e medição de ruído -->
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- Necessária para busca de CEP (ViaCEP) -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Convenções do Projeto

- **Nomenclatura:** `camelCase` para variáveis/funções, `PascalCase` para classes
- **Activities** herdam de `BaseActivity` (gerenciamento de idioma e menu lateral)
- **Diálogos modais** herdam de `Dialog` com `private val activity: Activity` no construtor — não usar `Context` genérico para garantir `startActivity()` funcional após `dismiss()`
- **Banco de dados** acessado via `Coroutines` em `Dispatchers.IO`
- **IDs de layout:** prefixo por tipo (`btn_`, `tv_`, `et_`, `ll_`, `rv_`, `til_`)
- **Strings** externalizadas nos 3 idiomas — sem strings hardcoded na UI

---

## Roadmap / Em Desenvolvimento

- [ ] Módulo **Ouvir Brasil** (triagem rápida +18)
- [ ] **Protocolo de teste** configurável por frequência e intensidade
- [ ] **Calibração** de dispositivo de áudio
- [ ] **Gerenciar facilitadores** (CRUD completo)
- [ ] **Editar perfil** do usuário logado
- [ ] Autenticação via **backend** (substituir banco local)
- [ ] **Sincronização** de dados com servidor remoto
- [ ] Exportação de resultados em **PDF / CSV**
- [ ] Login via **GOV.BR**
- [ ] Recuperação de senha por e-mail

---

## Licença

Este projeto foi desenvolvido para a **Rede reabLITA** — uso restrito a fins de pesquisa e triagem auditiva.

```
Copyright © 2026 Rede reab.LITA. Todos os direitos reservados.
Hospital Universitário Onofre Lopes (HUOL) — Universidade Federal do Rio Grande do Norte (UFRN)
Desenvolvedor: Alexandre Garcez Vieira — UNESP
```

---

*Desenvolvido com Android Studio + Kotlin para a Rede reabLITA.*
