package assembler;

import java.io.File;
import java.util.*;

/**
 * Assembler
 * @author Angelo Bradley at NCAT February 4th, 2018
 */
public class Assembler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws java.io.IOException {
        int currentAddr = 0;
        StringTokenizer tokenizer;
        String instName;
        String oper;
        Symbol opSymbol;
        int machineInstr;
        int r1, r2, r3, addr;
        HashMap<String, Symbol> symbolTable = new HashMap<>(64);

        File myFile = new File("input.txt");
        Scanner inSource = new Scanner(myFile);

        symbolTable.put("add", new Symbol("add", 0, 2, 0));
        symbolTable.put("sub", new Symbol("sub", 1, 2, 0));
        symbolTable.put("mult", new Symbol("mult", 2, 2, 0));
        symbolTable.put("div", new Symbol("div", 3, 2, 0));
        symbolTable.put("ld", new Symbol("ld", 4, 4, 1));
        symbolTable.put("la", new Symbol("la", 5, 4, 1));
        symbolTable.put("st", new Symbol("st", 6, 4, 1));
        symbolTable.put("call", new Symbol("call", 7, 4, 2));
        symbolTable.put("rtn", new Symbol("rtn", 8, 1, 4));
        symbolTable.put("jmp", new Symbol("jmp", 9, 4, 2));
        symbolTable.put("jz", new Symbol("jz", 10, 4, 1));
        symbolTable.put("jn", new Symbol("jn", 11, 4, 1));
        symbolTable.put("push", new Symbol("push", 12, 1, 3));
        symbolTable.put("pop", new Symbol("pop", 13, 1, 3));
        symbolTable.put("lpsw", new Symbol("lpsw", 14, 1, 3));
        symbolTable.put("spsw", new Symbol("spsw", 15, 1, 3));

        for (int i = 0; i < 16; i++) {
            String regName = "r" + i;
            symbolTable.put(regName, new Symbol(regName, i));
        }

        /* First Pass */
        String inLine = inSource.nextLine();
        while (inSource.hasNextLine()) {
            if (!inLine.startsWith("*")) {
                tokenizer = new StringTokenizer(inLine, "\t+,[ ]", false);
                String first = tokenizer.nextToken();
                if (symbolTable.get(first) == null) {
                    String label = first;
                    Symbol location = new Symbol(label, currentAddr);
                    Symbol dup = symbolTable.put(label, location);
                    if (dup != null) {
                        System.out.println("* Duplicate use of label " + label + " *");
                    }
                }
                instName = first.toLowerCase();
                Symbol inst = symbolTable.get(instName);
                if (inst == null) {
                    System.out.println("* Invalid instruction " + instName + " *");
                } else {
                    addr = inst.length;
                }
            }
            inLine = inSource.nextLine();
        }

        /* Second pass */
        inSource.close();
        inSource = new Scanner(myFile);
        addr = 0;

        inLine = inSource.nextLine();
        while (inSource.hasNextLine()) {
            if (inLine.startsWith("*")) {
                System.out.println(inLine);
            } else {
                machineInstr = 0;
                tokenizer = new StringTokenizer(inLine, "\t+,[ ]", false);
                if (inLine.charAt(0) != ' ' && inLine.charAt(0) != '\t') {
                    tokenizer.nextToken();
                }
                instName = tokenizer.nextToken().toLowerCase();
                Symbol inst = symbolTable.get(instName);
                if (inst == null) {
                    System.out.println("* Invalid Instruction " + instName + " *");
                } else {
                    switch (inst.machineFormat) {
                        case 0: //Arithmetic Instruction
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("* Missing register 1 for " + instName + " *");
                                break;
                            }
                            oper = tokenizer.nextToken().toLowerCase();
                            opSymbol = symbolTable.get(oper); // get operand information
                            if (opSymbol == null) {
                                System.out.println("* invalid register 1 " + oper + " *");
                                break;
                            }
                            r1 = opSymbol.value; // get register value
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("* missing register 2 for " + instName + " *");
                                break;
                            }
                            oper = tokenizer.nextToken().toLowerCase();
                            opSymbol = symbolTable.get(oper); // get operand information
                            if (opSymbol == null) {
                                System.out.println("* invalid register 2 " + oper + " *");
                                break;
                            }
                            r2 = opSymbol.value;
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("* missing register 3 for " + instName + " *");
                                break;
                            }
                            oper = tokenizer.nextToken().toLowerCase();
                            opSymbol = symbolTable.get(oper);// get operand information
                            if (opSymbol == null) {
                                System.out.println("* invalid register 3 " + oper + " *");
                                break;
                            }
                            r3 = opSymbol.value;
                            machineInstr = (inst.value << 12) | (r1 << 8) | (r2 << 4) | r3;
                            break;
                        case 1: //Load, Store and Conditional Jumps
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("missing register 1");
                                break;
                            }
                            oper = tokenizer.nextToken().toLowerCase();
                            opSymbol = symbolTable.get(oper); // get operand information
                            if (opSymbol == null) {
                                System.out.println("* invalid register " + oper + " *");
                                break;
                            }
                            r1 = opSymbol.value; // get register value
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("* missing address for " + instName + " *");
                                break;
                            }
                            oper = tokenizer.nextToken();
                            opSymbol = symbolTable.get(oper); // get operand information
                            if (opSymbol == null) {
                                System.out.println("* undefined address label " + oper + " *");
                                break;
                            }
                            addr = opSymbol.value; // get register value
                            if (tokenizer.hasMoreTokens()) { // if index register
                                oper = tokenizer.nextToken().toLowerCase();
                                opSymbol = symbolTable.get(oper); // get operand information
                                if (opSymbol == null) {
                                    System.out.println("* undefined index register " + oper + " *");
                                    break;
                                }
                                r2 = opSymbol.value; // get register value
                            } else {
                                r2 = 0; // if no index reg given
                            }
                            machineInstr = (inst.value << 28) | (r1 << 24) | (r2 << 20) | addr;
                            break;
                        case 2: //Call and Unconditional Jumps
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("* missing address for " + instName + " *");
                                break;
                            }
                            oper = tokenizer.nextToken();
                            opSymbol = symbolTable.get(oper); // get operand information
                            if (opSymbol == null) {
                                System.out.println("* undefined address label " + oper + " *");
                                break;
                            }
                            addr = opSymbol.value; // get register value
                            if (tokenizer.hasMoreTokens()) { // if index register
                                oper = tokenizer.nextToken().toLowerCase();
                                opSymbol = symbolTable.get(oper); // get operand information
                                if (opSymbol == null) {
                                    System.out.println("* undefined index register " + oper + " *");
                                    break;
                                }
                                r2 = opSymbol.value; // get register value
                            } else {
                                r2 = 0; // if no index reg given
                            }
                            machineInstr = (inst.value << 28) | (r2 << 20) | addr;
                            break;
                        case 3: //Push, Pop and Lpsw
                            if (!tokenizer.hasMoreTokens()) {
                                System.out.println("* missing register for " + instName + " *");
                                break;
                            }
                            oper = tokenizer.nextToken().toLowerCase();
                            opSymbol = symbolTable.get(oper); // get operand information
                            if (opSymbol == null) {
                                System.out.println("* invalid register " + oper + " *");
                                break;
                            }
                            r1 = opSymbol.value; // get register value
                            machineInstr = (inst.value << 4) | r1;
                            break;
                        case 4: //Return
                            machineInstr = inst.value << 4;
                            break;

                    }
                    /* Output the machine language */
                    String hexAddr = Integer.toUnsignedString(currentAddr, 16);
                    String hexInst = Integer.toUnsignedString(machineInstr, 16);
                    System.out.printf("%8s %8s %s\n", hexAddr, hexInst, inLine);
                    currentAddr += inst.length;
                }
            }
            inLine = inSource.nextLine();
        }

    }

}
