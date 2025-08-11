# Qalan UI Tests

Автоматизированные **UI-тесты** для платформы **Qalan**.  
Стек: **Java 21 · Gradle 8.11.1 · JUnit 5 · Selenide 7.5.1 · Rest-Assured · Allure Reports · Telegram**  
CI/CD: **GitLab CI · Docker · GitLab Runner · Selenoid/Selenium Grid**

---

## 1) Что это и как устроено

Эти тесты проверяют веб-интерфейс Qalan. Внутри UI-сценариев мы иногда дергаем API (получаем токен/данные и сверяем с UI).

**Ключевые принципы:**
- Домен (host) **не хардкодим** в коде страниц: открываем только **относительные пути** (`open("/")`, `open("/login")`).
- База URL задается через Gradle-ключ `-Penv` и прокидывается в `TestBase`:
    - `Configuration.baseUrl` — для браузера,
    - `RestAssured.baseURI` + `RestAssured.basePath="/api"` — для вспомогательных API-вызовов.
- Все REST-вызовы тоже относительные: `get("/users/login")`, `get("/personalStudy/kpi")`.

---

## 2) Технологии (кратко и зачем)

| Инструмент | Роль |
|---|---|
| **Java 17** | Язык тестов (LTS, стабильность и скорость). |
| **Gradle 8** | Сборка/запуск тестов, управление зависимостями, переключение окружений `-Penv`. |
| **JUnit 5** | Фреймворк тестирования: `@Test`, `@BeforeEach`, фильтр по `@Tag`. |
| **Selenide 7** | Упрощённая работа с Selenium: умные ожидания, короткие селекторы, авто-скриншоты/логи. |
| **Rest-Assured** | HTTP-клиент для API внутри UI-тестов (получить токен, сверить данные с UI). |
| **Allure** | Отчёты со скриншотами, шагами, вложениями и историей. |
| **GitLab CI + Docker + Runner** | Прогоны в пайплайне, хранение артефактов, отправка уведомлений. |
| **Selenoid / Selenium Grid** | Удалённые браузеры в контейнерах для CI. |
| **Telegram Bot API** | Уведомления о результатах прогонов. |

---

## 3) Структура проекта

QalanWeb/
├─ build.gradle # Gradle-конфиг (env-переключатель, теги-наборы, Allure)
├─ gradlew / gradlew.bat # Gradle Wrapper
├─ notifications/ # (опционально) Allure Notifications jar + конфиг
│ ├─ allure-notifications-4.9.0.jar
│ └─ config.json
├─ src/
│ └─ test/
│ ├─ java/
│ │ ├─ pages/ # Page Object'ы: LoginPage, ChatPage, KpiTablePage, ...
│ │ ├─ testData/ # TestBase, DriverFactory, TokenProvider и прочие хелперы
│ │ └─ tests/ # Тесты: LoginTest, ChatTest, KpiTest, ...
│ └─ resources/ # Ресурсы (если нужны)
└─ build/ # Артефакты сборки (allure-results, allure-report, html-отчёты JUnit)

---

## 4) Окружения (переключатель)

В `build.gradle` уже настроено:

| Ключ Gradle | URL |
|---|---|
| `-Penv=test` | `https://test.qalan.kz` |
| `-Penv=preprod` | `https://preprod.qalan.kz` *(по умолчанию)* |
| `-Penv=prod` | `https://qalan.kz` |

---

## 5) Запуск локально (шаги)

**Требования:** установленны **JDK 17+** и **Google Chrome**.

### 5.1 Все тесты (preprod по умолчанию)
```bash```
```
./gradlew clean test
```

### 5.2 Запуск на другом окружении
```bash```
```
./gradlew clean test -Penv=test
```
```bash```
```
./gradlew clean test -Penv=prod
```

### 5.3 Запуск по тегу (готовые наборы)
Теги в проекте: auth, registration, getCashback, personalTask, chat, kpiService, kpiSbb.

```bash```
```
./gradlew clean test -Penv=test
```
```bash```
```
./gradlew clean test -Penv=prod
```
### 5.4 Один класс / один тест
#### Весь класс
```bash```
```
./gradlew test --tests "tests.ChatTest" -Penv=test
```
#### Конкретный метод
```bash```
```
./gradlew test --tests "tests.ChatTest.ChatFlowPersonalStudy2" -Penv=test
```
### 5.5 Удалённый браузер (Selenoid / Selenium Grid)
#### Пример, если Grid доступен по http://grid:4444/wd/hub
```
JAVA_TOOL_OPTIONS="-Dselenide.remote=http://grid:4444/wd/hub -Dselenide.headless=true" \
./gradlew clean test -Penv=preprod
```

---

## 6) Allure отчёты (локально)

**Результаты: build/allure-results**.

### Посмотреть отчёт:
allure serve build/allure-results

### Сгенерировать статический отчёт:
```
allure generate build/allure-results -o build/allure-report --clean
allure open build/allure-report
```

---

## 7) GitLab CI/CD (для DevOps и QA)

**Переменные GitLab (Settings → CI/CD → Variables)**

### Создать:

* ENV — целевое окружение запуска: test / preprod / prod (пример: preprod)

* TG_BOT_TOKEN — токен Telegram-бота (секрет)

* TG_CHAT_ID — id чата/канала (пример: -1002372063611)

### Что делает pipeline

* Поднимает браузер в контейнере (Selenoid / Standalone Chrome).

* Запускает ./gradlew clean test -Penv=$ENV с прокинутым -Dselenide.remote.

* Сохраняет build/allure-results, генерирует Allure HTML.

* Отправляет Telegram-уведомление со ссылкой на отчёт.

---

## 8) Конфиги для CI/CD
***.gitlab-ci.yml***
```
stages: [test, report, notify]

cache:
  key: "${CI_PROJECT_ID}"
  paths:
    - .gradle/caches/
    - .gradle/wrapper/

ui_tests:
  stage: test
  image: gradle:8.11.1-jdk21
  services:
    # Если Selenoid уже крутится вне pipeline — удалите блок services и укажите его адрес в JAVA_TOOL_OPTIONS
    - name: selenoid/vnc:chrome_137.0
      alias: selenoid
  variables:
    ENV: "${ENV:-preprod}"
    # Адрес удалённого браузера и headless-режим
    JAVA_TOOL_OPTIONS: "-Dselenide.remote=http://selenoid:4444/wd/hub -Dselenide.headless=true"
    _JAVA_OPTIONS: "-Xms256m -Xmx1024m"
  script:
    - ./gradlew clean test -Penv=${ENV}
  artifacts:
    when: always
    expire_in: 14 days
    paths:
      - build/allure-results
      - build/reports/tests/test

allure_report:
  stage: report
  image: "allureframework/allure2:2.29.0"
  needs: ["ui_tests"]
  script:
    - allure generate build/allure-results -o build/allure-report --clean
  artifacts:
    when: always
    expire_in: 30 days
    paths:
      - build/allure-report
  allow_failure: true

pages:
  stage: report
  image: alpine:3.20
  needs: ["allure_report"]
  rules:
    - if: $CI_COMMIT_BRANCH == $CI_DEFAULT_BRANCH
  script:
    - mkdir -p public
    - cp -r build/allure-report/* public/
  artifacts:
    paths:
      - public
    expire_in: 30 days

notify_telegram:
  stage: notify
  image: curlimages/curl:8.8.0
  needs: ["allure_report"]
  variables:
    TG_API: "https://api.telegram.org/bot${TG_BOT_TOKEN}/sendMessage"
    ALLURE_HTML: "${CI_SERVER_URL}/${CI_PROJECT_PATH}/-/jobs/${CI_JOB_ID}/artifacts/file/build/allure-report/index.html"
  script:
    - |
      MSG="*Qalan UI Tests*\nStatus: *${CI_JOB_STATUS}*\nEnv: \`${ENV}\`\nBranch: \`${CI_COMMIT_REF_NAME}\`\nAllure: ${ALLURE_HTML}\nPipeline: ${CI_PIPELINE_URL}"
      curl -sS -X POST "${TG_API}" \
        -H 'Content-Type: application/json' \
        -d "{\"chat_id\":\"${TG_CHAT_ID}\",\"text\":\"${MSG}\",\"parse_mode\":\"Markdown\"}"
  when: always
```

---

***browsers.json (Selenoid)***
```
{
  "chrome": {
    "default": "137.0",
    "versions": {
      "137.0": {
        "image": "selenoid/vnc:chrome_137.0",
        "port": "4444",
        "path": "/",
        "tmpfs": {"/tmp": "size=512m"},
        "env": ["TZ=Asia/Almaty"]
      }
    }
  }
}
```

---

***Dockerfile (self-hosted образ для запуска тестов)***
```
FROM selenoid/vnc:chrome_137.0

USER root

# Java + Allure CLI
RUN apt-get update && apt-get install -y openjdk-21-jdk unzip curl && \
    curl -o allure-2.29.0.zip -L https://github.com/allure-framework/allure2/releases/download/2.29.0/allure-2.29.0.zip && \
    unzip allure-2.29.0.zip -d /opt && \
    ln -s /opt/allure-2.29.0/bin/allure /usr/bin/allure && \
    rm allure-2.29.0.zip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

USER selenoid
```

---

***notifications/config.json (для allure-notifications jar — опционально)***
```
{
  "base": {
    "project": "Qalan UI Tests",
    "environment": "${ENV}",
    "comment": "@tshodorov"
    "reportLink": "${ALLURE_LINK}",
    "language": "ru"
  },
  "telegram": {
    "token": "${TG_BOT_TOKEN}",
    "chat": "${TG_CHAT_ID}",
    "templatePath": "telegram.ftl"
  }
}
```
***Job для запуска allure-notifications (если вместо notify_telegram)***
```
notify_allure_jar:
  stage: notify
  image: eclipse-temurin:21-jre
  needs: ["allure_report"]
  variables:
    ALLURE_LINK: "${CI_SERVER_URL}/${CI_PROJECT_PATH}/-/jobs/${CI_JOB_ID}/artifacts/file/build/allure-report/index.html"
  script:
    - |
      sed -i "s|\${ENV}|${ENV:-preprod}|g; s|\${TG_BOT_TOKEN}|${TG_BOT_TOKEN}|g; s|\${TG_CHAT_ID}|${TG_CHAT_ID}|g; s|\${ALLURE_LINK}|${ALLURE_LINK}|g" notifications/config.json
      java -jar notifications/allure-notifications-4.9.0.jar \
        --config notifications/config.json \
        --reportFolder build/allure-report
  when: always
```

---

## 9) Частые проблемы и решения
* MalformedURLException — путь без ведущего / или хардкод домена. Используйте open("/"), get("/endpoint"). Домен приходит через -Penv.

* Браузер не стартует в CI — проверьте доступность Selenoid/Selenium и JVM-флаги: -Dselenide.remote=..., -Dselenide.headless=true.

* Allure пустой — нет build/allure-results после теста. Проверь падения и конфигурацию.

* Нет уведомления в Telegram — проверь TG_BOT_TOKEN, TG_CHAT_ID и логи job notify_telegram.

---

## 10) Шпаргалка команд 
| Что сделать           | Команда                                                                |
| --------------------- | ---------------------------------------------------------------------- |
| Все тесты (preprod)   | `./gradlew clean test`                                                 |
| Все тесты (test/prod) | `./gradlew clean test -Penv=test` / `-Penv=prod`                       |
| По тегу               | `./gradlew chatTest -Penv=preprod`                                     |
| Один класс            | `./gradlew test --tests "tests.LoginTest" -Penv=test`                  |
| Один метод            | `./gradlew test --tests "tests.LoginTest.testSuccessLogin" -Penv=test` |
| Allure (serve)        | `allure serve build/allure-results`                                    |
| Allure (статич.)      | `allure generate build/allure-results -o build/allure-report --clean`  |
