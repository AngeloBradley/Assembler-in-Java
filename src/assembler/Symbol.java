
package assembler;

/**
 * Symbols for assembler symbol table
 * @author Angelo Bradley at NCAT February 4th, 2018
 */
public class Symbol {
    final String name;
    final int value;
    final int machineFormat;
    final int length;
    
    
    /*
    @param identifier = name used in the assembler program
    @param addr = address or value of the name
    */
    public Symbol(String identifier, int addr){
        name = identifier;
        value = addr;
        machineFormat = -1;
        length = -1;
    }
    
    public Symbol(String identifier, int opcode, int len, int format){
        name = identifier;
        value = opcode;
        machineFormat = format;
        length = len;
    }
    
    
}
