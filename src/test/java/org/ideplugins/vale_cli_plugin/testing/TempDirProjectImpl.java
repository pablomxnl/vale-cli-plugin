package org.ideplugins.vale_cli_plugin.testing;

import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class TempDirProjectImpl extends TempDirTestFixtureImpl {

    private final Path root;

    public TempDirProjectImpl(Path root){
        this.root = root;
    }

    @Override
    protected @Nullable Path getTempHome() {
        return root;
    }
}
