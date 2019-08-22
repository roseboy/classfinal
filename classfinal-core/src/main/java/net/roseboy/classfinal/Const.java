package net.roseboy.classfinal;

/**
 * 常量
 *
 * @author roseboy
 */
public class Const {

    //打包时需要删除的文件
    public static final String[] DLE_FILES = {".DS_Store", "Thumbs.db"};

    //加密出来的文件名
    public static final String FILE_NAME = "classes";

    //lib下的jar解压的目录名后缀
    public static final String LIB_JAR_DIR = "__temp__";

    //调试模式
    public static boolean DEBUG = false;

    public static void pringInfo() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println("=                                                       =");
        System.out.println("=         Java Class Encryption Tool   by Mr.K          =");
        System.out.println("=                                                       =");
        System.out.println("=========================================================");
        System.out.println();
    }

    public static void printDog() {
        System.out.println("                                                                           ");
        System.out.println("                                                                           ");
        System.out.println("          .,:,,,                                        .::,,,::.          ");
        System.out.println("        .::::,,;;,                                  .,;;:,,....:i:         ");
        System.out.println("        :i,.::::,;i:.      ....,,:::::::::,....   .;i:,.  ......;i.        ");
        System.out.println("        :;..:::;::::i;,,:::;:,,,,,,,,,,..,.,,:::iri:. .,:irsr:,.;i.        ");
        System.out.println("        ;;..,::::;;;;ri,,,.                    ..,,:;s1s1ssrr;,.;r,        ");
        System.out.println("        :;. ,::;ii;:,     . ...................     .;iirri;;;,,;i,        ");
        System.out.println("        ,i. .;ri:.   ... ............................  .,,:;:,,,;i:        ");
        System.out.println("        :s,.;r:... ....................................... .::;::s;        ");
        System.out.println("        ,1r::. .............,,,.,,:,,........................,;iir;        ");
        System.out.println("        ,s;...........     ..::.,;:,,.          ...............,;1s        ");
        System.out.println("       :i,..,.              .,:,,::,.          .......... .......;1,       ");
        System.out.println("      ir,....:rrssr;:,       ,,.,::.     .r5S9989398G95hr;. ....,.:s,      ");
        System.out.println("     ;r,..,s9855513XHAG3i   .,,,,,,,.  ,S931,.,,.;s;s&BHHA8s.,..,..:r:     ");
        System.out.println("    :r;..rGGh,  :SAG;;G@BS:.,,,,,,,,,.r83:      hHH1sXMBHHHM3..,,,,.ir.    ");
        System.out.println("   ,si,.1GS,   sBMAAX&MBMB5,,,,,,:,,.:&8       3@HXHBMBHBBH#X,.,,,,,,rr    ");
        System.out.println("   ;1:,,SH:   .A@&&B#&8H#BS,,,,,,,,,.,5XS,     3@MHABM&59M#As..,,,,:,is,   ");
        System.out.println("  .rr,,,;9&1   hBHHBB&8AMGr,,,,,,,,,,,:h&&9s;   r9&BMHBHMB9:  . .,,,,;ri.  ");
        System.out.println("  :1:....:5&XSi;r8BMBHHA9r:,......,,,,:ii19GG88899XHHH&GSr.      ...,:rs.  ");
        System.out.println("  ;s.     .:sS8G8GG889hi.        ....,,:;:,.:irssrriii:,.        ...,,i1,  ");
        System.out.println("  ;1,         ..,....,,isssi;,        .,,.                      ....,.i1,  ");
        System.out.println("  ;h:               i9HHBMBBHAX9:         .                     ...,,,rs,  ");
        System.out.println("  ,1i..            :A#MBBBBMHB##s                             ....,,,;si.  ");
        System.out.println("  .r1,..        ,..;3BMBBBHBB#Bh.     ..                    ....,,,,,i1;   ");
        System.out.println("   :h;..       .,..;,1XBMMMMBXs,.,, .. :: ,.               ....,,,,,,ss.   ");
        System.out.println("    ih: ..    .;;;, ;;:s58A3i,..    ,. ,.:,,.             ...,,,,,:,s1,    ");
        System.out.println("    .s1,....   .,;sh,  ,iSAXs;.    ,.  ,,.i85            ...,,,,,,:i1;     ");
        System.out.println("     .rh: ...     rXG9XBBM#M#MHAX3hss13&&HHXr         .....,,,,,,,ih;      ");
        System.out.println("      .s5: .....    i598X&&A&AAAAAA&XG851r:       ........,,,,:,,sh;       ");
        System.out.println("      . ihr, ...  .         ..                    ........,,,,,;11:.       ");
        System.out.println("         ,s1i. ...  ..,,,..,,,.,,.,,.,..       ........,,.,,.;s5i.         ");
        System.out.println("          .:s1r,......................       ..............;shs,           ");
        System.out.println("          . .:shr:.  ....                 ..............,ishs.             ");
        System.out.println("              .,issr;,... ...........................,is1s;.               ");
        System.out.println("                 .,is1si;:,....................,:;ir1sr;,                  ");
        System.out.println("                    ..:isssssrrii;::::::;;iirsssssr;:..                    ");
        System.out.println("                         .,::iiirsssssssssrri;;:.                          ");
        System.out.println("");

        try {
            for (int i = 0; i < 30; i++) {
                System.out.print(".");
                Thread.sleep(100);
            }
            System.out.println("ok");
        } catch (Exception e) {

        }
    }
}
