package org.featurehouse.mcmod.symlinkcheck.fabric;

import net.fabricmc.loader.api.FabricLoader;
import org.featurehouse.mcmod.symlinkcheck.Dotted;
import org.featurehouse.mcmod.symlinkcheck.MappingProvider;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public class MappingProviderImpl implements MappingProvider {
    private final net.fabricmc.loader.api.MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

    public static MappingProvider getInstance() {
        return new MappingProviderImpl();
    }

    @Override
    @Dotted
    public String mapClass(@Dotted(force = false) String intermediary) {
        if (intermediary.startsWith("[")) {
            return mapTypeDesc(Type.getType(intermediary)).getDescriptor();
        }
        return resolver.mapClassName("intermediary", intermediary.replace('/', '.'));
    }

    @Override
    public @NotNull String mapMethodName(@NotNull @Dotted(force = false) String owner, @NotNull String name, @NotNull String desc) {
        return resolver.mapMethodName("intermediary", owner.replace('/', '.'), name, desc);
    }

    @Override
    public @NotNull String mapFieldName(@NotNull @Dotted(force = false) String owner, @NotNull String name, @NotNull String desc) {
        return resolver.mapFieldName("intermediary", owner.replace('/', '.'), name, desc);
    }
}
