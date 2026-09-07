package io.github.ricardodlm.springai.mcp.develop.archetype;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import static io.github.ricardodlm.springai.mcp.develop.archetype.Constants.WINDOWS_RESERVED_WORDS_LIST;

public class MvnArchetype {

    // Uso esperado: <nombreMicro> <versionMicro> <nombreProyectoOpenShift>
    private static final int ARG_NOMBRE_MICRO = 0;
    private static final int ARG_VERSION_MICRO = 1;
    private static final int ARG_NOMBRE_PROYECTO_OPENSHIFT = 2;

    public static void main(String[] args) throws InterruptedException, IOException {

        String sistemaOperativo = System.getProperty(Constants.OSNAME);

        // Version del arquetipo a generar. Se resuelve en runtime contra
        // Nexus por coordenadas Maven — no se embebe ningun jar/pom.
        String archetypeVersion = readArchetypeVersion();

        try {

            validateArguments(args);

            String nombreMicro = args[ARG_NOMBRE_MICRO];
            String versionMicro = args[ARG_VERSION_MICRO];
            String nombreProyectoOpenShift = args[ARG_NOMBRE_PROYECTO_OPENSHIFT];

            // Java no permite guiones en un package, y el package tampoco
            // admite mayúsculas por convención. nombreMicro y
            // nombreProyectoOpenShift SÍ pueden llevar guiones (lo exige
            // PROJECT_REGEX), así que se calculan aquí las versiones
            // normalizadas (minúsculas, sin guiones) para construir tanto
            // el package como las properties micro/domain del arquetipo.
            String domainCode = sanitizeForPackage(nombreProyectoOpenShift);
            String microCode = sanitizeForPackage(nombreMicro);
            String javaPackage = "io.github.ricardodlm.springai.mcp." + domainCode + "." + microCode;

            // groupId y package comparten el mismo valor por convención.
            // nombreMicro se pasa como -DmicroName (property obligatoria),
            // artifactId se deriva automaticamente en el propio arquetipo.
            String command = String.format(Constants.UNFORMATTED_MVN_COMMAND, archetypeVersion,
                    nombreMicro, microCode, nombreProyectoOpenShift, domainCode, javaPackage, javaPackage, versionMicro);

            int exitCode = runCommand(sistemaOperativo, command, System.getProperty("user.dir"));

            if (exitCode != 0) {
                throw new IllegalStateException("Archetype process not successful");
            }
            System.exit(exitCode);

        } catch (IllegalArgumentException e) {
            System.out.println(" ! " + e.getMessage());
        } catch (IOException e) {
            System.out.println("No se ha podido generar el proyecto desde el arquetipo");
        }

    }

    private static int runCommand(String sistemaOperativo, String command, String workingDir)
            throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();

        if (sistemaOperativo.startsWith(Constants.WINDOWS)) {
            builder.command("cmd.exe", "/c", command);
        } else {
            builder.command("sh", "-c", command);
        }

        builder.directory(new File(workingDir));
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);

        return process.waitFor();
    }

    private static String readArchetypeVersion() throws IOException {

        Properties properties = new Properties();

        try (InputStream in = MvnArchetype.class.getClassLoader().getResourceAsStream("archetype.properties")) {

            if (in == null) {
                throw new IOException("No se ha encontrado archetype.properties en el classpath");
            }

            properties.load(in);
        }

        String version = properties.getProperty("archetype.version");

        if (version == null || version.trim().isEmpty()) {
            throw new IOException("La propiedad archetype.version no está definida en archetype.properties");
        }

        return version;
    }

    /**
     * Convierte un nombre válido de proyecto (permite guiones y mayúsculas,
     * PROJECT_REGEX) en un código válido para package Java y para las
     * properties micro/domain del arquetipo: minúsculas, sin guiones. Si el
     * resultado empezara por dígito, lo prefija para evitar un identificador
     * Java inválido.
     */
    private static String sanitizeForPackage(String value) {

        String sanitized = value.replace("-", "").toLowerCase();

        if (sanitized.isEmpty()) {
            throw new IllegalArgumentException("El valor '" + value + "' queda vacío al eliminar los guiones");
        }

        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "p" + sanitized;
        }

        return sanitized;
    }

    private static void validateArguments(String[] args) {

        if (args == null || args.length == 0 || args[ARG_NOMBRE_MICRO] == null || args[ARG_NOMBRE_MICRO].trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.NOARGUMENTS_ERROR_MESSAGE);
        }

        if (args.length == 1 || args[ARG_VERSION_MICRO] == null || args[ARG_VERSION_MICRO].trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.ONE_ARGUMENT_ERROR_MESSAGE);
        }

        if (args.length == 2 || args[ARG_NOMBRE_PROYECTO_OPENSHIFT] == null || args[ARG_NOMBRE_PROYECTO_OPENSHIFT].trim().isEmpty()) {
            throw new IllegalArgumentException(Constants.TWO_ARGUMENTS_ERROR_MESSAGE);
        }

        validateFormatArguments(args);

    }

    private static void validateFormatArguments(String[] args) {

        if (!args[ARG_NOMBRE_MICRO].matches(Constants.PROJECT_REGEX)) {
            throw new IllegalArgumentException(Constants.INVALID_MICRONAME_MESSAGE);
        }

        if (WINDOWS_RESERVED_WORDS_LIST.contains(args[ARG_NOMBRE_MICRO].toUpperCase())) {
            throw new IllegalArgumentException(Constants.RESERVED_WORDS_ERROR_MESSAGE);
        }

    }

}
