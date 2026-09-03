package io.github.ricardodlm.springai.mcp.develop.archetype;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Constants {

    private Constants() {
    }

    /**
     * <h2>Project names: </h2>
     * [a-z] must start by lowercase letters or in other words, CANNOT start by a number or the hyphen ('-'). <br />
     * [0-9a-z\-]* may have any lowercase, number or hypen <br />
     * (?<!\-) but the names cannot end in hyphen
     */
    public static final String PROJECT_REGEX = "^[a-z][0-9a-z\\-]*(?<!\\-)$";
    public static final String uPROJECT_REGEX = "^(?=.*\\pL)[A-Z_a-z][0-9A-Z_a-z]*(?<!_)$";

    public static final String OSNAME = "os.name";
    public static final String WINDOWS = "Windows";

    private static final String[] WIN_RESERVED_WORDS = new String[]{"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
            "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    public static final List WINDOWS_RESERVED_WORDS_LIST = Collections.unmodifiableList (Arrays.asList(WIN_RESERVED_WORDS));

    // microName es la property OBLIGATORIA (sin defaultValue en el XML):
    // se pasa siempre explícita, artifactId deriva de ella automáticamente.
    // groupId y package SÍ se pasan explícitos (calculados en Java, sin
    // guiones, minúsculas) porque Velocity no puede limpiar guiones ni pasar
    // a minúsculas dentro de un defaultValue. Ambos comparten el mismo valor
    // por convención (com.sca.mcp.<domain>.<micro>).
    // transport queda fijo a "mvc" directamente en la plantilla (no es argumento del jar).
    // Placeholders (%s), en orden: archetypeVersion, microName (nombreMicro, legible),
    // micro (código corto sin guiones), dominio (nombreProyectoOpenShift, legible),
    // domain (código corto sin guiones), groupId (calculado), package (calculado,
    // mismo valor que groupId), appVersion (versionMicro)
    //
    // NOTA: sin -DarchetypeCatalog=local — el arquetipo se resuelve directamente
    // contra Nexus por coordenadas Maven, igual que cualquier otra dependencia.
    static final String UNFORMATTED_MVN_COMMAND =
            "mvn archetype:generate -DarchetypeGroupId=com.sca.framework.architecture.mcp"
                    + " -DarchetypeArtifactId=mcp-server-archetype -DarchetypeVersion=%s"
                    + " -DmicroName=%s -Dmicro=%s -DdomainName=%s -Ddomain=%s -DgroupId=%s -Dpackage=%s -Dversion=%s"
                    + " -DinteractiveMode=false -Dtransport=mvc";

    public static final String NOARGUMENTS_ERROR_MESSAGE = "The Online installer must be provided with at least three arguments to work "
            + "properly, <nombreMicro>, <versionMicro> and <nombreProyectoOpenShift>, ex: \n\t"
            + " mcp-scaapa-poc 1.0.0 salud";

    public static final String ONE_ARGUMENT_ERROR_MESSAGE = "Missing arguments <versionMicro> and <nombreProyectoOpenShift>. Please provide them to proceed.";
    public static final String TWO_ARGUMENTS_ERROR_MESSAGE = "Missing argument <nombreProyectoOpenShift>. Please provide it to proceed.";

    public static final String INVALID_MICRONAME_MESSAGE = "The project name may only contain lowercase letters,"
            + " numbers and hyphens, and it cannot start by number or end by underscore (regex: " + Constants.PROJECT_REGEX + ")";

    public static final String RESERVED_WORDS_ERROR_MESSAGE =
            "The nombreMicro cannot contain words reserved by the Windows OS";

}
