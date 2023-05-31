package org.featurehouse.mcmod.symlinkcheck;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class MappingConstants {
    public static /* dotted */ @NotNull String mapClass(/* dotted */ @NotNull String intermediary) {
        return FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", intermediary);
    }

    public static @NotNull String mapMethodType(String desc) {
        Type methodType = Type.getMethodType(desc);
        Type[] args = methodType.getArgumentTypes();
        int len = args.length;
        for (int i = 0; i < len; i++) {
            args[i] = mapTypeDesc(args[i]);
        }

        Type returnType = mapTypeDesc(methodType.getReturnType());

        return Type.getMethodType(returnType, args).getDescriptor();
    }

    public static @NotNull ImmutableTriple<String, String, String> mapMethod(
            @NotNull String owner,
            @NotNull String name,
            @NotNull String desc) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        String mappedOwner = resolver.mapClassName("intermediary", owner);
        String mappedName = resolver.mapMethodName("intermediary", owner, name, desc);

        String mappedDescriptor = mapMethodType(desc);

        return ImmutableTriple.of(mappedOwner, mappedName, mappedDescriptor);
    }

    private static boolean isPrimitive(/* type desc */ Type type) {
        return type.getSort() <= 8; // [0, 8]
    }

    protected static Type mapTypeDesc(Type type) {
        if (isPrimitive(type)) return type;
        if (type.getSort() == Type.ARRAY) {
            int dimension = type.getDimensions();
            Type mappedComponent = mapTypeDesc(type.getElementType());
            String descriptor = StringUtils.repeat('[', dimension).concat(mappedComponent.getDescriptor());
            return Type.getType(descriptor);
        } // Object
        String mappedName = mapClass(type.getClassName());
        return Type.getObjectType(mappedName.replaceAll("\\.", "/"));
    }

    public static @Nullable MethodNode findMethod(@NotNull ClassNode classNode,
                                                  @NotNull ImmutableTriple<String, String, String> method) {
        for (MethodNode m : classNode.methods) {
            if (m.name.equals(method.middle) && m.desc.equals(method.right)) return m;
        } return null;
    }

    public static MethodInsnNode findVirtualInvocation(InsnList list, ImmutableTriple<String, String, String> method) {
        for (AbstractInsnNode node : list) {
            if (node.getOpcode() == Opcodes.INVOKEVIRTUAL && node instanceof MethodInsnNode min) {
                if (min.name.equals(method.getMiddle()) && min.owner.equals(method.getLeft()) && min.desc.equals(method.getRight()))
                    return min;
            }
        }
        return null;
    }

    public static MethodInsnNode createMethodInstruction(int opcode, ImmutableTriple<String, String, String> method) {
        return new MethodInsnNode(opcode, method.getLeft(), method.getMiddle(), method.getRight());
    }

    public static String mapMethodName(String owner, String name, String desc) {
        return FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", owner, name, desc);
    }
}
