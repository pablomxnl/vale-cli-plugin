package org.ideplugins.vale_cli_plugin.annotator;

import java.util.List;

public record ValeConfig(String rootIni, List<String> configFiles,
List<String> paths, List<String> vocab) {
}
