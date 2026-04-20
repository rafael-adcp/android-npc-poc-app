# Android NFC POC

![CI](https://github.com/rafael-adcp/android-npc-poc-app/actions/workflows/ci.yml/badge.svg?branch=main)
![Coverage](https://img.shields.io/badge/coverage-85%25-brightgreen)

Aplicativo Android nativo (Kotlin + Jetpack Compose) que lê cartões/tags via **NFC**, envia o valor para uma **API REST** e armazena o resultado localmente em um banco **Room (SQLite)**.

> Consulte [PLAN.md](PLAN.md) para o plano técnico detalhado.

---

## Pré-requisitos

| Ferramenta | Versão | Observações |
|------------|--------|-------------|
| **Android Studio** | Ladybug (2024.2+) ou superior | [Download](https://developer.android.com/studio) |
| **JDK** | 17 | Normalmente embutido no Android Studio |
| **Android SDK** | API 35 (Android 15) | Instalado via SDK Manager |
| **Android SDK Build-Tools** | 35.0.0+ | Instalado via SDK Manager |
| **Gradle** | 8.11 | Gerenciado pelo wrapper (`gradlew`) |
| **Dispositivo físico com NFC** | Android 7.0+ (API 24) | Emulador **NÃO** suporta NFC |
| **Tag/cartão NFC** | Qualquer | Mifare, NTAG, cartão de transporte, etc. |

---

## 1. Configurando o Ambiente

### 1.1 Instalar o Android Studio

1. Baixe em https://developer.android.com/studio
2. Execute o instalador e aceite as opções padrão
3. Na primeira abertura, deixe o setup wizard baixar o SDK padrão

### 1.2 Instalar os componentes do SDK

Abra **Android Studio → More Actions → SDK Manager** (ou `File → Settings → Languages & Frameworks → Android SDK`) e em **SDK Platforms** marque:

- [x] Android 15 (API 35)

Em **SDK Tools** marque:

- [x] Android SDK Build-Tools 35
- [x] Android SDK Platform-Tools
- [x] Android SDK Command-line Tools (latest)

Clique em **Apply** para baixar.

### 1.3 Configurar variáveis de ambiente (opcional, mas recomendado)

No Windows (PowerShell como admin):

```powershell
setx ANDROID_HOME "$env:LOCALAPPDATA\Android\Sdk"
setx PATH "$env:PATH;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator"
```

Reabra o terminal e valide:

```bash
adb --version
```

### 1.4 Habilitar modo desenvolvedor no celular

1. Em **Configurações → Sobre o telefone**, toque 7 vezes em **Número da compilação**
2. Volte para **Configurações → Sistema → Opções do desenvolvedor**
3. Habilite **Depuração USB**
4. Garanta que **NFC** esteja ativado em **Configurações → Conexões**

### 1.5 Samsung - desativar o Bloqueador Automático (Auto Blocker)

Em dispositivos Samsung com One UI 6.0+, o **Bloqueador Automático** bloqueia comandos USB por padrão. Se ao ativar a Depuração USB aparecer a mensagem **"bloqueado pelo bloqueador automático"**, faça:

1. **Configurações → Segurança e privacidade → Bloqueador automático** (em versões antigas: **Biometria e segurança → Bloqueador automático**)
2. Desative o toggle principal **OU** apenas a opção **"Bloquear comandos do cabo USB"**
3. Volte em **Opções do desenvolvedor** e ative **Depuração USB**

> O Bloqueador Automático também bloqueia instalação de APKs não autorizados. Se a instalação do app falhar, desative também **"Bloquear instalação de apps não autorizados"**.

---

## 2. Clonando e Abrindo o Projeto

```bash
git clone <URL_DO_REPOSITORIO>
cd android-app-poc
```

No Android Studio:

1. **File → Open** → selecione a pasta `android-app-poc`
2. Aguarde o **Gradle Sync** terminar (primeira vez pode demorar vários minutos ao baixar dependências)
3. Se aparecer prompt para instalar SDK/Build-Tools faltando, aceite

> **Gradle Wrapper**: este repositório inclui `gradle/wrapper/gradle-wrapper.properties` mas não os scripts `gradlew`/`gradlew.bat` nem o `gradle-wrapper.jar` (arquivos binários gerados). Ao abrir pela primeira vez no Android Studio eles são gerados automaticamente. Para gerar manualmente (necessário para rodar a CI sem passar pela IDE), com Gradle instalado na máquina execute uma vez: `gradle wrapper --gradle-version 8.11`.

---

## 3. Configuração da API

A URL base da API fica em [app/build.gradle.kts](app/build.gradle.kts) via `buildConfigField`.

Crie (ou edite) o arquivo `local.properties` na raiz com:

```properties
API_BASE_URL=https://sua-api.exemplo.com/
```

> **Não comite `local.properties`** - já está no `.gitignore`.

Para desenvolvimento sem backend pronto, use um mock (ex.: [https://webhook.site](https://webhook.site) ou [Mockoon](https://mockoon.com/)).

---

## 4. Rodando o Aplicativo

### 4.1 Via Android Studio (recomendado)

1. Conecte o celular via USB
2. Autorize a depuração quando o prompt aparecer no celular
3. Na barra superior do Android Studio, selecione o dispositivo no dropdown
4. Clique no botão **Run ▶** (ou `Shift + F10`)
5. O app será instalado e aberto automaticamente

### 4.2 Via linha de comando

Na raiz do projeto:

```bash
# Build do APK de debug
./gradlew assembleDebug

# Instalar no dispositivo conectado
./gradlew installDebug

# Iniciar a MainActivity
adb shell am start -n com.example.nfcpoc/.MainActivity
```

No Windows use `gradlew.bat` em vez de `./gradlew`.

---

## 5. Como usar o app

1. Abra o aplicativo
2. A tela inicial exibirá **"Aproxime o cartão do celular"**
3. Aproxime uma tag NFC da parte traseira do celular (onde está a antena NFC)
4. O app lerá o valor, chamará a API e mostrará o resultado
5. Navegue para **Histórico** para ver todas as leituras persistidas localmente
6. Na aba **Histórico**, o botão **"Limpar histórico (N)"** apaga todas as leituras do banco local (com diálogo de confirmação)

### 5.1 Painel de debug (somente em builds debug)

Em builds **debug** (`./gradlew assembleDebug` / Run ▶ no Android Studio) um card com a tag `DEBUG` aparece abaixo das abas exibindo:

- **`API:`** URL base da API que será chamada (vem de `BuildConfig.API_BASE_URL`, configurada em `local.properties`)
- **`Último valor lido:`** o conteúdo bruto extraído da última tag NFC (UID hex ou payload NDEF). Aparece assim que a leitura é disparada - útil para depurar tags que não geram resposta esperada da API.

O painel **não é incluído** em builds release (`assembleRelease`) porque a renderização é guardada por `BuildConfig.DEBUG`. Para forçar o comportamento em testes, `AppScaffold` aceita os parâmetros `debugEnabled: Boolean` e `apiBaseUrl: String`.

---

## 6. Onde ficam os dados salvos?

Os dados ficam em um banco **SQLite** gerenciado pelo **Room**, dentro do sandbox do app:

```
/data/data/com.example.nfcpoc/databases/nfc_poc.db
```

Para inspecionar durante o desenvolvimento:

- **Android Studio → View → Tool Windows → App Inspection → Database Inspector**
- Conecte ao processo e visualize/edite as tabelas em tempo real

---

## 7. Comandos úteis

```bash
# Limpar build
./gradlew clean

# Rodar testes unitários
./gradlew test

# Rodar testes instrumentados (precisa de dispositivo)
./gradlew connectedDebugAndroidTest

# Ver dispositivos conectados
adb devices

# Ver logs do app em tempo real
adb logcat -s NfcPoc:V
```

---

## 8. Troubleshooting

| Problema | Solução |
|----------|---------|
| `SDK location not found` | Defina `sdk.dir` em `local.properties` ou a variável `ANDROID_HOME` |
| App não aparece na lista de dispositivos | Verifique depuração USB, tente `adb kill-server && adb start-server` |
| "NFC não disponível" | Ative em Configurações do celular; teste em outro dispositivo |
| Gradle Sync travado | `File → Invalidate Caches → Invalidate and Restart` |
| Erro de JDK | `File → Settings → Build Tools → Gradle` → selecione Embedded JDK 17 |
| Tag não é lida | Confirme que a tag está dentro do filtro NFC e encosta na área da antena |

---

## 9. Estrutura do Projeto

Veja [PLAN.md](PLAN.md#2-estrutura-de-pastas-proposta) para a estrutura completa.

---

## 10. CI - GitHub Actions

O arquivo [.github/workflows/ci.yml](.github/workflows/ci.yml) dispara automaticamente em toda **pull request** (e push em `main`/`master`):

- **`unit-tests`** — roda `./gradlew test lintDebug assembleDebug` em ~5 min.
- **`instrumented-tests`** — sobe um emulador Android API 29 e roda `./gradlew connectedDebugAndroidTest` (testes e2e com HTTP real via MockWebServer e Room real).

Relatórios de teste e lint são publicados como artifacts mesmo em caso de falha.

---

## Licença

POC interna - sem licença pública definida.
