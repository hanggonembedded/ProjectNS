package kr.co.driver.serial;

import android.util.Log;

public class FTDriverUtil {
	// Defines of Display Settings
	public static final int DISP_CHAR  = 0;
	public static final int DISP_DEC   = 1;
	public static final int DISP_HEX   = 2;
	
	// Linefeed Code Settings
	public static final int LINEFEED_CODE_CR   = 0;
	public static final int LINEFEED_CODE_CRLF = 1;
	public static final int LINEFEED_CODE_LF   = 2;
	
	public static int mReadLinefeedCode   = LINEFEED_CODE_LF;
	public static int mWriteLinefeedCode  = LINEFEED_CODE_LF;
	
	private final static String BR = System.getProperty("line.separator");
	private static boolean lastDataIs0x0D = false;
	
	public static String changeLinefeedcode(String str) {
        str = str.replace("\\r", "\r");
        str = str.replace("\\n", "\n");
        switch (mWriteLinefeedCode) {
            case LINEFEED_CODE_CR:
                str = str + "\r";
                break;
            case LINEFEED_CODE_CRLF:
                str = str + "\r\n";
                break;
            case LINEFEED_CODE_LF:
                str = str + "\n";
                break;
            default:
        }
        return str;
    }
	
	public static String IntToHex2(final int Value) {
        char HEX2[] = {
                Character.forDigit((Value >> 4) & 0x0F, 16),
                Character.forDigit(Value & 0x0F, 16)
        };
        String Hex2Str = new String(HEX2);
        return Hex2Str;
    }
	
	public static void setSerialDataToTextView(StringBuilder mText, int disp, byte[] rbuf, int len, String sCr, String sLf) {
        int tmpbuf;
        for (int i = 0; i < len; ++i) {

            // "\r":CR(0x0D) "\n":LF(0x0A)
            if ((mReadLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
                mText.append(sCr);
                mText.append(BR);
            } else if ((mReadLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
                mText.append(sLf);
                mText.append(BR);
            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)
                    && (rbuf[i + 1] == 0x0A)) {
                mText.append(sCr);
                if (disp != DISP_CHAR) {
                    mText.append(" ");
                }
                mText.append(sLf);
                mText.append(BR);
                ++i;
            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)) {
                // case of rbuf[last] == 0x0D and rbuf[0] == 0x0A
                mText.append(sCr);
                lastDataIs0x0D = true;
            } else if (lastDataIs0x0D && (rbuf[0] == 0x0A)) {
                if (disp != DISP_CHAR) {
                    mText.append(" ");
                }
                mText.append(sLf);
                mText.append(BR);
                lastDataIs0x0D = false;
            } else if (lastDataIs0x0D && (i != 0)) {
                // only disable flag
                lastDataIs0x0D = false;
                --i;
            } else {
                switch (disp) {
                    case DISP_CHAR:
                        mText.append((char) rbuf[i]);
                        break;
                    case DISP_DEC:
                        tmpbuf = rbuf[i];
                        if (tmpbuf < 0) {
                            tmpbuf += 256;
                        }
                        mText.append(String.format("%1$03d", tmpbuf));
                        mText.append(" ");
                        break;
                    case DISP_HEX:
                        mText.append(IntToHex2((int) rbuf[i]));
                        mText.append(" ");
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
