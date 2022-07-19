package ar.com.benjamd.junit5app.ejemplo.models;

import ar.com.benjamd.junit5app.ejemplo.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//Ciclo de vida instancia por clase. rompe el concepto de test unitario
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaTest {

    Cuenta cuenta;
    TestInfo testInfo;
    TestReporter testReporter;
    @BeforeEach
    void methodTest(TestInfo testInfo, TestReporter testReporter){
        this.testInfo     = testInfo;
        this.testReporter = testReporter;
        this.cuenta       = new Cuenta("Benjamin", new BigDecimal("10332.221"));
        System.out.println("Iniciando el metodo");
       //System.out.println("ejecutando: " + testInfo.getDisplayName() + "con metodo: " + testInfo.getTestMethod() + " y etiquetas: " + testInfo.getTags());
       testReporter.publishEntry("ejecutando: " + testInfo.getDisplayName() + "con metodo: " + testInfo.getTestMethod() + " y etiquetas: " + testInfo.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el metodo");
    }

    @BeforeAll
    static void beforeAll() {
//     void beforeAll() {
        System.out.println("Inicializando el test");
    }

    @AfterAll
    static void afterAll() {
//    void afterAll() {
        System.out.println("Finalizando el test");
    }

    @Tag("Cuenta")
    @Nested
    @DisplayName("probando atributos de Cuenta")
    class CuentaNombreYSaldoTest {
        @Test
        @DisplayName("Probando nombre de la cuenta")
        void nombreCuentaTest() {

            testReporter.publishEntry(testInfo.getTags().toString());
            if(testInfo.getTags().contains("Cuenta")){
                testReporter.publishEntry("Este test tiene el tag [Cuenta]");

            }

//        cuenta.setPersona("Benjamin");
            String expected = "Benjamin";
            String actual = cuenta.getPersona();
            assertNotNull(actual, () -> "la cuenta no puede ser nula");
            assertEquals(expected, actual, () -> "el nombre de la cuenta no es el esperado " + expected
                    + "sin embargo fue " + actual);
            assertTrue(actual.equals("Benjamin"), () -> "nombre de cuenta debe ser igual a la real");
        }

        @Test
        @DisplayName("Probando saldo de la cuenta")
        void saldoCuentaTest() {

            assertNotNull(cuenta.getSaldo());
            assertEquals(10332.221, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

        }

        @Test
        @DisplayName("Probando referencias de la cuenta")
        void referenciaCuentaTest() {
            cuenta = new Cuenta("juan doe", new BigDecimal("1123.09991"));
            Cuenta cuenta2 = new Cuenta("juan doe", new BigDecimal("1123.09991"));

//        assertNotEquals(cuenta2, cuenta);
            assertEquals(cuenta2, cuenta);
        }

    }

    @Nested
    class CuentaOperacionesTest {
        @Tag("Cuenta")
        @Test
        @DisplayName("Probando debito de la cuenta")
        void debitoCuentaTest() {
            cuenta = new Cuenta("Pedro", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal("100"));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("Cuenta")
        @Test
        @DisplayName("Probando credito de la cuenta")
        void creditoCuentaTest() {
            Cuenta cuenta = new Cuenta("Pedro", new BigDecimal("1000.12345"));
            cuenta.credito(new BigDecimal("100"));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }


        @Test
        @DisplayName("prueba dinero insuficiente")
        void dineroInsuficienteTest() {
            cuenta = new Cuenta("Pedro", new BigDecimal("1000.12345"));
            Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
                cuenta.debito(new BigDecimal(1500));
            });

            String actual = exception.getMessage();
            String expected = "Dinero Insuficiente";
            assertEquals(expected, actual);
        }

        @Tag("Cuenta")
        @Tag("Banco")
        @Test
        @Disabled
        void transferirDineroCuentasTest() {

            fail(); //forzamos la falla
            Cuenta cuenta1 = new Cuenta("Juan Pedro", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Diego", new BigDecimal("1500.8989"));
            Banco banco = new Banco();
            banco.setNombre("Banco Nacion");
            banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());

        }

        @Tag("Cuenta")
        @Tag("Banco")
        @Test
        void relacionBancoCuentasTest() {

            Cuenta cuenta1 = new Cuenta("Juan Pedro", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Diego", new BigDecimal("1500.8989"));
            Banco banco = new Banco();
            banco.addCuenta(cuenta1);
            banco.addCuenta(cuenta2);

            banco.setNombre("Banco Nacion");
            banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));

            assertAll(() -> {
                        assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
                    },
                    () -> {
                        assertEquals("3000", cuenta1.getSaldo().toPlainString());
                    },
                    () -> {
                        assertEquals(2, banco.getCuentas().size());
                    },
                    () -> {
                        assertEquals("Banco Nacion", cuenta1.getBanco().getNombre());
                    },
                    () -> {
                        assertEquals("Diego", banco.getCuentas().stream()
                                .filter(c -> c.getPersona().equals("Diego"))
                                .findFirst()
                                .get().getPersona()
                        );
                    },
                    () -> {
                        assertTrue(banco.getCuentas().stream()
                                .filter(c -> c.getPersona().equals("Diego"))
                                .findFirst().isPresent()
                        );
                    },
                    () -> { //same as above
                        assertTrue(banco.getCuentas().stream()
                                .anyMatch(c -> c.getPersona().equals("Diego"))
                        );
                    });
        }

    }

    @Nested
    @DisplayName("Probando diferentes sistemas operativos")
    class SistemaOperativoTest {
        //Test condicional para ciertos escenarios por ejemplo el SO
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void soloWindowsTest() {

        }

        @Test
        @EnabledOnOs({OS.MAC,OS.LINUX})
        void macOLinuxTest() {

        }

        @Test
        @DisabledOnOs(OS.LINUX)
        void noLinuxTest(){

        }

    }

    @Nested
    @DisplayName("probando las diferentes versiones de java")
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void soloJDK8(){

        }

        @Test
        @EnabledOnJre(JRE.JAVA_15)
        void soloJDK15() {

        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void noTestearJDK15() {

        }

    }

    @Nested
    @DisplayName("Probando variables de ambiente")
    class VariableAmbienteTest{
        @Test
        void imprimirVariablesSistemaTest() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ": " + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = ".*15.*")
        void javaVersionTest(){
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = "amd64")
        void arch64OnlyTest(){
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void arch32excludedTest(){
        }

        @Test
        void imprimirVariablesAmbiente(){
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> System.out.println(k + ": " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = "/usr/java/openjdk/jdk-15")
        void javaHomeTest(){
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void envNotExistTest(){
        }

    }

    @Test
    @DisplayName("AssumeTrue saldo cuenta")
    void saldoCuentaAsumeDevTest() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);

        assertNotNull(cuenta.getSaldo());
        assertEquals(10332.221, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

    }

    @Test
    @DisplayName("AssumingThatDev saldo cuenta")
    void saldoCuentaAssumingThatDevTest() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, () -> {
            assertNotNull(cuenta.getSaldo());
            assertEquals(10332.221, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        });
    }

    @RepeatedTest(value = 5, name = "{displayName} - Repeticion numero {currentRepetition} de {totalRepetitions}")
    @DisplayName("Probando credito de la cuenta")
    void creditoCuentaRepetirTest(RepetitionInfo info) {
        if(info.getCurrentRepetition()   == 3) {
            System.out.println("Estamos en la  repeticion " + info.getCurrentRepetition() + " de " + info.getTotalRepetitions());
        }
        Cuenta cuenta = new Cuenta("Pedro", new BigDecimal("1000.12345"));
        cuenta.credito(new BigDecimal("100"));
        assertNotNull(cuenta.getSaldo());
        assertEquals(1100, cuenta.getSaldo().intValue());
        assertEquals("1100.12345", cuenta.getSaldo().toPlainString());

    }

    @Tag("Param")
    @Nested
    class PruebasParametrizadasTest{

        @ParameterizedTest(name = "numero {index} ejecuntando con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100","200","300","500","700","1000"})
        @DisplayName("Probando debito de la cuenta")
        void debitoCuentaTest(String monto) {
            cuenta = new Cuenta("Pedro", new BigDecimal("900.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }



        @ParameterizedTest(name = "numero {index} ejecuntando con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100","2,200","3,300","4,500","5,700","6,1000"})
        @DisplayName("Probando debito de la cuenta")
        void debitoCuentaCsvTest(String index, String monto) {
            System.out.println( index + "->" + monto);
            cuenta = new Cuenta("Pedro", new BigDecimal("900.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecuntando con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100,pedro,pablo","250,200,maria,maria","310,300,diego,D10S","510,500,pepe,pepe","750,700,lala,lala","1000,1000,dedede,dedede"})
        @DisplayName("Probando debito de la cuenta")
        void debitoCuentaCsvTest2(String saldo, String monto,String expected, String actual) {
            System.out.println( saldo + " - " + monto + " - " + expected + " - " + actual);
            cuenta = new Cuenta("Pedro", new BigDecimal("900.12345"));
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(expected, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecuntando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        @DisplayName("Probando debito de la cuenta")
        void debitoCuentaCsvFileTest(String monto) {

            cuenta = new Cuenta("Pedro", new BigDecimal("900.12345"));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

    }

    @Tag("Param")
    @ParameterizedTest(name = "numero {index} ejecuntando con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    @DisplayName("Probando debito de la cuenta")
    void debitoCuentaMethodTest(String monto) {

        cuenta = new Cuenta("Pedro", new BigDecimal("1000.12345"));
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    public static List<String> montoList(){
        return Arrays.asList("100","200","300","500","700","1000","1000.12345");
    }

    @Tag("TimeOut")
    @Nested
    class TimeOutTest{
        @Test
        @Timeout(5)
        void timeOutTest() throws InterruptedException {
            TimeUnit.SECONDS.sleep(6);
        }

        @Test
        @Timeout(value = 500 , unit = TimeUnit.MILLISECONDS)
        void timeOutTest2() throws InterruptedException {
            TimeUnit.SECONDS.sleep(6);
        }

        @Test
        void timeOutAssertionsTest(){
            assertTimeout(Duration.ofSeconds(5), ()->{
                TimeUnit.MILLISECONDS.sleep(500);
            });
        }

    }


}