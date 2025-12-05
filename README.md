# Тестовое задание для NordCodes (AQA Engineer)

## Описание
Автоматизированные тесты для Spring Boot приложения.

## Технологии
- Java 17
- JUnit 5
- WireMock
- Allure Report
- Apache HttpClient
- Maven

## Как запустить

### 1. Скачайте тестируемое приложение
- Ссылка: https://gametests.nyc.wf/aga.7z
- Пароль: g7%Kp9#rX2bl
- Распакуйте архив

### 2. Запустите приложение
\`\`\`bash
cd /путь/к/распакованному/архиву
java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
\`\`\`

### 3. Запустите тесты (в другом терминале)
\`\`\`bash
mvn clean test
\`\`\`

### 4. Просмотрите отчёт Allure
\`\`\`bash
mvn allure:serve
\`\`\`

## Структура проекта
- \`src/test/java/\` — тестовые классы
- \`pom.xml\` — зависимости Maven
- \`.gitignore\` — исключённые файлы

## Контакты
Фёдор Вянцкус
- Email: vyanckus@mail.ru
- GitHub: vyanckus