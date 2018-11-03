import java.io.*;
import java.util.Scanner;

public class Assembler {
    static int MaxI, InstrP = 0,MaxS = 0,MAX_INS=1;
    Reg[] reg=new Reg[20];
    Ins[] instr=new Ins[100],modInstr=new Ins[100];
    public Symbol[] symbolTbl=new Symbol[30];
    Sentence sen;
    int LC;

    static String strncpy(String string1,String string2,int count){
        String ret="";
        ret+=string2.substring(0,count);
        ret+=string1.substring(count);
        return ret;
    }
    static String strchr(String string,String key){
        return string.substring(string.indexOf(key));
    }
    static boolean strcmp(String str1,String str2){
        if(str1.equals(str2))return false;
        else return true;
    }
    void Initialize() {
        int i = 0, j = 1;
        instr[0]=new Ins();
        try {
            Scanner regi = new Scanner(new File("reg.tbl"));
            Scanner inst = new Scanner(new File("inst.tbl"));
            while (regi.hasNext()) {
                reg[i]=new Reg();
                reg[i].reg_name = regi.next();
                reg[i].reg_num = regi.next();
                i++;
            }
            while (inst.hasNext()) {
                instr[j]=new Ins();
                instr[j].instruct = inst.next();
                instr[j].dest = inst.next();
                instr[j].sour = inst.next();
                instr[j].word_type = inst.next();
                instr[j].ins_code = inst.next();
                instr[j].ins_len = inst.next();
                instr[j].mod_reg = inst.next();
                j++;
            }
            MaxI = j - 1;
            regi.close();
            inst.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    int Analyze(String operand) {
        int i = 0;
        String[] regist = { "AX","BX","CX","DX","AL","BL","CL","DL","AH","BH","CH","DH","0x00" };
        if (Character.isDigit(operand.charAt(0)))return 0;
        else {
            while (i!=12) {
                if (operand.equals(regist[i])) {
                    if (i < 4)return 1;
                    else return 2;
                }
                else i++;
            }
        }
        return 3;
    }

    int Add_Chk(String sen) {
        int k = MaxI;
        int i = 0, j = 0, l = 0, wp = 0;
        String[] op=new String[6];
        String[] opcode = { "MOV","" };
        while (wp<sen.length()) {
            while (wp<sen.length()&&sen.charAt(wp) == ' ' || sen.charAt(wp) == '\t' || sen.charAt(wp) == ',')
                wp++;
            op[j]="";
            while (wp<sen.length()&&sen.charAt(wp) != ' '&&sen.charAt(wp) != '\n'&&sen.charAt(wp) != ',') {
                op[j]+=sen.charAt(wp);
                i++;
                wp++;
            }
            i = 0;
            j++;
        }
        i = 0;
        while (opcode[i].equals("")) {
            if (opcode[i].equals(op[0]))i++;
            else {
                this.sen._operator=op[0];
                for (l = 1; l < j; l++)
                    this.sen.operand[l - 1]=op[l];
                break;
            }
        }
        if (i == MAX_INS) {
            this.sen.label=op[0];
            this.sen._operator=op[1];
            for (l = 2; l < j; l++)
                this.sen.operand[l - 2]=op[l];
        }

        instr[0].instruct=op[0];
        switch (Analyze(op[1])) {
            case 0: instr[0].dest="i";
                break;
            case 1: instr[0].dest="r";
                instr[0].word_type="w";
                break;
            case 2: instr[0].dest="r";
                instr[0].word_type="b";
                break;
            case 3: instr[0].dest="m";
                break;
        }
        switch (Analyze(op[2])) {
            case 0: instr[0].sour="i";
                break;
            case 1: instr[0].sour="r";
                instr[0].word_type="w";
                break;
            case 2: instr[0].sour="r";
                instr[0].word_type="b";
                break;
            case 3: instr[0].sour="m";
                break;
        }
        while (instr[k].instruct.equals(instr[0].instruct) && instr[k].dest.equals(instr[0].dest) &&
                instr[k].sour.equals(instr[0].sour) && instr[k].word_type.equals(instr[0].word_type)) {
            k--;
        }
        return k;
    }
    void PassI(String buf) {
        int i; int j = 0;
        i = Add_Chk(buf);
        if (i!=0) {
            System.out.printf("%04X: %s\n", LC, buf);
            LC += Integer.parseInt(instr[i].ins_len);
        }
        else {
            symbolTbl[j]=new Symbol();
            if (sen._operator.equals("DW"))
                symbolTbl[j].word_type = "w";
            else if (sen._operator.equals("DB"))
                symbolTbl[j].word_type="b";
            symbolTbl[j].symbol=sen.label;
            symbolTbl[j].data=sen.operand[0];
            symbolTbl[j].lc = LC;
            System.out.printf("%04X: %s\n", LC, buf);
            if (symbolTbl[j].word_type.charAt(0) == 'w')LC += 2;
            else if (symbolTbl[j].word_type.charAt(0) == 'b')LC += 1;
            j++;
            MaxS++;
        }
    }
    int btoi(String dig) {
        int i = 0, ret = 0;
        while (dig.charAt(i) != '\0') {
            if (dig.charAt(i) == '1')ret += Math.pow(2, dig.length()-i- 1);
            i++;
        }
        return ret;
    }

    void PassII(String buf) {
        int i, j = 0, k = 0;
        OutputStream ObjSave;
        try {
            ObjSave = new FileOutputStream("ObjCode.txt");
            i = Add_Chk(buf);
            if (i != 0) {
                modInstr[InstrP] = instr[i];
                System.out.printf("%04X: %3s", LC, instr[i].ins_code);
                ObjSave.write(String.format("%04x: %3s", LC, instr[i].ins_code).getBytes());
                if (instr[i].dest.equals("r")) {
                    while (!reg[j].reg_name.equals(sen.operand[0]))
                        j++;
                    strncpy(strchr(modInstr[InstrP].mod_reg, "?"), reg[j].reg_num, 3);
                }
                j = 0;
                if (instr[i].sour.equals("r")) {
                    while (!reg[j].reg_name.equals(sen.operand[1])) j++;
                    strncpy(strchr(modInstr[InstrP].mod_reg, "?"), reg[j].reg_num, 3);
                }
                if (instr[i].dest.equals("m") &&!instr[i].sour.equals("m")) {
                    System.out.printf(" %02X\t\t%s", btoi(modInstr[InstrP].mod_reg), buf);
                    ObjSave.write(String.format(" %02X\t\t%s", btoi(modInstr[InstrP].mod_reg), buf).getBytes());
                } else {
                    if (instr[i].dest.equals("m"))
                        while (!symbolTbl[k].symbol.equals(sen.operand[0])) k++;
                    else if (instr[i].sour.equals("m"))
                        while (!symbolTbl[k].symbol.equals(sen.operand[1])) k++;
                    if (symbolTbl[k].lc == (Integer.parseInt(symbolTbl[k].data))) {
                        System.out.printf(" %02X\t%04X\t%s", btoi(modInstr[InstrP].mod_reg), symbolTbl[k].lc, buf);
                        ObjSave.write(String.format(" %02X\t%04X\t%s", btoi(modInstr[InstrP].mod_reg), symbolTbl[k].lc, buf).getBytes());
                    } else {
                        System.out.printf(" %02X\t%04X R\t%s", btoi(modInstr[InstrP].mod_reg), symbolTbl[k].lc, buf);
                        ObjSave.write(String.format(" %02X\t%04X R\t%s", btoi(modInstr[InstrP].mod_reg), symbolTbl[k].lc, buf).getBytes());
                    }
                }
                LC += Integer.parseInt(instr[i].ins_len);
            } else {
                k = 0;
                while (strcmp(symbolTbl[k].symbol, sen.label)) k++;
                if (!strcmp(symbolTbl[k].word_type, "w")) {
                    System.out.printf("%04X:%04X\t\t%s", LC, Integer.parseInt(symbolTbl[k].data), buf);
                    ObjSave.write(String.format("%04X:%04X\t\t%s", LC, Integer.parseInt(symbolTbl[k].data), buf).getBytes());
                }
                if (!strcmp(symbolTbl[k].word_type, "b")) {
                    System.out.printf("%04X:%02X\t\t%s", LC, Integer.parseInt(symbolTbl[k].data), buf);
                    ObjSave.write(String.format("%04X:%02X\t\t%s", LC, Integer.parseInt(symbolTbl[k].data), buf).getBytes());
                }
                if (symbolTbl[k].word_type.equals("w"))LC += 2;
        else if (symbolTbl[k].word_type.equals("b"))LC += 1;
            }
            InstrP++;
            ObjSave.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    void Symbol_Print() {
        int i = 0;
        OutputStream SymbolSave;
        try {
            SymbolSave= new FileOutputStream("symbol.txt");
            System.out.printf("\n* symbolTbl Table *\n");
            System.out.printf("SYMBOL\tData(ADDRESS)\tRELOCATION\n");
            for (i = 0; i < MaxS; i++) {
                if (!strcmp(symbolTbl[i].word_type, "w")) {
                    System.out.printf("%s\t%X\t\t%d \n", symbolTbl[i].symbol, symbolTbl[i].lc, (symbolTbl[i].lc != Integer.parseInt(symbolTbl[i].data) ? 1 : 0));
                    SymbolSave.write(String.format("%s\t%X\t\t%d \n", symbolTbl[i].symbol, symbolTbl[i].lc, (symbolTbl[i].lc != Integer.parseInt(symbolTbl[i].data) ? 1 : 0)).getBytes());
                }
                if (!strcmp(symbolTbl[i].word_type, "b")) {
                    System.out.printf("%s\t%X\t\t%d \n", symbolTbl[i].symbol, symbolTbl[i].lc, (symbolTbl[i].lc != Integer.parseInt(symbolTbl[i].data) ? 1 : 0));
                    SymbolSave.write(String.format("%s\t%X\t\t%d \n", symbolTbl[i].symbol, symbolTbl[i].lc, (symbolTbl[i].lc != Integer.parseInt(symbolTbl[i].data) ? 1 : 0)).getBytes());
                }
            }
            SymbolSave.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        String buf;
        try {
            Scanner sysScanner = new Scanner(System.in), in = new Scanner(new File("test1.asm"));
            Assembler asm = new Assembler();
            asm.Initialize();
            System.out.printf("\nPass1:\n");
            while (true) {
                if (!in.hasNext()) break;
                buf = in.nextLine();
                asm.PassI(buf);
            }
            asm.Symbol_Print();
            in = new Scanner("/test1.asm");
            asm.LC = 0;
            System.out.printf("\nPass2:\n");
            while (true) {
                if (!in.hasNext()) break;
                buf = in.nextLine();
                asm.PassII(buf);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

class Reg {
    String reg_name="";
    String reg_num="";
}

class Ins {
    String instruct="";
    String dest="";
    String sour="";
    String word_type="";
    String ins_code="";
    String ins_len="";
    String mod_reg="";
}

class Symbol {
    String symbol="";
    String word_type="";
    int lc=0;
    String data="";
}

class Sentence {
    String label="";
    String _operator="";
    String[] operand=new String[3];
    public Sentence(){
        for(int i=0;i<3;i++){
            operand[i]="";
        }
    }
}







