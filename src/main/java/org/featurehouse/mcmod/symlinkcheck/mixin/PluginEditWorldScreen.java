package org.featurehouse.mcmod.symlinkcheck.mixin;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.featurehouse.mcmod.symlinkcheck.SafeStorageSource;
import org.featurehouse.mcmod.symlinkcheck.impl.ImplLevelStorageSource;
import org.featurehouse.mcmod.symlinkcheck.marks.Mark0;
import org.featurehouse.mcmod.symlinkcheck.marks.Mark1;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.featurehouse.mcmod.symlinkcheck.MappingConstants.*;
import static org.objectweb.asm.Opcodes.*;

public class PluginEditWorldScreen implements IMixinConfigPlugin {
    //private String c_editWorldScreen;
    private ImmutableTriple<String, String, String> m_makeBackupAndShowToast;
    private ImmutableTriple<String, String, String> m_createAccess;
    //= "net/minecraft/class_528$class_4272";
    private String m_joinWorld, m_editWorld, m_recreateWorld;
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginEditWorldScreen.class);

    @Override
    public void onLoad(String mixinPackage) {
        m_makeBackupAndShowToast = mapMethod("net.minecraft.class_524", "method_29784",
                "(Lnet/minecraft/class_32;Ljava/lang/String;)V");
        m_createAccess = mapMethod("net.minecraft.class_32", "method_27002",
                "(Ljava/lang/String;)Lnet/minecraft/class_32$class_5143;");
        m_joinWorld = mapMethodName("net.minecraft.class_528$class_4272", "method_20164", "()V");
        m_editWorld = mapMethodName("net.minecraft.class_528$class_4272", "method_20171", "()V");
        m_recreateWorld = mapMethodName("net.minecraft.class_528$class_4272", "method_20173", "()V");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.interfaces.contains(Type.getInternalName(Mark0.class))) {
            MethodNode method = findMethod(targetClass, m_makeBackupAndShowToast);
            if (method == null) {
                LOGGER.warn("Can't find method {}", m_makeBackupAndShowToast);
                return;
            }
            MethodInsnNode node = findVirtualInvocation(method.instructions, m_createAccess);
            if (node == null) {
                LOGGER.warn("Can't find virtual method invocation {}", m_createAccess);
                return;
            }
            InsnList l = new InsnList();
            {
                String c_safeStorageSource = Type.getInternalName(SafeStorageSource.class);
                l.add(new InsnNode(SWAP));
                l.add(new TypeInsnNode(CHECKCAST, c_safeStorageSource));
                l.add(new InsnNode(SWAP));
                Type methodType = Type.getMethodType(Type.getReturnType(m_createAccess.getRight()), Type.getType(String.class), Type.getType(Consumer.class));
                // ex consumer:
                l.add(new VarInsnNode(ALOAD, 1));
                l.add(createMethodInstruction(INVOKESTATIC, ImmutableTriple.of(Type.getInternalName(ImplLevelStorageSource.class), "consumerEditWorldScreen", "(Ljava/lang/String;)Ljava/util/function/Consumer;")));
                ImmutableTriple<String, String, String> triple = ImmutableTriple.of(c_safeStorageSource, "validateAndCreateAccessCatch", methodType.getDescriptor());
                l.add(createMethodInstruction(INVOKEINTERFACE, triple));
            }
            method.instructions.insert(node, l);
            method.instructions.remove(node);
        } else if (targetClass.interfaces.contains(Type.getInternalName(Mark1.class))) {
            Stream.of(findMethod(targetClass, ImmutableTriple.of("", m_joinWorld, "()V")), findMethod(targetClass, ImmutableTriple.of("", m_editWorld, "()V")))
                    .filter(Objects::nonNull)
                    .forEach(method -> {
                        MethodInsnNode node = findVirtualInvocation(method.instructions, m_createAccess);
                        if (node == null) {
                            LOGGER.warn("Can't find virtual method invocation {}", m_createAccess);
                            return;
                        }
                        InsnList l = new InsnList();
                        {
                            String c_safeStorageSource = Type.getInternalName(SafeStorageSource.class);
                            l.add(new InsnNode(SWAP));
                            l.add(new TypeInsnNode(CHECKCAST, c_safeStorageSource));
                            l.add(new InsnNode(SWAP));
                            Type methodType = Type.getMethodType(Type.getReturnType(m_createAccess.getRight()), Type.getType(String.class), Type.getType(Consumer.class));
                            // ex consumer:
                            l.add(new VarInsnNode(ALOAD, 0));
                            l.add(createMethodInstruction(INVOKESTATIC, ImmutableTriple.of(Type.getInternalName(ImplLevelStorageSource.class), "consumer1", "(Ljava/lang/Object;)Ljava/util/function/Consumer;")));
                            ImmutableTriple<String, String, String> triple = ImmutableTriple.of(c_safeStorageSource, "validateAndCreateAccessCatch", methodType.getDescriptor());
                            l.add(createMethodInstruction(INVOKEINTERFACE, triple));
                        }
                        method.instructions.insert(node, l);
                        method.instructions.remove(node);
                    });
            MethodNode method = findMethod(targetClass, ImmutableTriple.of("", m_recreateWorld, "()V"));
            if (method == null) {
                LOGGER.warn("Can't find method {}:()V", m_recreateWorld);
                return;
            }
            MethodInsnNode node = findVirtualInvocation(method.instructions, m_createAccess);
            if (node == null) {
                LOGGER.warn("Can't find virtual method invocation {}", m_createAccess);
                return;
            }
            InsnList l = new InsnList();
            {
                String c_safeStorageSource = Type.getInternalName(SafeStorageSource.class);
                l.add(new InsnNode(SWAP));
                l.add(new TypeInsnNode(CHECKCAST, c_safeStorageSource));
                l.add(new InsnNode(SWAP));
                Type methodType = Type.getMethodType(Type.getReturnType(m_createAccess.getRight()), Type.getType(String.class), Type.getType(Consumer.class));
                // ex consumer:
                l.add(new VarInsnNode(ALOAD, 0));
                l.add(createMethodInstruction(INVOKESTATIC, ImmutableTriple.of(Type.getInternalName(ImplLevelStorageSource.class), "consumer2", "(Ljava/lang/Object;)Ljava/util/function/Consumer;")));
                ImmutableTriple<String, String, String> triple = ImmutableTriple.of(c_safeStorageSource, "validateAndCreateAccessCatch", methodType.getDescriptor());
                l.add(createMethodInstruction(INVOKEINTERFACE, triple));
            }
            method.instructions.insert(node, l);
            method.instructions.remove(node);
        }
    }
}
