package debug.tty;

import debug.classfile.Instruction;
import debug.classfile.Instruction.TypeKind;

import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

import java.util.TreeMap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;



public class DisassembleMethod {

    public static final int CONSTANT_Utf8 = 1;
    public static final int CONSTANT_Integer = 3;
    public static final int CONSTANT_Float = 4;
    public static final int CONSTANT_Long = 5;
    public static final int CONSTANT_Double = 6;
    public static final int CONSTANT_Class = 7;
    public static final int CONSTANT_String = 8;
    public static final int CONSTANT_Fieldref = 9;
    public static final int CONSTANT_Methodref = 10;
    public static final int CONSTANT_InterfaceMethodref = 11;
    public static final int CONSTANT_NameAndType = 12;
    public static final int CONSTANT_MethodHandle = 15;
    public static final int CONSTANT_MethodType = 16;
    public static final int CONSTANT_Dynamic = 17;
    public static final int CONSTANT_InvokeDynamic = 18;
    public static final int CONSTANT_Module = 19;
    public static final int CONSTANT_Package = 20;
    public static final int ANYTHING = -1;
    public static final int NOTHING = -2;
    public static final int EITHER_Methodref_OR_InterfaceMethodref = -3;

    public static final int REF_getField = 1;
    public static final int REF_getStatic = 2;
    public static final int REF_putField = 3;
    public static final int REF_putStatic = 4;
    public static final int REF_invokeVirtual = 5;
    public static final int REF_invokeStatic = 6;
    public static final int REF_invokeSpecial = 7;
    public static final int REF_newInvokeSpecial = 8;
    public static final int REF_invokeInterface = 9;

    private class ConstantPool {

        public ConstantPool (ReferenceType parent) {
            try {
                byte[] data = parent.constantPool();
                try {
                    readData(data);
                } catch (IOException e) {}
            } catch (UnsupportedOperationException e) {}
        }

        public class ConstantPoolInfo {
            public int tag;
            public int nameIndex, classIndex, nameAndTypeIndex, typeIndex;
            public Object value;
            public int descriptorIndex, bootstrapMethodAttrIndex;
            public int referenceKind, referenceIndex, stringIndex;
        }

        private ArrayList<ConstantPoolInfo> pool = new ArrayList<>();

        private String getConstantUTF8(int index) {
            return getConstantWithTag(index, CONSTANT_Utf8);
        }

        private String getConstantWithTag(int index, int expected_tag) {
            ConstantPoolInfo c = null;

            if ((index > 0) && (index < pool.size())) {
                // constant index is valid
                c = pool.get(index);
            }
            if ((c != null) && (expected_tag == ANYTHING)) {
                // any tag is allowed
                expected_tag = c.tag;
            }
            if ((c != null) && (expected_tag == EITHER_Methodref_OR_InterfaceMethodref)) {
                // Two tags are allowed
                switch (c.tag) {
                    case CONSTANT_InterfaceMethodref:
                    case CONSTANT_Methodref:
                        expected_tag = c.tag;
                        break;
                    default:
                        break;
                }
            }
            if ((c == null) || (c.tag != expected_tag)) {
                // This constant cannot be decoded
                expected_tag = NOTHING;
            }

            switch (expected_tag) {
                case CONSTANT_Module:
                    return "module(" + getConstantUTF8(c.nameIndex) + ")";

                case CONSTANT_Class:
                    return "class(" + getConstantUTF8(c.nameIndex) + ")";

                case CONSTANT_Package:
                    return "package(" + getConstantUTF8(c.nameIndex) + ")";

                case CONSTANT_Utf8:
                    return "\"" + c.value + "\"";

                case CONSTANT_Fieldref:
                    return "fieldRef(" + getConstantWithTag(c.classIndex, CONSTANT_Class) + ", " +
                            getConstantWithTag(c.nameAndTypeIndex, CONSTANT_NameAndType) + ")";

                case CONSTANT_InterfaceMethodref:
                    return "interfaceMethodRef(" + getConstantWithTag(c.classIndex, CONSTANT_Class) + ", " +
                            getConstantWithTag(c.nameAndTypeIndex, CONSTANT_NameAndType) + ")";

                case CONSTANT_Methodref:
                    return "methodRef(" + getConstantWithTag(c.classIndex, CONSTANT_Class) + ", " +
                            getConstantWithTag(c.nameAndTypeIndex, CONSTANT_NameAndType) + ")";

                case CONSTANT_Double:
                    return "double(" + c.value.toString() + ")";

                case CONSTANT_Float:
                    return "float(" + c.value.toString() + ")";

                case CONSTANT_Integer:
                    return "int(" + c.value.toString() + ")";

                case CONSTANT_Long:
                    return "long(" + c.value.toString() + ")";

                case CONSTANT_InvokeDynamic:
                    return "invokeDynamic(" + c.bootstrapMethodAttrIndex + ", " +
                            getConstantWithTag(c.nameAndTypeIndex, CONSTANT_NameAndType) + ")";

                case CONSTANT_Dynamic:
                    return "dynamic(" + c.bootstrapMethodAttrIndex + ", " +
                            getConstantWithTag(c.nameAndTypeIndex, CONSTANT_NameAndType) + ")";

                case CONSTANT_MethodHandle:
                    switch (c.referenceKind) {
                        case REF_getField:
                            return "methodHandle(getField, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_Fieldref) + ")";
                        case REF_getStatic:
                            return "methodHandle(getStatic, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_Fieldref) + ")";
                        case REF_putField:
                            return "methodHandle(putField, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_Fieldref) + ")";
                        case REF_putStatic:
                            return "methodHandle(putStatic, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_Fieldref) + ")";
                        case REF_invokeVirtual:
                            return "methodHandle(invokeVirtual, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_Methodref) + ")";
                        case REF_newInvokeSpecial:
                            return "methodHandle(newInvokeSpecial, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_Methodref) + ")";
                        case REF_invokeStatic:
                            return "methodHandle(invokeStatic, " +
                                getConstantWithTag(c.referenceIndex, EITHER_Methodref_OR_InterfaceMethodref) + ")";
                        case REF_invokeSpecial:
                            return "methodHandle(invokeSpecial, " +
                                getConstantWithTag(c.referenceIndex, EITHER_Methodref_OR_InterfaceMethodref) + ")";
                        case REF_invokeInterface:
                            return "methodHandle(invokeInterface, " +
                                getConstantWithTag(c.referenceIndex, CONSTANT_InterfaceMethodref) + ")";
                        default:
                            return "methodHandle(" + c.referenceKind + ", " +
                                getConstantWithTag(c.nameAndTypeIndex, NOTHING) + ")";
                    }

                case CONSTANT_MethodType:
                    return "methodType(" + getConstantUTF8(c.descriptorIndex) + ")";

                case CONSTANT_NameAndType:
                    return "nameAndType(" + getConstantUTF8(c.nameIndex) + ", " +
                            getConstantUTF8(c.typeIndex) + ")";

                case CONSTANT_String:
                    return "string(" + getConstantUTF8(c.stringIndex) + ")";

                default:
                    // This constant cannot be decoded
                    return "constant(" + index + ")";
            }
        }

        public String getConstant(int index) {
            return getConstantWithTag(index, ANYTHING);
        }

        private void readData(byte[] data) throws IOException {
/*
            System.out.println("");
            System.out.print(" >>");
            for (int j = 0; j < data.length; j++) {
                System.out.print(" " + Integer.toHexString(data[j]));
            }
            System.out.println(" <<");
*/
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

            pool.add(null); // element zero - unused

            while (true) {
                ConstantPoolInfo c = new ConstantPoolInfo();
                c.tag = in.readUnsignedByte();
                switch (c.tag) {
                    case CONSTANT_Module:
                    case CONSTANT_Class:
                    case CONSTANT_Package:
                        c.nameIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_Fieldref:
                    case CONSTANT_InterfaceMethodref:
                    case CONSTANT_Methodref:
                        c.classIndex = in.readUnsignedShort();
                        c.nameAndTypeIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_Double:
                        c.value = Double.valueOf(in.readDouble());
                        pool.add(c);
                        c = null; // add a blank space
                        break;

                    case CONSTANT_Float:
                        c.value = Float.valueOf(in.readFloat());
                        break;

                    case CONSTANT_Integer:
                        c.value = Integer.valueOf(in.readInt());
                        break;

                    case CONSTANT_Long:
                        c.value = Long.valueOf(in.readLong());
                        pool.add(c);
                        c = null; // add a blank space
                        break;

                    case CONSTANT_InvokeDynamic:
                    case CONSTANT_Dynamic:
                        c.bootstrapMethodAttrIndex = in.readUnsignedShort();
                        c.nameAndTypeIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_MethodHandle:
                        c.referenceKind = in.readUnsignedByte();
                        c.referenceIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_MethodType:
                        c.descriptorIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_NameAndType:
                        c.nameIndex = in.readUnsignedShort();
                        c.typeIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_String:
                        c.stringIndex = in.readUnsignedShort();
                        break;

                    case CONSTANT_Utf8:
                        c.value = in.readUTF();
                        break;

                    default:
                        // invalid entry
                        return;
                }
                pool.add(c);
            }
        }
    }

    private class DisassembleInstruction {

        private class Description implements Instruction.KindVisitor<Void,Integer> {

            private String text = "";
            private ConstantPool constantPool;

            public Description(ConstantPool _constantPool) {
                constantPool = _constantPool;
            }

            public String toString() {
                return text;
            }

            public Void visitNoOperands(Instruction instr, Integer indent) {
                return null;
            }

            public Void visitArrayType(Instruction instr, TypeKind kind, Integer indent) {
                text += "arrayType(" + kind.name + ")";
                return null;
            }

            public Void visitBranch(Instruction instr, int offset, Integer indent) {
                text += "pc(" + Integer.toString(instr.getPC() + offset) + ")";
                return null;
            }

            public Void visitConstantPoolRef(Instruction instr, int index, Integer indent) {
                text += constantPool.getConstant(index);
                return null;
            }

            public Void visitConstantPoolRefAndValue(Instruction instr, int index, int value, Integer indent) {
                text += constantPool.getConstant(index) + ", " + value;
                return null;
            }

            public Void visitLocal(Instruction instr, int index, Integer indent) {
                text += "local(" + index + ")";
                return null;
            }

            public Void visitLocalAndValue(Instruction instr, int index, int value, Integer indent) {
                text += "local(" + index + "), " + value;
                return null;
            }

            public Void visitLookupSwitch(Instruction instr,
                    int default_, int npairs, int[] matches, int[] offsets, Integer indent) {
                text += "lookupSwitch(...)";
                /* int pc = instr.getPC();
                print("{ // " + npairs);
                indent(indent);
                for (int i = 0; i < npairs; i++) {
                    print(String.format("%n%12d: %d", matches[i], (pc + offsets[i])));
                }
                print("\n     default: " + (pc + default_) + "\n}");
                indent(-indent); */
                return null;
            }

            public Void visitTableSwitch(Instruction instr,
                    int default_, int low, int high, int[] offsets, Integer indent) {
                text += "tableSwitch(...)";
                /* int pc = instr.getPC();
                print("{ // " + low + " to " + high);
                indent(indent);
                for (int i = 0; i < offsets.length; i++) {
                    print(String.format("%n%12d: %d", (low + i), (pc + offsets[i])));
                }
                print("\n     default: " + (pc + default_) + "\n}");
                indent(-indent); */
                return null;
            }

            public Void visitValue(Instruction instr, int value, Integer indent) {
                text += "value(" + value + ")";
                return null;
            }

            public Void visitUnknown(Instruction instr, Integer indent) {
                return null;
            }
        }

        private String text;

        public DisassembleInstruction (ConstantPool constantPool, Instruction instruction) {
            Description description = new Description(constantPool);
            instruction.accept(description, 0);
            text = String.format("%10s = %s(%s)",
                        "pc(" + Integer.toString(instruction.getPC()) + ")",
                        instruction.getMnemonic(), description.toString());
        }

        public String toString() {
            return text;
        }
    }

    private Method method = null;
    private ReferenceType parent = null;
    private byte[] bytecodes = new byte[0];
    private ConstantPool constantPool = null;
    private TreeMap<Integer, DisassembleInstruction> disassemblyForInstruction = new TreeMap<>();

    public DisassembleMethod (Method _method) {
        method = _method;

        // get bytecodes from debugger if possible
        if (!method.isAbstract()) {
            try {
                bytecodes = method.bytecodes();
            } catch (UnsupportedOperationException e) {}
        }

        // get constant pool
        parent = method.declaringType();
        constantPool = new ConstantPool(parent);

        // disassemble instructions
        int pc = 0;
        while (pc < bytecodes.length) {
            Instruction instruction = new Instruction(bytecodes, pc);
            disassemblyForInstruction.put(Integer.valueOf(pc),
                                new DisassembleInstruction(constantPool, instruction));
            pc += instruction.length();
        }
    }

    public String getInstruction(int pc) {
        DisassembleInstruction instructionDisassembly = disassemblyForInstruction.get(Integer.valueOf(pc));
        if (instructionDisassembly == null) {
            return null;
        } else {
            return instructionDisassembly.toString();
        }
    }

    public int getPrevious(int pc) {
        Integer lower = disassemblyForInstruction.lowerKey(Integer.valueOf(pc));
        if (lower == null) {
            return 0;
        } else {
            return lower.intValue();
        }
    }

    public int getNext(int pc) {
        Integer higher = disassemblyForInstruction.higherKey(Integer.valueOf(pc));
        if (higher == null) {
            return bytecodes.length;
        } else {
            return higher.intValue();
        }
    }

    public int length() {
        return bytecodes.length;
    }
}

