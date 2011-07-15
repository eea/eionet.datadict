package eionet.meta.exports.xls;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class WarningStyle{
    
    public static final int FONT_HEIGHT = 12;
    
    public static short create(HSSFWorkbook wb){
        
        HSSFCellStyle style = wb.createCellStyle();
        
        HSSFFont font = wb.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short)FONT_HEIGHT);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        style.setFont(font);
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        
        return style.getIndex();
    }
}
