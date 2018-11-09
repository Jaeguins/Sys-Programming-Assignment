import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
class CustScanner {
    Scanner scanner;
    public CustScanner(File source) throws FileNotFoundException {
        scanner=new Scanner(source);
    }
    public boolean hasNext(){
        return scanner.hasNext();
    }
    public String next(){
        String ret=scanner.next();
        if(ret.length()==0)
            return next();
        else return ret;
    }
    public void close(){
        scanner.close();
    }
    public String nextLine(){
        String ret=scanner.nextLine();
        if(ret.charAt(0)=='/'&&ret.charAt(1)=='/')
            return nextLine();
        else return ret;
    }
}
public class Assembler {
    static int MaxI, InstrP = 0,MaxS = 0,MAX_INS=3;
    static int DataCounter=0;
    Reg[] reg=new Reg[20];
    Ins[] instr=new Ins[100],modInstr=new Ins[100];
    public LinkedList<Symbol> symbolTbl=new LinkedList<>();
    Sentence sen=new Sentence();
    int LC;
    File f=new File("ObjCode.txt");
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
            CustScanner regi = new CustScanner(new File("reg.tbl"));
            CustScanner inst = new CustScanner(new File("inst.tbl"));
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
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    int Analyze(String operand) {
        int i = 0;
        String[] regist = { "AX","BX","CX","DX","AL","BL","CL","DL","AH","BH","CH","DH","0x00" };
        if (Character.isDigit(operand.charAt(0)))return 0;
        else {
            while (i!=regist.length) {
                if (operand.equals(regist[i])) {
                    if(regist[i].charAt(0)=='A'){
                        if(regist[i].charAt(1)=='X')return 5;
                        else return 4;
                    }
                    else if (i < 4)return 1;
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
        String[] opcode = { "MOV","ADD","SUB","" };
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
        while (!opcode[i].equals("")) {
            if (!opcode[i].equals(op[0]))i++;
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
            case 4: instr[0].dest="a";
                instr[0].word_type="b";
                break;
            case 5:instr[0].dest="a";
                instr[0].word_type="w";
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
            case 4: instr[0].sour="a";
                instr[0].word_type="b";
                break;
            case 5:instr[0].sour="a";
                instr[0].word_type="w";
                break;
        }
        while (!instr[k].instruct.equals(instr[0].instruct) || !instr[k].dest.equals(instr[0].dest) ||
                !instr[k].sour.equals(instr[0].sour) || !instr[k].word_type.equals(instr[0].word_type)) {
            k--;
        }
        return k;
    }
    void PassI(String buf) {
        int i;
        i = Add_Chk(buf);
        if (i!=0) {
            System.out.printf("%04X: %s\n", LC, buf);
            LC += Integer.parseInt(instr[i].ins_len);
        }
        else {
            Symbol tSymbol=new Symbol();
            symbolTbl.add(tSymbol);
            if (sen._operator.equals("DW"))
                tSymbol.word_type = "w";
            else if (sen._operator.equals("DB"))
                tSymbol.word_type="b";
            tSymbol.symbol=sen.label;
            tSymbol.data=sen.operand[0];
            tSymbol.lc = LC;
            System.out.printf("%04X: %s\n", LC, buf);
            if (tSymbol.word_type.charAt(0) == 'w')LC += 2;
            else if (tSymbol.word_type.charAt(0) == 'b')LC += 1;
            DataCounter++;
            MaxS++;
        }
    }
    int btoi(String dig) {
        int i = 0, ret = 0;
        while (i<dig.length()) {
            if (dig.charAt(i) == '1')ret += Math.pow(2, dig.length()-i- 1);
            i++;
        }
        return ret;
    }

    void PassII(String buf) {
        int i, j = 0, k = 0;
        OutputStream ObjSave;
        try {

            ObjSave = new FileOutputStream(f,true);
            i = Add_Chk(buf);
            if (i != 0) {
                modInstr[InstrP] = instr[i].copy();
                System.out.printf("%04X: %3s", LC, instr[i].ins_code);
                ObjSave.write(String.format("%04x: %3s", LC, instr[i].ins_code).getBytes());
                if (instr[i].dest.equals("a")||instr[i].dest.equals("r")) {
                    while (!reg[j].reg_name.equals(sen.operand[0]))
                        j++;
                    modInstr[InstrP].mod_reg=modInstr[InstrP].mod_reg.replaceFirst("\\?\\?\\?",reg[j].reg_num);
                }
                j = 0;
                if (instr[i].sour.equals("a")||instr[i].sour.equals("r")) {
                    while (!reg[j].reg_name.equals(sen.operand[1])) j++;
                    modInstr[InstrP].mod_reg=modInstr[InstrP].mod_reg.replaceFirst("\\?\\?\\?",reg[j].reg_num);
                }

                if(!instr[i].dest.equals("m") &&!instr[i].sour.equals("m")) {
                    System.out.printf(" %02X\t\t\t%s\n", btoi(modInstr[InstrP].mod_reg), buf);
                    ObjSave.write(String.format(" %02X\t\t\t%s\n", btoi(modInstr[InstrP].mod_reg), buf).getBytes());
                } else{
                    if(k<symbolTbl.size()) {
                        if (instr[i].ins_code.charAt(0)!='A'){
                            System.out.printf(" %02X",btoi(modInstr[InstrP].mod_reg));
                            ObjSave.write(String.format(" %02X",btoi(modInstr[InstrP].mod_reg)).getBytes());
                        }
                        if (instr[i].dest.equals("m") || instr[i].sour.equals("m")) {
                            if (instr[i].dest.equals("m"))
                                while (!symbolTbl.get(k).symbol.equals(sen.operand[0])) k++;
                            else if (instr[i].sour.equals("m"))
                                while (!symbolTbl.get(k).symbol.equals(sen.operand[1])) k++;
                            Symbol tSym = symbolTbl.get(k);
                            if (tSym.lc == (Integer.parseInt(tSym.data))) {
                                System.out.printf(" %02X %02X\t\t%s\n", tSym.lc%0x100,tSym.lc/0x100, buf);
                                ObjSave.write(String.format(" %02X %02X\t\t%s\n", tSym.lc%0x100,tSym.lc/0x100, buf).getBytes());
                            } else {
                                System.out.printf(" %02X %02X R\t\t%s\n",  tSym.lc%0x100,tSym.lc/0x100, buf);
                                ObjSave.write(String.format(" %02X %02X R\t\t%s\n", tSym.lc%0x100,tSym.lc/0x100, buf).getBytes());
                            }
                        }
                        else {
                            System.out.printf(" %02X\t\t\t%s\n", btoi(modInstr[InstrP].mod_reg), buf);
                            ObjSave.write(String.format(" %02X\t\t\t%s\n", btoi(modInstr[InstrP].mod_reg), buf).getBytes());
                        }
                    }
                }
                LC += Integer.parseInt(instr[i].ins_len);
            } else {
                k = 0;
                while (strcmp(symbolTbl.get(k).symbol, sen.label)) k++;
                Symbol tSym=symbolTbl.get(k);
                if (!strcmp(tSym.word_type, "w")) {
                    System.out.printf("%04X:%04X\t\t%s\n", LC, Integer.parseInt(tSym.data), buf);
                    ObjSave.write(String.format("%04X:%04X\t\t%s\n", LC, Integer.parseInt(tSym.data), buf).getBytes());
                }
                if (!strcmp(tSym.word_type, "b")) {
                    System.out.printf("%04X:%02X\t\t%s\n", LC, Integer.parseInt(tSym.data), buf);
                    ObjSave.write(String.format("%04X:%02X\t\t%s\n", LC, Integer.parseInt(tSym.data), buf).getBytes());
                }
                if (tSym.word_type.equals("w"))LC += 2;
        else if (tSym.word_type.equals("b"))LC += 1;
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
                Symbol tSym=symbolTbl.get(i);
                if (!strcmp(tSym.word_type, "w")) {
                    System.out.printf("%s\t%X\t\t%d \n", tSym.symbol, tSym.lc, (tSym.lc != Integer.parseInt(tSym.data) ? 1 : 0));
                    SymbolSave.write(String.format("%s\t%X\t\t%d \n", tSym.symbol, tSym.lc, (tSym.lc != Integer.parseInt(tSym.data) ? 1 : 0)).getBytes());
                }
                if (!strcmp(tSym.word_type, "b")) {
                    System.out.printf("%s\t%X\t\t%d \n", tSym.symbol, tSym.lc, (tSym.lc != Integer.parseInt(tSym.data) ? 1 : 0));
                    SymbolSave.write(String.format("%s\t%X\t\t%d \n", tSym.symbol, tSym.lc, (tSym.lc != Integer.parseInt(tSym.data) ? 1 : 0)).getBytes());
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
            Scanner sysScanner = new Scanner(System.in);
            CustScanner in = new CustScanner(new File("test1.asm"));
            Assembler asm = new Assembler();
            asm.Initialize();
            System.out.printf("\nPass1:\n");

            while (true) {
                if (!in.hasNext()) break;
                buf = in.nextLine();
                asm.PassI(buf.toUpperCase());
            }
            asm.Symbol_Print();
            in = new CustScanner(new File("test1.asm"));
            asm.LC = 0;
            System.out.printf("\nPass2:\n");
            asm.f.delete();
            while (true) {
                if (!in.hasNext()) break;
                buf = in.nextLine();
                asm.PassII(buf.toUpperCase());
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
    public Ins copy(){
        Ins i=new Ins();
        i.instruct=instruct;
        i.dest=dest;
        i.sour=sour;
        i.word_type=word_type;
        i.ins_code=ins_code;
        i.ins_len=ins_len;
        i.mod_reg=mod_reg;
        return i;
    }
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







