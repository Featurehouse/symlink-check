package org.featurehouse.mcmod.symlinkcheck;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public interface MappingProvider {
    @ExpectPlatform
    static MappingProvider getInstance() {
        throw new IllegalStateException("Unimplemented");
    }

    @Dotted String mapClass(@Dotted String intermediary);
    @NotNull String mapMethodName(
            @NotNull @Dotted String owner,
            @NotNull String name,
            @NotNull String desc
    );

    @NotNull String mapFieldName(
            @NotNull @Dotted String owner,
            @NotNull String name,
            @NotNull String desc
    );

    /* ************************** */

    default @NotNull ImmutableTriple<String, String, String> mapMethod(
            @NotNull @Dotted String owner,
            @NotNull String name,
            @NotNull String desc
    ) {
        String mappedOwner = this.mapClass(owner);
        String mappedName = this.mapMethodName(owner, name, desc);
        String mappedDescriptor = mapMethodType(desc);

        return ImmutableTriple.of(mappedOwner, mappedName, mappedDescriptor);
    }

    @SuppressWarnings("unused")
    default @NotNull ImmutableTriple<String, String, String> mapField(
            @NotNull @Dotted String owner,
            @NotNull String name,
            @NotNull String desc
    ) {
        String mappedOwner = this.mapClass(owner);
        String mappedName = this.mapFieldName(owner, name, desc);
        String mappedDescriptor = this.mapTypeDesc(Type.getType(desc)).getDescriptor();

        return ImmutableTriple.of(mappedOwner, mappedName, mappedDescriptor);
    }

    default @NotNull String mapMethodType(String desc) {
        Type methodType = Type.getMethodType(desc);
        Type[] args = methodType.getArgumentTypes();
        int len = args.length;
        for (int i = 0; i < len; i++) {
            args[i] = mapTypeDesc(args[i]);
        }

        Type returnType = mapTypeDesc(methodType.getReturnType());

        return Type.getMethodType(returnType, args).getDescriptor();
    }

    private static boolean isPrimitive(/* type desc */ Type type) {
        return type.getSort() <= 8; // [0, 8]
    }

    default Type mapTypeDesc(Type type) {
        if (isPrimitive(type)) return type;
        if (type.getSort() == Type.ARRAY) {
            int dimension = type.getDimensions();
            Type mappedComponent = mapTypeDesc(type.getElementType());
            String descriptor = StringUtils.repeat('[', dimension).concat(mappedComponent.getDescriptor());
            return Type.getType(descriptor);
        } // Object
        String mappedName = mapClass(type.getClassName());
        return Type.getObjectType(mappedName.replace('.', '/'));
    }

    static @Nullable MethodNode findMethod(@NotNull ClassNode classNode,
                                           @NotNull ImmutableTriple<?, String, String> method) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(method.middle) && m.desc.equals(method.right)) return m;
        } return null;
    }

    static MethodInsnNode findVirtualInvocation(InsnList list, ImmutableTriple<String, String, String> method) {
        for (AbstractInsnNode node : list) {
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode min) {
                if (min.name.equals(method.getMiddle()) && min.owner.equals(method.getLeft()) && min.desc.equals(method.getRight()))
                    return min;
            }
        }
        return null;
    }

    static MethodInsnNode createMethodInstruction(int opcode, ImmutableTriple<@Slashed(force = false) String, String, String> method) {
        return new MethodInsnNode(opcode, method.getLeft().replace('.', '/'), method.getMiddle(), method.getRight());
    }
}
