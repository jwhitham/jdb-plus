package com.sun.tools.example.debug.tty;

import com.sun.jdi.Method;
import java.util.HashMap;

public class DisassembleProgram {
    private HashMap<Method, DisassembleMethod> disassembleMethod = new HashMap<>();

    public DisassembleMethod getMethod(Method method) {
        DisassembleMethod dis = disassembleMethod.get(method);
        if (dis == null) {
            dis = new DisassembleMethod(method);
            disassembleMethod.put(method, dis);
        }
        return dis;
    }

    public String getInstruction(Method method, long pc) {
        DisassembleMethod dis = getMethod(method);
        String text = null;
        if ((pc <= (long) Integer.MAX_VALUE) && (pc >= 0)) {
            text = dis.getInstruction((int) pc);
        }
        if (text == null) {
            text = "pc(" + Long.toString(pc) + ")";
        }
        return text;
    }

}


