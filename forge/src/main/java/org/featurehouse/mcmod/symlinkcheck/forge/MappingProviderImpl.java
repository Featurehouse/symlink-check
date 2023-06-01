package org.featurehouse.mcmod.symlinkcheck.forge;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraftforge.fml.ModList;
import org.featurehouse.mcmod.symlinkcheck.MappingProvider;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class MappingProviderImpl implements MappingProvider {
    private final BiMap<Type, Type> classes;
    private final Map<NodeElement, String> fields;
    private final Map<NodeElement, String> methods;

    MappingProviderImpl(BiMap<Type, Type> classes, Map<NodeElement, String> fields, Map<NodeElement, String> methods) {
        this.classes = classes;
        this.fields = fields;
        this.methods = methods;
    }

    public static class NodeElement {
        final Type owner;
        final String name;
        final Type desc;

        NodeElement(Type owner, String name, Type desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        public static NodeElement of(String owner, String name, String desc) {
            return new NodeElement(Type.getObjectType(owner),
                    name, Type.getType(desc));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeElement that = (NodeElement) o;
            return Objects.equals(owner, that.owner) && Objects.equals(name, that.name) && Objects.equals(desc, that.desc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, name, desc);
        }

        @Override
        public String toString() {
            return owner + "." + name + ':' + desc;
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        public void addClass(String klass, String mapped) {
            classes.put(Type.getObjectType(klass), Type.getObjectType(mapped));
        }

        public void addField(String klass, String field, String mapped, String desc) {
            fields.put(NodeElement.of(klass, field, desc), mapped);
        }

        public void addMethod(String klass, String method, String mapped, String desc) {
            methods.put(NodeElement.of(klass, method, desc), mapped);
        }

        Builder() {}

        public MappingProviderImpl build() {
            return new MappingProviderImpl(classes, fields, methods);
        }

        private final BiMap<Type, Type> classes = HashBiMap.create();
        final Map<NodeElement, String> fields = new HashMap<>();
        final Map<NodeElement, String> methods = new HashMap<>();
    }

    @Nonnull
    public String mapClass(String name) {
        final Type type1;
        if (name.startsWith("[")) {
            type1 = Type.getType(name);
        } else
            type1 = Type.getObjectType(name.replace('.', '/'));

        return mapType(type1).getInternalName().replace('/', '.');
    }

    @Override
    public @NotNull String mapFieldName(@NotNull String owner, @NotNull String name, @NotNull String desc) {
        return fields.get(NodeElement.of(owner.replace('.', '/'), name, desc));
    }

    @Override
    public @NotNull String mapMethodName(@NotNull String owner, @NotNull String name, @NotNull String desc) {
        return methods.get(NodeElement.of(owner.replace('.', '/'), name, desc));
    }

    public Type mapType(Type type) {
        switch (type.getSort()) {
            case Type.ARRAY -> {
                final int dimensions = type.getDimensions();
                Type elementType = type.getElementType();
                elementType = mapType(elementType);
                String sb = "[".repeat(Math.max(0, dimensions)) +
                        elementType.getDescriptor();
                return Type.getType(sb);
            }
            case Type.OBJECT -> {
                return classes.getOrDefault(type, type);
            }
            default -> {
                return type;
            }
        }
    }

    public Type mapMethodType(Type type) {
        Type returnType = type.getReturnType();
        returnType = mapType(returnType);
        final Type[] types = Arrays.stream(type.getArgumentTypes())
                .map(this::mapType)
                .toArray(Type[]::new);
        return Type.getMethodType(returnType, types);
    }

    public static MappingProviderImpl empty() {
        return new MappingProviderImpl(ImmutableBiMap.of(), Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "classes=" + classes +
                ", fields=" + fields +
                ", methods=" + methods +
                '}';
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingProviderImpl.class);
    private static final Supplier<MappingProvider> INSTANCE = Suppliers.memoize(() -> {
        Path path = Objects.requireNonNull(ModList.get().getModFileById("symlinkcheck")).getFile().findResource("forge-map.txt");
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return TinyUtils.read(reader, "intermediary", "srg");
        } catch (IOException e) {
            LOGGER.error("Failed to read mapping", e);
            return empty();
        }
    });

    public static MappingProvider getInstance() {
        return INSTANCE.get();
    }
}
