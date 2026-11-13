Context7 obejmuje tylko stronę checkstyle.org. Osobnej biblioteki z dokumentacją `maven-checkstyle-plugin` (maven.apache.org) w nim nie ma. Dlatego z Context7 potwierdziłem tylko wersje, `configLocation` i polecenie uruchomienia. Pozostałe parametry podaję z własnej wiedzy i nie są sprawdzone w źródle.

## Fragment do `backend/pom.xml` (w `<build><plugins>`)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <configLocation>${project.basedir}/checkstyle.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failOnViolation>true</failOnViolation>
        <failsOnError>false</failsOnError>
        <violationSeverity>warning</violationSeverity>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>com.puppycrawl.tools</groupId>
            <artifactId>checkstyle</artifactId>
            <version>14.3.0</version>
        </dependency>
    </dependencies>
</plugin>
```

Sekcji `<executions>` celowo nie ma, więc plugin nie jest podpięty pod żadną fazę. Konfiguracja leży na poziomie `<plugin>`, a nie w `<execution>`. To ważne, bo polecenie `./mvnw checkstyle:check` używa wykonania `default-cli` i konfiguracji z `<execution>` by nie zobaczyło.

## Parametry

**Biblioteka w Context7: `/websites/checkstyle`** (źródło: checkstyle.org)

- **Wersja pluginu 3.6.0 i `com.puppycrawl.tools:checkstyle` 14.3.0.** Źródło: https://checkstyle.org/getting-started.html
  > „pins the Checkstyle dependency to version 14.3.0, which is the version documented on this site. The Maven plugin has its own release cycle, so the explicit dependency ensures the documented version is used."
- **`configLocation`.** Na tej samej stronie przykład `<configLocation>config/checkstyle/checkstyle.xml</configLocation>` i polecenie:
  > „Run: `./mvnw checkstyle:check`"
- **`failOnViolation`.** Context7 zwrócił tylko odpowiednik z zadania Ant (`CheckstyleAntTask`), nie z pluginu Maven:
  > „failOnViolation … Whether to fail build on violations."

**Spoza Context7, do sprawdzenia na https://maven.apache.org/plugins/maven-checkstyle-plugin/check-mojo.html**

- `consoleOutput` (domyślnie `false`): wypisuje naruszenia na konsolę.
- `failOnViolation` (domyślnie `true`): przerywa build, gdy liczba naruszeń o poziomie ≥ `violationSeverity` jest większa niż `maxAllowedViolations`.
- `failsOnError` (domyślnie `false`): przerywa build już w fazie generowania raportu, przy naruszeniach o poziomie `error`. Do samego `check` wystarczy `failOnViolation`.
- `violationSeverity` (domyślnie `error`): najniższy poziom, który liczy się jako naruszenie. Ustawiłem `warning`. Jeśli reguły w `checkstyle.xml` mają poziom `warning`, przy wartości domyślnej build by nie padał.
- `includeTestSourceDirectory` (domyślnie `false`): sprawdza też `src/test/java`.

## Uwagi

- Spring Boot 3.2.5 przez parent zarządza starszą wersją tego pluginu. Jawne `<version>3.6.0</version>` ją nadpisuje.
- Checkstyle 14.x prawdopodobnie wymaga nowego JDK (21+), ale tego nie sprawdziłem w Context7. Projekt i tak używa JDK 22–26.
- Plugin sprawdza też `legacy/`, więc przy pierwszym uruchomieniu może zgłosić dużo naruszeń. Jeśli tak będzie, wyklucz ten katalog przez `<excludes>` albo dodaj plik tłumienia ostrzeżeń w `<suppressionsLocation>`.